package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRedAlert

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    strings: AppStrings,
    onNavigateToIntruderLogs: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val emergencyPhone by viewModel.emergencyPhone.collectAsState()
    val securityPin by viewModel.antiTheftEngine.securityPin.collectAsState()
    val strobeEnabled by viewModel.antiTheftEngine.strobeEnabled.collectAsState()

    var showPinChangeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // User Account Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_profile_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberGreen.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Google Account Linked",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Security & Credentials
        SettingsSectionHeader(title = "SECURITY CREDENTIALS")

        SettingsCard(
            title = "Security PIN",
            subtitle = "Current PIN: • • • • (Tap to change)",
            icon = Icons.Default.Pin,
            onClick = { showPinChangeDialog = true },
            testTag = "change_pin_item"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard(
            title = strings.intruderLogs,
            subtitle = "Inspect wrong PIN photos and unauthorized attempts",
            icon = Icons.Default.PhotoCamera,
            onClick = onNavigateToIntruderLogs,
            testTag = "view_intruder_logs_item"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Screen strobe toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = strings.strobeLight, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(text = "Flashes red/blue screen when siren triggers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = strobeEnabled,
                    onCheckedChange = { viewModel.antiTheftEngine.strobeEnabled.value = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Preferences
        SettingsSectionHeader(title = "PREFERENCES & LOCALIZATION")

        SettingsCard(
            title = strings.language,
            subtitle = "${currentLang.displayName} (${currentLang.nativeName})",
            icon = Icons.Default.Language,
            onClick = { showLanguageDialog = true },
            testTag = "change_language_item"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dark Theme Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = strings.darkMode, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(text = "High-contrast dark mode for surveillance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = isDarkMode == true,
                    onCheckedChange = { isDark ->
                        viewModel.setDarkMode(isDark)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Support & Contacts
        SettingsSectionHeader(title = "EMERGENCY CONTACT & SUPPORT")

        SettingsCard(
            title = strings.emergencyContact,
            subtitle = emergencyPhone,
            icon = Icons.Default.ContactPhone,
            onClick = { showContactDialog = true },
            testTag = "emergency_contact_item"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // App Info / Policy Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Thief Hunter v1.0 • Play Policy Compliant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Equipped with Room offline database, sensor anti-theft triggers, and public stolen IMEI verification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // Change PIN Dialog
    if (showPinChangeDialog) {
        ChangePinDialog(
            currentPin = securityPin,
            onDismiss = { showPinChangeDialog = false },
            onPinChanged = { newPin ->
                viewModel.updateSecurityPin(newPin)
                showPinChangeDialog = false
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            current = currentLang,
            onDismiss = { showLanguageDialog = false },
            onSelect = { lang ->
                viewModel.setLanguage(lang)
                showLanguageDialog = false
            }
        )
    }

    // Contact Dialog
    if (showContactDialog) {
        EmergencyContactDialog(
            phone = emergencyPhone,
            onDismiss = { showContactDialog = false },
            onSave = { newPhone ->
                viewModel.emergencyPhone.value = newPhone
                showContactDialog = false
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ChangePinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onPinChanged: (String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("change_pin_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Change Security PIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { oldPin = it },
                    label = { Text("Current 4-Digit PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("New 4-Digit PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    label = { Text("Confirm New PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (error != null) {
                    Text(text = error!!, color = CyberRedAlert, fontSize = 12.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (oldPin != currentPin) {
                                error = "Current PIN is incorrect"
                            } else if (newPin.length != 4) {
                                error = "New PIN must be exactly 4 digits"
                            } else if (newPin != confirmPin) {
                                error = "New PINs do not match"
                            } else {
                                onPinChanged(newPin)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363F))
                    ) {
                        Text("Update PIN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageDialog(
    current: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("language_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Select Language / زبان منتخب کریں", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                AppLanguage.values().forEach { lang ->
                    Card(
                        onClick = { onSelect(lang) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (lang == current) CyberCyan.copy(alpha = 0.2f) else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = lang.displayName, fontWeight = FontWeight.Bold)
                                Text(text = lang.nativeName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (lang == current) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CyberCyan)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
fun EmergencyContactDialog(
    phone: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var contactPhone by remember { mutableStateOf(phone) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Emergency Contact Number", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "This number receives recovery alert coordinates when SOS is triggered.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Emergency Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(contactPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363F))
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
