package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DeviceEntity
import com.example.localization.AppStrings
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRedAlert

@Composable
fun DeviceVaultScreen(
    viewModel: MainViewModel,
    strings: AppStrings
) {
    val devices by viewModel.registeredDevices.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (devices.size < 10) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = CyberCyan,
                    contentColor = Color(0xFF00363F),
                    modifier = Modifier
                        .padding(bottom = 80.dp)
                        .testTag("add_device_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Mobile")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Vault Header & Capacity Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_capacity_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.registeredMobiles,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyberCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${devices.size} / 10",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { devices.size / 10f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (devices.size >= 10) CyberAmber else CyberCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = strings.maxDevicesNotice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No devices registered yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap + to securely save your phone's IMEI & proof",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
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
                    items(devices, key = { it.id }) { device ->
                        DeviceCard(
                            device = device,
                            strings = strings,
                            onDelete = { viewModel.deleteDevice(device) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDeviceDialog(
            strings = strings,
            onDismiss = { showAddDialog = false },
            onSave = { brand, model, imei1, imei2, serial, date, owner, notes ->
                viewModel.registerDevice(brand, model, imei1, imei2, serial, date, owner, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DeviceCard(
    device: DeviceEntity,
    strings: AppStrings,
    onDelete: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${device.brand} ${device.model}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (device.isThisDevice) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyberGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "THIS PHONE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Owner: ${device.ownerName.ifEmpty { "Primary Owner" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (device.status) {
                        "STOLEN" -> CyberRedAlert.copy(alpha = 0.18f)
                        "REPORTED" -> CyberAmber.copy(alpha = 0.18f)
                        else -> CyberGreen.copy(alpha = 0.18f)
                    }
                ) {
                    Text(
                        text = when (device.status) {
                            "STOLEN" -> strings.statusStolen
                            "REPORTED" -> strings.statusReported
                            else -> strings.statusSafe
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (device.status) {
                            "STOLEN" -> CyberRedAlert
                            "REPORTED" -> CyberAmber
                            else -> CyberGreen
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // IMEI box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IMEI 1: ${device.imei1}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(
                        onClick = { showDetails = !showDetails },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showDetails) "Less" else "Details",
                            fontSize = 12.sp,
                            color = CyberCyan
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    if (device.imei2.isNotBlank()) {
                        Text(
                            text = "IMEI 2: ${device.imei2}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (device.serialNumber.isNotBlank()) {
                        Text(
                            text = "Serial: ${device.serialNumber}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (device.purchaseDate.isNotBlank()) {
                        Text(
                            text = "Purchase Date: ${device.purchaseDate}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (device.notes.isNotBlank()) {
                        Text(
                            text = "Notes: ${device.notes}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = CyberRedAlert)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddDeviceDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (brand: String, model: String, imei1: String, imei2: String, serial: String, date: String, owner: String, notes: String) -> Unit
) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var imei1 by remember { mutableStateOf("") }
    var imei2 by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_device_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = strings.registerNewDevice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.imeiHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand (e.g. Samsung, Apple, Xiaomi)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model (e.g. Galaxy S24, iPhone 15)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = imei1,
                        onValueChange = { imei1 = it },
                        label = { Text("IMEI 1 (15 digits)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = imei2,
                        onValueChange = { imei2 = it },
                        label = { Text("IMEI 2 (Optional for Dual SIM)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = serial,
                        onValueChange = { serial = it },
                        label = { Text("Serial Number (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = purchaseDate,
                        onValueChange = { purchaseDate = it },
                        label = { Text("Purchase Date (e.g. 2025-06-12)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Security Notes & Distinguishing Marks") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                if (error != null) {
                    item {
                        Text(
                            text = error!!,
                            color = CyberRedAlert,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (brand.isBlank() || model.isBlank() || imei1.isBlank()) {
                                    error = "Brand, Model, and IMEI 1 are required."
                                } else {
                                    onSave(brand, model, imei1, imei2, serial, purchaseDate, ownerName, notes)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363F)),
                            modifier = Modifier.testTag("save_device_button")
                        ) {
                            Text("Save Device", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
