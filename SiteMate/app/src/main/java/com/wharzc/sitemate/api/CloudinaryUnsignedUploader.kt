package com.wharzc.sitemate.api   // change to a non‑navigation package

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.get

object CloudinaryUnsignedUploader {
    private const val CLOUD_NAME = "your_cloud_name"      // replace
    private const val UPLOAD_PRESET = "unsigned_sitemate" // replace
    private const val BASE_FOLDER = "site_mate_uploads"

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            // Always use applicationContext to avoid leaks
            MediaManager.init(context.applicationContext)
            isInitialized = true
        }
    }

    fun uploadFile(
        uri: Uri,
        projectId: String,
        subFolder: String = ""
    ): Flow<UploadResult> = callbackFlow {
        val fullFolder = "$BASE_FOLDER/projects/$projectId/${subFolder.trim('/')}".trimEnd('/')
        val requestId = MediaManager.get()
            .upload(uri)
            .unsigned(UPLOAD_PRESET)
            .option("folder", fullFolder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    trySend(UploadResult.Loading)
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val percent = (bytes * 100 / totalBytes).toInt()
                    trySend(UploadResult.Progress(percent))
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        trySend(UploadResult.Success(secureUrl))
                    } else {
                        trySend(UploadResult.Error("Missing secure_url"))
                    }
                    close()
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    trySend(UploadResult.Error(error.description))
                    close()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()

        awaitClose {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}

sealed class UploadResult {
    object Loading : UploadResult()
    data class Progress(val percent: Int) : UploadResult()
    data class Success(val secureUrl: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}