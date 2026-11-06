package com.wharzc.sitemate.model

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wharzc.sitemate.api.CloudinaryUnsignedUploader
import com.wharzc.sitemate.api.UploadResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

import kotlinx.coroutines.flow.collect

// Updated data classes – store Cloudinary URL only
data class LoggedWork(
    val name: String,
    val cloudinaryUrl: String? = null,
    val isUploading: Boolean = false
)

data class ResourceLog(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val name: String,
    val quantity: Int,
    val cost: String,
    val cloudinaryUrl: String? = null,
    val isUploading: Boolean = false
)

class ProjectDailyViewModel : ViewModel() {
    var loggedWorks = mutableStateListOf<LoggedWork>()
    var manpowerList = mutableStateListOf<Pair<String, Int>>()
    var equipmentList = mutableStateListOf<ResourceLog>()
    var materialList = mutableStateListOf<ResourceLog>()

    // Manpower photo: just a URL
    var manpowerPhotoUrl by mutableStateOf<String?>(null)

    var availableWorksForModal by mutableStateOf<List<String>>(emptyList())
        private set
    var currentActiveMilestoneId by mutableStateOf<String?>(null)
        private set

    var totalWorksForActiveMilestone = 0
        private set
    var totalMilestonesCount = 0
        private set

    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)

    private val todayDateString: String
        get() {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("Asia/Manila")
            return format.format(Date())
        }

    // ------------------------------------------------------------------
    // Upload helpers
    // ------------------------------------------------------------------
    private suspend fun uploadImage(uri: Uri, projectId: String, subFolder: String): String? {
        var resultUrl: String? = null
        CloudinaryUnsignedUploader.uploadFile(uri, projectId, subFolder).collect { result: UploadResult ->
            when (result) {
                is UploadResult.Success -> resultUrl = result.secureUrl
                is UploadResult.Error -> Log.e("DailyLog", "Upload failed: ${result.message}")
                else -> { /* ignore progress/loading */ }
            }
        }
        return resultUrl
    }

    fun addWorkWithPhoto(workName: String, photoUri: Uri, projectId: String) {
        viewModelScope.launch {
            val tempWork = LoggedWork(name = workName, isUploading = true)
            loggedWorks.add(tempWork)

            val url = uploadImage(photoUri, projectId, "daily/${todayDateString}")

            val index = loggedWorks.indexOf(tempWork)
            if (index != -1 && url != null) {
                loggedWorks[index] = tempWork.copy(cloudinaryUrl = url, isUploading = false)
            } else if (index != -1) {
                loggedWorks.removeAt(index) // remove on failure
            }
            autoSaveDraft(projectId)
        }
    }

    fun addManpowerPhoto(photoUri: Uri, projectId: String) {
        viewModelScope.launch {
            // Show uploading state? Could add a boolean, but simple for now.
            val url = uploadImage(photoUri, projectId, "daily/${todayDateString}/manpower")
            if (url != null) {
                manpowerPhotoUrl = url
                autoSaveDraft(projectId)
            }
        }
    }

    fun addResourcePhoto(resource: ResourceLog, photoUri: Uri, projectId: String) {
        viewModelScope.launch {
            // Update the specific resource with uploading flag
            val index = when (resource.type) {
                "Equipment" -> equipmentList.indexOf(resource)
                else -> materialList.indexOf(resource)
            }
            if (index != -1) {
                val updated = resource.copy(isUploading = true)
                if (resource.type == "Equipment") equipmentList[index] = updated
                else materialList[index] = updated
            }

            val url = uploadImage(photoUri, projectId, "daily/${todayDateString}/${resource.type.lowercase()}")

            if (url != null && index != -1) {
                val finalResource = resource.copy(cloudinaryUrl = url, isUploading = false)
                if (resource.type == "Equipment") equipmentList[index] = finalResource
                else materialList[index] = finalResource
            } else if (index != -1) {
                // remove or revert? We'll revert by setting back original without URL
                if (resource.type == "Equipment") equipmentList[index] = resource
                else materialList[index] = resource
            }
            autoSaveDraft(projectId)
        }
    }

    // ------------------------------------------------------------------
    // Load existing log from Firestore
    // ------------------------------------------------------------------
    @Suppress("UNCHECKED_CAST")
    fun loadDailyLog(projectId: String, projectType: String) {
        isLoading = true
        val db = FirebaseFirestore.getInstance()

        db.collection("projects").document(projectId).collection("daily_logs").document(todayDateString).get()
            .addOnSuccessListener { document ->
                loggedWorks.clear(); manpowerList.clear(); equipmentList.clear(); materialList.clear()

                if (document.exists()) {
                    val acts = document.get("activities") as? List<Map<String, Any>> ?: emptyList()
                    acts.forEach { map ->
                        val name = map["name"] as? String ?: ""
                        if (name.isNotEmpty()) {
                            loggedWorks.add(LoggedWork(
                                name = name,
                                cloudinaryUrl = map["cloudinaryUrl"] as? String
                            ))
                        }
                    }

                    val man = document.get("manpower") as? Map<String, Long> ?: emptyMap()
                    man.forEach { (role, count) -> manpowerList.add(role to count.toInt()) }
                    manpowerPhotoUrl = document.getString("manpowerPhotoUrl")

                    val equip = document.get("equipment") as? List<Map<String, Any>> ?: emptyList()
                    equip.forEach { map ->
                        equipmentList.add(ResourceLog(
                            id = map["id"] as? String ?: UUID.randomUUID().toString(),
                            type = map["type"] as? String ?: "Equipment",
                            name = map["name"] as? String ?: "",
                            quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                            cost = map["cost"] as? String ?: "0",
                            cloudinaryUrl = map["cloudinaryUrl"] as? String
                        ))
                    }

                    val mats = document.get("materials") as? List<Map<String, Any>> ?: emptyList()
                    mats.forEach { map ->
                        materialList.add(ResourceLog(
                            id = map["id"] as? String ?: UUID.randomUUID().toString(),
                            type = map["type"] as? String ?: "Material",
                            name = map["name"] as? String ?: "",
                            quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                            cost = map["cost"] as? String ?: "0",
                            cloudinaryUrl = map["cloudinaryUrl"] as? String
                        ))
                    }
                }
                fetchActiveMilestoneWorks(projectId, projectType)
            }
            .addOnFailureListener { fetchActiveMilestoneWorks(projectId, projectType) }
    }

    // ------------------------------------------------------------------
    // Milestone works (unchanged)
    // ------------------------------------------------------------------
    @Suppress("UNCHECKED_CAST")
    private fun fetchActiveMilestoneWorks(projectId: String, projectType: String) {
        val rtdbPath = when {
            projectType.contains("Vertical", ignoreCase = true) -> "1/VERTICAL CONSTRUCTION"
            projectType.contains("Horizontal", ignoreCase = true) -> "2/HORIZONTAL CONSTRUCTION"
            projectType.contains("Demolition", ignoreCase = true) -> "3/DEMOLITION"
            else -> return
        }

        FirebaseDatabase.getInstance().getReference(rtdbPath)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orderedPhaseNames = mutableListOf<String>()
                    val phaseWorksMap = mutableMapOf<String, List<String>>()

                    for (phaseNode in snapshot.children) {
                        val phaseName = phaseNode.key ?: continue
                        orderedPhaseNames.add(phaseName)

                        val worksList = mutableListOf<String>()
                        val worksNode = phaseNode.child("Works")

                        if (projectType.contains("Demolition", ignoreCase = true)) {
                            for (categoryNode in worksNode.children) {
                                for (workNode in categoryNode.children) {
                                    workNode.getValue(String::class.java)?.let { worksList.add(it) }
                                }
                            }
                        } else {
                            worksNode.children.forEach { workItem ->
                                workItem.getValue(String::class.java)?.let { worksList.add(it) }
                            }
                        }
                        phaseWorksMap[phaseName] = worksList
                    }

                    if (projectType.contains("Vertical", ignoreCase = true) && orderedPhaseNames.size >= 9) {
                        val order = listOf(7, 1, 8, 0, 3, 6, 5, 4, 2)
                        val sortedNames = mutableListOf<String>()
                        try {
                            order.forEach { sortedNames.add(orderedPhaseNames[it]) }
                            orderedPhaseNames.clear()
                            orderedPhaseNames.addAll(sortedNames)
                        } catch (e: Exception) { }
                    }

                    totalMilestonesCount = orderedPhaseNames.size

                    FirebaseFirestore.getInstance()
                        .collection("projects")
                        .document(projectId)
                        .collection("milestones")
                        .get()
                        .addOnSuccessListener { firestoreDocs ->
                            var activePhase: String? = null
                            var completedWorksInActivePhase = emptyList<String>()
                            for (phaseName in orderedPhaseNames) {
                                val savedDoc = firestoreDocs.documents.find { it.id == phaseName }
                                val progress = savedDoc?.getDouble("progress") ?: 0.0
                                if (progress < 1.0) {
                                    activePhase = phaseName
                                    completedWorksInActivePhase = savedDoc?.get("completedWorks") as? List<String> ?: emptyList()
                                    break
                                }
                            }

                            if (activePhase != null) {
                                currentActiveMilestoneId = activePhase
                                val allPhaseWorks = phaseWorksMap[activePhase] ?: emptyList()
                                totalWorksForActiveMilestone = allPhaseWorks.size

                                val todayLoggedNames = loggedWorks.map { it.name }
                                availableWorksForModal = allPhaseWorks.filter {
                                    !completedWorksInActivePhase.contains(it) && !todayLoggedNames.contains(it)
                                }
                            } else {
                                availableWorksForModal = emptyList()
                                totalWorksForActiveMilestone = 0
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
    }

    // ------------------------------------------------------------------
    // Save to Firestore
    // ------------------------------------------------------------------
    fun saveDailyLog(projectId: String, onSuccess: () -> Unit) {
        isSaving = true
        saveToFirestore(projectId, onSuccess)
    }

    fun autoSaveDraft(projectId: String) {
        viewModelScope.launch { try { saveToFirestore(projectId, {}) } catch (e: Exception) { } }
    }

    private fun saveToFirestore(projectId: String, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        val manpowerMap = manpowerList.associate { it.first to it.second }
        val activitiesToSave = loggedWorks.map {
            mapOf(
                "name" to it.name,
                "cloudinaryUrl" to it.cloudinaryUrl
            )
        }
        val equipToSave = equipmentList.map {
            mapOf(
                "id" to it.id,
                "type" to it.type,
                "name" to it.name,
                "quantity" to it.quantity,
                "cost" to it.cost,
                "cloudinaryUrl" to it.cloudinaryUrl
            )
        }
        val matsToSave = materialList.map {
            mapOf(
                "id" to it.id,
                "type" to it.type,
                "name" to it.name,
                "quantity" to it.quantity,
                "cost" to it.cost,
                "cloudinaryUrl" to it.cloudinaryUrl
            )
        }

        val logData = hashMapOf(
            "activities" to activitiesToSave,
            "manpower" to manpowerMap,
            "manpowerPhotoUrl" to manpowerPhotoUrl,
            "equipment" to equipToSave,
            "materials" to matsToSave,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("projects").document(projectId).collection("daily_logs").document(todayDateString)
            .set(logData)
            .addOnSuccessListener { updateActiveMilestoneProgress(projectId, onSuccess) }
            .addOnFailureListener { isSaving = false }
    }

    // ------------------------------------------------------------------
    // Milestone progress (unchanged logic)
    // ------------------------------------------------------------------
    @Suppress("UNCHECKED_CAST")
    private fun updateActiveMilestoneProgress(projectId: String, onSuccess: () -> Unit) {
        val phaseId = currentActiveMilestoneId
        if (phaseId == null) { isSaving = false; onSuccess(); return }

        val db = FirebaseFirestore.getInstance()
        val milestoneRef = db.collection("projects").document(projectId).collection("milestones").document(phaseId)

        milestoneRef.get().addOnSuccessListener { doc ->
            val existingCompleted = if (doc.exists()) doc.get("completedWorks") as? List<String> ?: emptyList() else emptyList()
            val todayCompleted = loggedWorks.map { it.name }
            val mergedWorks = (existingCompleted + todayCompleted).distinct()

            val newProgress = if (totalWorksForActiveMilestone > 0) mergedWorks.size.toFloat() / totalWorksForActiveMilestone else 0f

            val updateData = hashMapOf(
                "completedWorks" to mergedWorks,
                "progress" to newProgress
            )

            milestoneRef.set(updateData, SetOptions.merge()).addOnSuccessListener {
                db.collection("projects").document(projectId).collection("milestones").get().addOnSuccessListener { msDocs ->
                    var totalProgress = 0f
                    for (m in msDocs) {
                        val p = if (m.id == phaseId) newProgress else (m.getDouble("progress") ?: 0.0).toFloat()
                        totalProgress += p
                    }
                    val expectedTotal = if (totalMilestonesCount > 0) totalMilestonesCount else 9
                    val overall = totalProgress / expectedTotal

                    db.collection("projects").document(projectId).update("overallProgress", overall)
                        .addOnSuccessListener { isSaving = false; onSuccess() }
                        .addOnFailureListener { isSaving = false; onSuccess() }
                }.addOnFailureListener { isSaving = false; onSuccess() }
            }.addOnFailureListener { isSaving = false; onSuccess() }
        }.addOnFailureListener { isSaving = false; onSuccess() }
    }
}