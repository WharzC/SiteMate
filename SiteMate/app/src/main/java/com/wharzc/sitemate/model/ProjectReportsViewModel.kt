package com.wharzc.sitemate.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class for a report document
data class ReportDoc(
    val id: String,
    val title: String,
    val date: String,
    val type: String,
    val timestamp: Long
)

class ProjectReportsViewModel : ViewModel() {
    var reports = mutableStateListOf<ReportDoc>()
        private set

    var isLoading by mutableStateOf(true)
        private set
    var isGenerating by mutableStateOf(false)
        private set

    fun loadReports(projectId: String) {
        isLoading = true
        val db = FirebaseFirestore.getInstance()

        db.collection("projects").document(projectId)
            .collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                reports.clear()
                for (doc in snap) {
                    reports.add(
                        ReportDoc(
                            id = doc.id,
                            title = doc.getString("title") ?: "Untitled Report",
                            date = doc.getString("date") ?: "Unknown Date",
                            type = doc.getString("type") ?: "General",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    )
                }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    // UPDATED: Now saves directly to the phone's Download folder, bypassing Firebase Storage!
    fun generateReport(
        context: Context,
        projectId: String,
        type: String,
        dateString: String,
        onComplete: (Boolean) -> Unit
    ) {
        isGenerating = true

        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()

                val projectSnapshot = db.collection("projects").document(projectId).get().await()
                val projectName = projectSnapshot.getString("name") ?: "Unknown Project"

                val logSnapshot = db.collection("projects").document(projectId).collection("daily_logs").document(dateString).get().await()
                val activities = logSnapshot.get("activities") as? List<Map<String, Any>> ?: emptyList()
                val manpower = logSnapshot.get("manpower") as? Map<String, Long> ?: emptyMap()
                val materials = logSnapshot.get("materials") as? List<Map<String, Any>> ?: emptyList()
                val equipment = logSnapshot.get("equipment") as? List<Map<String, Any>> ?: emptyList()

                val pdfDocument = PdfDocument()
                var pageNumber = 1
                var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                var page = pdfDocument.startPage(pageInfo)
                var canvas: Canvas = page.canvas

                val titlePaint = Paint().apply { textSize = 20f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = Color.BLACK }
                val headerPaint = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = Color.DKGRAY }
                val textPaint = Paint().apply { textSize = 12f; color = Color.BLACK }
                val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
                val combinedItems = mutableListOf<String>()

                var currentY = 50f
                val startX = 50f

                fun checkPageBreak() {
                    if (currentY > 780f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 50f
                    }
                }

                canvas.drawText("CONSTRUCTION PROGRESS REPORT", startX, currentY, titlePaint)
                currentY += 40f
                canvas.drawText("Project Name: $projectName", startX, currentY, headerPaint)
                currentY += 20f

                val displayDate = try {
                    val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(parsedDate!!)
                } catch (e: Exception) { dateString }

                canvas.drawText("Date: $displayDate", startX, currentY, headerPaint)
                currentY += 30f
                canvas.drawLine(startX, currentY, 545f, currentY, linePaint)
                currentY += 30f

                canvas.drawText("Progress Made:", startX, currentY, headerPaint)
                currentY += 20f
                if (activities.isEmpty()) {
                    canvas.drawText("No activities logged for this date.", startX + 15f, currentY, textPaint)
                    currentY += 20f
                } else {
                    activities.forEach {
                        val name = it["name"] as? String ?: ""
                        canvas.drawText("• $name", startX + 15f, currentY, textPaint)
                        currentY += 20f
                        checkPageBreak()
                    }
                }
                currentY += 10f
                canvas.drawLine(startX, currentY, 545f, currentY, linePaint)
                currentY += 30f

                checkPageBreak()
                canvas.drawText("Manpower:", startX, currentY, headerPaint)
                currentY += 20f
                if (manpower.isEmpty()) {
                    canvas.drawText("No manpower logged for this date.", startX + 15f, currentY, textPaint)
                    currentY += 20f
                } else {
                    manpower.forEach { (role, count) ->
                        canvas.drawText("• $role: $count Heads", startX + 15f, currentY, textPaint)
                        currentY += 20f
                        checkPageBreak()
                    }
                }
                currentY += 10f
                canvas.drawLine(startX, currentY, 545f, currentY, linePaint)
                currentY += 30f

                // -- Materials & Equipment --
                checkPageBreak()
                canvas.drawText("Materials & Equipment:", startX, currentY, headerPaint)
                currentY += 20f

                // Combine both lists into a single formatted list

                equipment.forEach { eq ->
                    val name = eq["name"] as? String ?: ""
                    val qty = eq["quantity"]?.toString() ?: "0"
                    val cost = eq["cost"]?.toString() ?: "0"
                    combinedItems.add("• [Equipment] $name: $qty units (₱$cost)")
                }

                materials.forEach { mat ->
                    val itemType = mat["type"] as? String ?: "Material"
                    val name = mat["name"] as? String ?: ""
                    val qty = mat["quantity"]?.toString() ?: "0"
                    val cost = mat["cost"]?.toString() ?: "0"
                    combinedItems.add("• [$itemType] $name: $qty units (₱$cost)")
                }

                if (combinedItems.isEmpty()) {
                    canvas.drawText("No materials or equipment logged.", startX + 15f, currentY, textPaint)
                    currentY += 20f
                } else {
                    combinedItems.forEach { line ->
                        canvas.drawText(line, startX + 15f, currentY, textPaint)
                        currentY += 20f
                        checkPageBreak()
                    }
                }

                pdfDocument.finishPage(page)

                // 1. SAVE DIRECTLY TO DEVICE DOWNLOADS FOLDER
                val fileName = "DailyReport_${projectId}_${dateString}.pdf"
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val file = File(downloadsDir, fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()

                // 2. SAVE RECORD TO FIRESTORE (Without fileUrl)
                val generatedTitle = "Daily Accomplishment - $displayDate"
                val timestamp = System.currentTimeMillis()

                val reportData = hashMapOf(
                    "title" to generatedTitle,
                    "type" to type,
                    "date" to displayDate,
                    "timestamp" to timestamp
                )

                db.collection("projects").document(projectId).collection("reports").document(dateString).set(reportData).await()

                // 3. UPDATE UI
                reports.removeAll { it.id == dateString }
                reports.add(0, ReportDoc(id = dateString, title = generatedTitle, date = displayDate, type = type, timestamp = timestamp))
                reports.sortByDescending { it.timestamp }

                isGenerating = false
                onComplete(true)

            } catch (e: Exception) {
                println("PDF Generation Error: ${e.message}")
                isGenerating = false
                onComplete(false)
            }
        }
    }

