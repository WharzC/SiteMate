package com.wharzc.sitemate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 1. Define Dark Mode Colors (Optional: tailored for low light)
private val DarkColorScheme = darkColorScheme(
    primary = SiteMateBlue,
    secondary = SiteMateOrange,
    tertiary = SiteMateGreen,
    background = TextBlack,
    surface = TextDarkGray,
    onPrimary = TextWhite,
    onSecondary = TextBlack,
    onBackground = TextWhite,
    onSurface = TextWhite
)

// 2. Define Light Mode Colors (Your main design)
private val LightColorScheme = lightColorScheme(
    primary = SiteMateBlue,
    secondary = SiteMateOrange,
    tertiary = SiteMateGreen,
    background = BackgroundLightGray,
    surface = BackgroundWhite,
    onPrimary = TextWhite,     // Text color on top of Blue buttons
    onSecondary = TextWhite,   // Text color on top of Orange accents
    onBackground = TextBlack,  // Main text color
    onSurface = TextBlack      // Text color on cards/surfaces
)

@Composable
fun SiteMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We set this to FALSE so your Navy/Orange colors always show,
    // instead of the user's phone wallpaper colors (Material You).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}