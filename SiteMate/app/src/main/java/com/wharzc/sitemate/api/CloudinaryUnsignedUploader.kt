package com.wharzc.sitemate.api

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object CloudinaryUnsignedUploader {
    private const val CLOUD_NAME = "ds3xbvqnp"// replace with your cloud name
    private const val UPLOAD_PRESET = "sitemate_2026_assets"     // the preset you created
    private const val BASE_FOLDER = "sitemate_uploads"       // root folder

    fun init(context: android.content.Context) {
        if (!MediaManager.isInitialized()) {
            MediaManager.init(context)
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
                    val publicId = resultData["public_id"] as String
                    val secureUrl = resultData["secure_url"] as String
                    trySend(UploadResult.Success(publicId, secureUrl))
                    close()
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    trySend(UploadResult.Error(error.exception?.message ?: error.description))
                    close()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()

        awaitClose {
            MediaManager.get().cancelUpload(requestId)
        }
    }
}

sealed class UploadResult {
    object Loading : UploadResult()
    data class Progress(val percent: Int) : UploadResult()
    data class Success(val publicId: String, val secureUrl: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}