    // Add this new method inside ProjectReportsViewModel
    fun generateOverallProgressReport(
        context: Context,
        projectId: String,
        onComplete: (Boolean) -> Unit
    ) {
        isGenerating = true
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                // Fetch project details
                val projectDoc = db.collection("projects").document(projectId).get().await()
                val projectName = projectDoc.getString("name") ?: "Unknown Project"
                val overallProgress = projectDoc.getDouble("overallProgress") ?: 0.0
                val startDate = projectDoc.getString("startDate") ?: "Not Set"
                val endDate = projectDoc.getString("endDate") ?: "Not Set"

                // Fetch milestones
                val milestonesSnapshot = db.collection("projects")
                    .document(projectId)
                    .collection("milestones")
                    .get()
                    .await()
                val milestones = milestonesSnapshot.documents.mapNotNull { doc ->
                    val title = doc.id
                    val progress = doc.getDouble("progress") ?: 0.0
                    val completedWorks = (doc.get("completedWorks") as? List<String>) ?: emptyList()
                    val totalWorks = (doc.get("totalWorks") as? Long)?.toInt() ?: 0 // you may need to store totalWorks per milestone during project creation; if not, we can skip or compute differently
                    MilestoneReportItem(
                        title = title,
                        progress = progress.toFloat(),
                        completedCount = completedWorks.size,
                        totalCount = totalWorks
                    )
                }

                val pdfDocument = PdfDocument()
                var pageNumber = 1
                var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                var page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                val titlePaint = Paint().apply { textSize = 20f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = Color.BLACK }
                val headerPaint = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = Color.DKGRAY }
                val textPaint = Paint().apply { textSize = 12f; color = Color.BLACK }
                val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

