package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.localization.AppStrings

enum class NavDestination(
    val route: String,
    val icon: ImageVector,
    val getLabel: (AppStrings) -> String
) {
    SHIELD("shield", Icons.Default.Security, { it.shield }),
    VAULT("vault", Icons.Default.PhoneAndroid, { it.vault }),
    TRACKING("tracking", Icons.Default.GpsFixed, { it.tracking }),
    COMMUNITY("community", Icons.Default.Group, { it.community }),
    SETTINGS("settings", Icons.Default.Settings, { it.settings })
}
