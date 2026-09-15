package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.localization.AppStrings
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRedAlert

@Composable
fun TheftReportWizardDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSubmit: (
        model: String,
        brand: String,
        imei: String,
        theftDate: String,
        location: String,
        fir: String,
        policeStation: String,
        officer: String,
        email: String,
        phone: String,
        reward: String,
        description: String,
        photoUri: String?
    ) -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Step 1: Device info
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var imei by remember { mutableStateOf("") }

    // Step 2: Incident details
    var theftDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Step 3: Police FIR Info
    var firNumber by remember { mutableStateOf("") }
    var policeStation by remember { mutableStateOf("") }
    var officerName by remember { mutableStateOf("") }

    // Step 4: Contact & Reward
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var rewardAmount by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var isConfirmed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("theft_report_wizard"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isConfirmed) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(CyberGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "THEFT REPORT BROADCASTED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CyberGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Incident has been logged into the public stolen database and confirmation dispatched to $contactEmail. FIR $firNumber is tied to IMEI $imei.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363F)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Header with Step indicator
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "File Theft Report",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyberCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "STEP $step / 4",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = { step / 4f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = CyberCyan
                            )
                        }
                    }

                    when (step) {
                        1 -> {
                            // Step 1: Device Details
                            item {
                                Text("1. Device Identification", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            item {
                                OutlinedTextField(
                                    value = brand,
                                    onValueChange = { brand = it },
                                    label = { Text("Brand (e.g. Samsung)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = model,
                                    onValueChange = { model = it },
                                    label = { Text("Model (e.g. Galaxy S24 Ultra)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = imei,
                                    onValueChange = { imei = it },
                                    label = { Text("Stolen IMEI Number (15 Digits)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                        2 -> {
                            // Step 2: Incident Details
                            item {
                                Text("2. Theft Incident Circumstances", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            item {
                                OutlinedTextField(
                                    value = theftDate,
                                    onValueChange = { theftDate = it },
                                    label = { Text("Incident Date & Time (e.g. 2026-03-12 14:30)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    label = { Text("Last Known Location / Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Description & Distinguishing Features") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            }
                        }
                        3 -> {
                            // Step 3: Police Station & FIR
                            item {
                                Text("3. Law Enforcement FIR Details", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            item {
                                OutlinedTextField(
                                    value = firNumber,
                                    onValueChange = { firNumber = it },
                                    label = { Text(strings.policeFirNumber) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = policeStation,
                                    onValueChange = { policeStation = it },
                                    label = { Text(strings.policeStation) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = officerName,
                                    onValueChange = { officerName = it },
                                    label = { Text("Investigating Officer Name (Optional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                        4 -> {
                            // Step 4: Contact & Reward
                            item {
                                Text("4. Owner Contact & Evidence", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            item {
                                OutlinedTextField(
                                    value = contactEmail,
                                    onValueChange = { contactEmail = it },
                                    label = { Text("Notification Email (For confirmation)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = contactPhone,
                                    onValueChange = { contactPhone = it },
                                    label = { Text("Alternative Phone Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = rewardAmount,
                                    onValueChange = { rewardAmount = it },
                                    label = { Text("Recovery Reward Offer (e.g. $100)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    if (error != null) {
                        item {
                            Text(text = error!!, color = CyberRedAlert, fontSize = 12.sp)
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (step > 1) {
                                OutlinedButton(onClick = { step--; error = null }) {
                                    Text("Back")
                                }
                            } else {
                                TextButton(onClick = onDismiss) {
                                    Text("Cancel")
                                }
                            }

                            Button(
                                onClick = {
                                    when (step) {
                                        1 -> {
                                            if (brand.isBlank() || model.isBlank() || imei.isBlank()) {
                                                error = "Please fill in Brand, Model, and IMEI"
                                            } else {
                                                error = null
                                                step = 2
                                            }
                                        }
                                        2 -> {
                                            if (theftDate.isBlank() || location.isBlank()) {
                                                error = "Please specify date and location"
                                            } else {
                                                error = null
                                                step = 3
                                            }
                                        }
                                        3 -> {
                                            if (firNumber.isBlank() || policeStation.isBlank()) {
                                                error = "Please specify FIR number and Police Station"
                                            } else {
                                                error = null
                                                step = 4
                                            }
                                        }
                                        4 -> {
                                            if (contactEmail.isBlank() || contactPhone.isBlank()) {
                                                error = "Please provide your email and contact phone"
                                            } else {
                                                onSubmit(
                                                    model, brand, imei, theftDate, location,
                                                    firNumber, policeStation, officerName,
                                                    contactEmail, contactPhone, rewardAmount,
                                                    description, null
                                                )
                                                isConfirmed = true
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyan,
                                    contentColor = Color(0xFF00363F)
                                )
                            ) {
                                Text(if (step == 4) "Submit & Broadcast" else "Next", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
