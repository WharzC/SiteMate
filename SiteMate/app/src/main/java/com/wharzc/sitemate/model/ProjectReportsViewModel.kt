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
}