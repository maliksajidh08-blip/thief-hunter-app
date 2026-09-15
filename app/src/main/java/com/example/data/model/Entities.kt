package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registered_devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brand: String,
    val model: String,
    val imei1: String,
    val imei2: String = "",
    val serialNumber: String = "",
    val purchaseDate: String = "",
    val photoUri: String? = null,
    val invoiceUri: String? = null,
    val ownerName: String = "",
    val emergencyPhone: String = "",
    val status: String = "SAFE", // SAFE, STOLEN, REPORTED
    val notes: String = "",
    val isThisDevice: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "theft_reports")
data class TheftReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceModel: String,
    val brand: String,
    val imei: String,
    val theftDate: String,
    val locationName: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val firNumber: String,
    val policeStation: String,
    val officerName: String = "",
    val contactEmail: String,
    val contactPhone: String,
    val evidencePhotoUri: String? = null,
    val rewardAmount: String = "",
    val description: String = "",
    val status: String = "ACTIVE_SEARCH", // ACTIVE_SEARCH, RECOVERED, CLOSED
    val reportTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "intruder_logs")
data class IntruderLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerType: String, // WRONG_PIN, MOTION_SPIKE, POCKET_BREACH, CHARGER_DISCONNECTED
    val enteredPin: String = "",
    val photoPath: String? = null,
    val locationText: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