                var y = 50f
                val startX = 50f

                fun checkPageBreak() {
                    if (y > 780f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        y = 50f
                    }
                }

                canvas.drawText("OVERALL PROGRESS REPORT", startX, y, titlePaint)
                y += 40f
                canvas.drawText("Project: $projectName", startX, y, headerPaint)
                y += 20f
                canvas.drawText("Start Date: $startDate", startX, y, textPaint)
                y += 20f
                canvas.drawText("Target Deadline: $endDate", startX, y, textPaint)
                y += 30f
                canvas.drawLine(startX, y, 545f, y, linePaint)
                y += 30f

                // Overall progress circle representation (optional text)
                val overallPercent = (overallProgress * 100).toInt()
                canvas.drawText("Overall Completion: $overallPercent%", startX, y, headerPaint)
                y += 30f
                canvas.drawLine(startX, y, 545f, y, linePaint)
                y += 30f

                canvas.drawText("Milestone Breakdown:", startX, y, headerPaint)
                y += 25f

                if (milestones.isEmpty()) {
                    canvas.drawText("No milestones found.", startX + 15f, y, textPaint)
                    y += 20f
                } else {
                    for (m in milestones) {
                        checkPageBreak()
                        val status = when {
                            m.progress == 1f -> "Completed"
                            m.progress > 0f -> "In Progress"
                            else -> "Pending"
                        }
                        canvas.drawText("• ${m.title}: ${(m.progress * 100).toInt()}% ($status)", startX + 10f, y, textPaint)
                        y += 20f
                        // Optional: show completed/total works if totalWorks > 0
                        if (m.totalCount > 0) {
                            canvas.drawText("   ${m.completedCount}/${m.totalCount} works completed", startX + 20f, y, textPaint)
                            y += 20f
                        }
                        checkPageBreak()
                    }
                }

                pdfDocument.finishPage(page)

                // Save PDF to Downloads
                val timestamp = System.currentTimeMillis()
                val fileName = "OverallProgress_${projectId}_$timestamp.pdf"
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                pdfDocument.close()

                // Save record to Firestore
                val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val displayDate = dateFormatter.format(Date())
                val reportData = hashMapOf(
                    "title" to "Overall Progress Report",
                    "type" to "Overall Progress",
                    "date" to displayDate,
                    "timestamp" to timestamp
                )
                db.collection("projects").document(projectId)
                    .collection("reports")
                    .document("overall_$timestamp")
                    .set(reportData)
                    .await()

                // Update UI list
                reports.add(0, ReportDoc(
                    id = "overall_$timestamp",
                    title = "Overall Progress Report",
                    date = displayDate,
                    type = "Overall Progress",
                    timestamp = timestamp
                ))
                reports.sortByDescending { it.timestamp }

                isGenerating = false
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                isGenerating = false
                onComplete(false)
            }
        }
    }

    // Add this method inside ProjectReportsViewModel
    fun deleteReport(projectId: String, reportDoc: ReportDoc, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("projects")
                    .document(projectId)
                    .collection("reports")
                    .document(reportDoc.id)
                    .delete()
                    .await()

                // Remove from local list
                reports.removeAll { it.id == reportDoc.id }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally handle failure
            }
        }
    }

    // Helper data class
    data class MilestoneReportItem(
        val title: String,
        val progress: Float,
        val completedCount: Int,
        val totalCount: Int
    )
}