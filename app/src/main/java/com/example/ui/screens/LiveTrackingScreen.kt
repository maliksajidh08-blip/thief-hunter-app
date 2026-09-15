package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRedAlert
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiveTrackingScreen(
    viewModel: MainViewModel,
    strings: AppStrings
) {
    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsState()

    // Radar scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Device Status Header Cards (Battery & Network)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Battery Status
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("battery_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.batteryStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                            contentDescription = null,
                            tint = if (telemetry.batteryPercent > 20) CyberGreen else CyberAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${telemetry.batteryPercent}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (telemetry.batteryPercent > 20) CyberGreen else CyberAmber
                    )
                    Text(
                        text = if (telemetry.isCharging) "Fast Charging" else "Discharging",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Online / Connection Status
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("network_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.onlineStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (telemetry.isOnline) CyberGreen else CyberRedAlert)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (telemetry.isOnline) "ONLINE" else "OFFLINE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (telemetry.isOnline) CyberGreen else CyberRedAlert
                    )
                    Text(
                        text = telemetry.connectionType,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive GPS Radar Visualization Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gps_radar_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.liveCoordinates,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Accuracy: ±%.1fm • Updated: %s".format(telemetry.accuracyMeters, telemetry.lastUpdated),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyberCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "GPS LOCK",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Radar Map Canvas
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF070B14)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val maxRadius = size.width / 2

                        // Concentric grid rings
                        val ringColors = Color(0xFF00E5FF).copy(alpha = 0.15f)
                        drawCircle(color = ringColors, radius = maxRadius * 0.25f, style = Stroke(1.5f))
                        drawCircle(color = ringColors, radius = maxRadius * 0.5f, style = Stroke(1.5f))
                        drawCircle(color = ringColors, radius = maxRadius * 0.75f, style = Stroke(1.5f))
                        drawCircle(color = ringColors, radius = maxRadius * 0.98f, style = Stroke(2f))

                        // Crosshairs
                        drawLine(
                            color = ringColors,
                            start = Offset(center.x, 0f),
                            end = Offset(center.x, size.height),
                            strokeWidth = 1.2f
                        )
                        drawLine(
                            color = ringColors,
                            start = Offset(0f, center.y),
                            end = Offset(size.width, center.y),
                            strokeWidth = 1.2f
                        )

                        // Radar sweep line
                        val rad = Math.toRadians(radarAngle.toDouble())
                        val sweepEnd = Offset(
                            (center.x + maxRadius * cos(rad)).toFloat(),
                            (center.y + maxRadius * sin(rad)).toFloat()
                        )
                        drawLine(
                            color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                            start = center,
                            end = sweepEnd,
                            strokeWidth = 2.5f
                        )

                        // Pulsing target phone dot
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                            radius = 16f,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFF00E5FF),
                            radius = 6f,
                            center = center
                        )
                    }

                    // Direction indicators (N, S, E, W)
                    Text(
                        text = "N",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Coordinate Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LATITUDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "%.5f".format(telemetry.latitude),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LONGITUDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "%.5f".format(telemetry.longitude),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Launch Google Maps Button
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:${telemetry.latitude},${telemetry.longitude}?q=${telemetry.latitude},${telemetry.longitude}(Thief+Hunter+Target)")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {
                            // Fallback to web browser maps
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${telemetry.latitude},${telemetry.longitude}"))
                            context.startActivity(webIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("open_maps_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color(0xFF00363F)
                    )
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.openGoogleMaps, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
