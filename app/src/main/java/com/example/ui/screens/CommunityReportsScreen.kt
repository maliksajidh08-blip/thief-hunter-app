package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TheftReportEntity
import com.example.localization.AppStrings
import com.example.ui.MainViewModel
import com.example.ui.components.TheftReportWizardDialog
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRedAlert

@Composable
fun CommunityReportsScreen(
    viewModel: MainViewModel,
    strings: AppStrings
) {
    val context = LocalContext.current
    val reports by viewModel.filteredReports.collectAsState()
    val totalCount by viewModel.totalDevicesCount.collectAsState()
    val searchQuery by viewModel.searchImeiQuery.collectAsState()

    var showWizard by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showWizard = true },
                containerColor = CyberRedAlert,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.ReportProblem, contentDescription = null) },
                text = { Text("Report Theft", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("report_theft_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Community Network Stats Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("community_stats_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatMetric(label = "PROTECTED", value = "${totalCount + 142}", color = CyberCyan)
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    StatMetric(label = "RECOVERED", value = "89.4%", color = CyberGreen)
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    StatMetric(label = "CASES", value = "${reports.size}", color = CyberAmber)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar for IMEI / Model verification
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchImeiQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_imei_textfield"),
                placeholder = { Text(strings.searchImeiHint, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchImeiQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PUBLIC STOLEN REGISTRY FEED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No Stolen Reports Found for '$searchQuery'" else "All devices secure",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "This IMEI is clean in Thief Hunter registry" else "No active public theft reports",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(reports, key = { it.id }) { report ->
                        TheftReportCard(
                            report = report,
                            strings = strings,
                            onShare = {
                                val shareText = "🚨 STOLEN PHONE ALERT! 🚨\n" +
                                        "Model: ${report.brand} ${report.deviceModel}\n" +
                                        "IMEI: ${report.imei}\n" +
                                        "FIR Number: ${report.firNumber} (${report.policeStation})\n" +
                                        "Last Seen: ${report.locationName}\n" +
                                        "Reward: ${report.rewardAmount}\n" +
                                        "Contact: ${report.contactPhone}\n" +
                                        "Logged in Thief Hunter Community Security Network."

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Stolen Device Alert")
                                context.startActivity(shareIntent)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showWizard) {
        TheftReportWizardDialog(
            strings = strings,
            onDismiss = { showWizard = false },
            onSubmit = { model, brand, imei, theftDate, location, fir, policeStation, officer, email, phone, reward, description, photoUri ->
                viewModel.submitTheftReport(
                    model, brand, imei, theftDate, location,
                    fir, policeStation, officer, email, phone,
                    reward, description, photoUri
                )
            }
        )
    }
}

@Composable
fun StatMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TheftReportCard(
    report: TheftReportEntity,
    strings: AppStrings,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theft_report_card_${report.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberRedAlert.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CyberRedAlert,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${report.brand} ${report.deviceModel}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Reported: ${report.theftDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (report.status == "RECOVERED") CyberGreen.copy(alpha = 0.18f) else CyberRedAlert.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = if (report.status == "RECOVERED") "RECOVERED" else "STOLEN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (report.status == "RECOVERED") CyberGreen else CyberRedAlert,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "IMEI: ${report.imei}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "FIR: ${report.firNumber} • ${report.policeStation}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Location: ${report.locationName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (report.rewardAmount.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reward Offered: ${report.rewardAmount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contact: ${report.contactPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("share_alert_button_${report.id}"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Alert", fontSize = 12.sp)
                }
            }
        }
    }
}
