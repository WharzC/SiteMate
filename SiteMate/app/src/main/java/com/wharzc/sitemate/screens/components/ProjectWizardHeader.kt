package com.wharzc.sitemate.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wharzc.sitemate.ui.theme.SiteMateBlue

@Composable
fun ProjectWizardHeader(
    title: String,
    onBackClick: () -> Unit
) {
    var showBackWarning by remember { mutableStateOf(false) }

    // Use a BOX to stack elements:
    // 1. Back Button aligned to Start (Left)
    // 2. Title aligned to Center
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp) // Space below the header
    ) {
        // --- 1. OBLONG BACK BUTTON (Left) ---
        IconButton(
            onClick = { showBackWarning = true },
            modifier = Modifier
                .align(Alignment.CenterStart) // Stick to the left
                .clip(CircleShape)
                .background(SiteMateBlue.copy(alpha = 0.1f))
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = SiteMateBlue
            )
        }

        // --- 2. OBLONG PAGE TITLE (Center) ---
        Box(
            modifier = Modifier
                .align(Alignment.Center) // Stick to the center
                .clip(RoundedCornerShape(50))
                .background(SiteMateBlue.copy(alpha = 0.05f))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp, // Slightly smaller to fit better
                fontWeight = FontWeight.Bold,
                color = SiteMateBlue
            )
        }
    }

    // --- WARNING DIALOG ---
    if (showBackWarning) {
        AlertDialog(
            onDismissRequest = { showBackWarning = false },
            title = { Text("Go Back?") },
            text = { Text("Are you sure? Unsaved progress will be lost.") },
            confirmButton = {
                TextButton(onClick = { showBackWarning = false; onBackClick() }) {
                    Text("Yes, Go Back", color = SiteMateBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackWarning = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}