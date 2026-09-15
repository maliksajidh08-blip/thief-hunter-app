package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppStrings
import com.example.ui.MainViewModel
import com.example.ui.components.DisarmDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRedAlert

@Composable
fun ShieldScreen(
    viewModel: MainViewModel,
    strings: AppStrings,
    onNavigateToIntruderLogs: () -> Unit
) {
    val isArmed by viewModel.antiTheftEngine.isArmed.collectAsState()
    val motionEnabled by viewModel.antiTheftEngine.motionProtectionEnabled.collectAsState()
    val pocketEnabled by viewModel.antiTheftEngine.pocketProtectionEnabled.collectAsState()
    val chargerEnabled by viewModel.antiTheftEngine.chargerAlarmEnabled.collectAsState()
    val intruderEnabled by viewModel.antiTheftEngine.intruderSelfieEnabled.collectAsState()
    val voiceEnabled by viewModel.antiTheftEngine.voiceAlarmEnabled.collectAsState()
    val intruderLogs by viewModel.intruderLogs.collectAsState()

    var showDisarmDialog by remember { mutableStateOf(false) }

    val statusColor by animateColorAsState(
        targetValue = if (isArmed) CyberGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "statusColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        // Hero Visual Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.security_hero_banner_1789469812069),
                contentDescription = "Security Command Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Top Status Bar Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArmed) strings.armed else strings.disarmed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                if (intruderLogs.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = onNavigateToIntruderLogs,
                        modifier = Modifier.testTag("intruder_logs_badge"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CyberRedAlert.copy(alpha = 0.2f),
                            contentColor = CyberRedAlert
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${intruderLogs.size} Alerts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Main Arm / Disarm Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (isArmed) {
                        showDisarmDialog = true
                    } else {
                        viewModel.armShield()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("arm_disarm_main_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isArmed) CyberRedAlert else CyberCyan,
                    contentColor = if (isArmed) Color.White else Color(0xFF00363F)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = if (isArmed) Icons.Default.ShieldMoon else Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isArmed) strings.disarmShield else strings.armShield,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Emergency SOS Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("panic_sos_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CyberRedAlert.copy(alpha = 0.12f)
            ),
            border = BorderStroke(1.dp, CyberRedAlert.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = CyberRedAlert,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.emergencyPanic,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedAlert
                        )
                    }
                    Text(
                        text = strings.emergencyPanicDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Button(
                    onClick = { viewModel.triggerEmergencyPanic() },
                    modifier = Modifier.testTag("sos_siren_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberRedAlert,
                        contentColor = Color.White
                    )
                ) {
                    Text("PANIC", fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Shield Modules Section
        Text(
            text = "ACTIVE DEFENSE MODULES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            letterSpacing = 1.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Motion Protection
            ShieldModuleCard(
                title = strings.motionDetection,
                description = strings.motionDesc,
                icon = Icons.Default.Vibration,
                enabled = motionEnabled,
                onToggle = { viewModel.antiTheftEngine.motionProtectionEnabled.value = it },
                testTag = "toggle_motion_sensor"
            )

            // 2. Pocket Protection
            ShieldModuleCard(
                title = strings.pocketProtection,
                description = strings.pocketDesc,
                icon = Icons.Default.PanTool,
                enabled = pocketEnabled,
                onToggle = { viewModel.antiTheftEngine.pocketProtectionEnabled.value = it },
                testTag = "toggle_pocket_sensor"
            )

            // 3. Charger Unplug Alert
            ShieldModuleCard(
                title = strings.chargerAlarm,
                description = strings.chargerDesc,
                icon = Icons.Default.Power,
                enabled = chargerEnabled,
                onToggle = { viewModel.antiTheftEngine.chargerAlarmEnabled.value = it },
                testTag = "toggle_charger_sensor"
            )

            // 4. Wrong PIN Intruder Trap
            ShieldModuleCard(
                title = strings.wrongPinTrap,
                description = strings.wrongPinDesc,
                icon = Icons.Default.CameraAlt,
                enabled = intruderEnabled,
                onToggle = { viewModel.antiTheftEngine.intruderSelfieEnabled.value = it },
                testTag = "toggle_intruder_trap"
            )

            // 5. Spoken Voice Warnings
            ShieldModuleCard(
                title = strings.voiceWarning,
                description = "Speaks synthesized alerts through loudspeaker",
                icon = Icons.Default.RecordVoiceOver,
                enabled = voiceEnabled,
                onToggle = { viewModel.antiTheftEngine.voiceAlarmEnabled.value = it },
                testTag = "toggle_voice_alarm"
            )
        }
    }

    if (showDisarmDialog) {
        DisarmDialog(
            strings = strings,
            onDismiss = { showDisarmDialog = false },
            onVerifyPin = { pin ->
                val success = viewModel.disarmShieldWithPin(pin)
                success
            }
        )
    }
}

@Composable
fun ShieldModuleCard(
    title: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (enabled) CyberCyan.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
