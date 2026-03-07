package com.wharzc.sitemate.screens.project

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.wharzc.sitemate.model.ProjectCreationViewModel
import com.wharzc.sitemate.screens.components.ProjectWizardHeader
import com.wharzc.sitemate.ui.theme.SiteMateBlue

@Composable
fun ProjectUploadScreen(
    viewModel: ProjectCreationViewModel, // NEW PARAMETER
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Update ViewModel when photos are picked
    val sitePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) {
        if (it.isNotEmpty()) viewModel.sitePhotos = (viewModel.sitePhotos + it).take(10)
    }
    val docPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) {
        if (it.isNotEmpty()) viewModel.documentPhotos = (viewModel.documentPhotos + it).take(10)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProjectWizardHeader(title = "Upload Assets", onBackClick = onBack)

        Text("Optional: Max 10 images.", fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray, modifier = Modifier.padding(bottom = 32.dp, top = 8.dp))

        // Read count/list from ViewModel
        UploadSection("Site Photos", Icons.Filled.AddPhotoAlternate, viewModel.sitePhotos.size, viewModel.sitePhotos) {
            sitePhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        Spacer(modifier = Modifier.height(24.dp))

        UploadSection("Documents", Icons.Filled.UploadFile, viewModel.documentPhotos.size, viewModel.documentPhotos) {
            docPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
        ) {
            Text("Proceed")
        }
    }

    if (showConfirmDialog) {
        val isEmpty = viewModel.sitePhotos.isEmpty() && viewModel.documentPhotos.isEmpty()
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(if (isEmpty) "No Assets" else "Confirm Uploads") },
            text = { Text(if (isEmpty) "Proceed without photos?" else "Finish setup with ${viewModel.sitePhotos.size} photos and ${viewModel.documentPhotos.size} documents?") },
            confirmButton = { TextButton(onClick = { showConfirmDialog = false; onProceed() }) { Text("Yes") } },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") } }
        )
    }
}
// (Keep UploadSection helper here)
@Composable
fun UploadSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, currentCount: Int, images: List<Uri>, onUploadClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = SiteMateBlue
                )
                Text(
                    text = "$currentCount/10",
                    fontSize = 12.sp,
                    color = androidx.compose.ui.graphics.Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onUploadClick, modifier = Modifier.fillMaxWidth()) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(
                modifier = Modifier.width(8.dp)
            ); Text("Select Images")
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(images) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}