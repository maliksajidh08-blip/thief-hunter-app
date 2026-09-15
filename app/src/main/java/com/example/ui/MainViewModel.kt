package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DeviceEntity
import com.example.data.model.IntruderLogEntity
import com.example.data.model.TheftReportEntity
import com.example.data.repository.SecurityRepository
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalizationProvider
import com.example.security.AlarmTriggerReason
import com.example.security.AntiTheftEngine
import com.example.security.DeviceTelemetry
import com.example.security.LocationTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = SecurityRepository(database)

    val antiTheftEngine = AntiTheftEngine(application)
    val locationTracker = LocationTracker(application)

    // Current in-app language
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage = _currentLanguage.asStateFlow()

    val strings: StateFlow<AppStrings> = _currentLanguage.map {
        LocalizationProvider.getStrings(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        LocalizationProvider.getStrings(AppLanguage.ENGLISH)
    )

    // Dark Mode toggle (null = follow system, true = force dark, false = force light)
    private val _isDarkMode = MutableStateFlow<Boolean?>(true)
    val isDarkMode = _isDarkMode.asStateFlow()

    // Devices & Stats
    val registeredDevices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDevicesCount: StateFlow<Int> = repository.deviceCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allTheftReports: StateFlow<List<TheftReportEntity>> = repository.allTheftReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val intruderLogs: StateFlow<List<IntruderLogEntity>> = repository.intruderLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query for public stolen registry
    val searchImeiQuery = MutableStateFlow("")
    val filteredReports = combine(allTheftReports, searchImeiQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.imei.contains(query, ignoreCase = true) ||
            it.deviceModel.contains(query, ignoreCase = true) ||
            it.brand.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Telemetry state
    val telemetry: StateFlow<DeviceTelemetry> = locationTracker.telemetry

    // User Account State
    val userEmail = MutableStateFlow("sajidhr905@gmail.com")
    val userName = MutableStateFlow("Sajid H.")
    val userAvatarUrl = MutableStateFlow("https://lh3.googleusercontent.com/a/default-user")
    val isSignedIn = MutableStateFlow(true)

    // Emergency Contact
    val emergencyPhone = MutableStateFlow("+1 (555) 019-2831")
    val emergencyEmail = MutableStateFlow("emergency@thiefhunter.org")

    init {
        // Collect alarm events to record intruder logs
        viewModelScope.launch {
            antiTheftEngine.alarmEvents.collect { reason ->
                recordAlarmIncident(reason, enteredPin = "")
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        antiTheftEngine.setLanguage(language)
    }

    fun setDarkMode(dark: Boolean?) {
        _isDarkMode.value = dark
    }

    fun armShield() {
        antiTheftEngine.armShield()
        locationTracker.startLocationUpdates()
    }

    fun disarmShieldWithPin(pin: String): Boolean {
        val success = antiTheftEngine.disarmShield(pin)
        if (!success) {
            recordAlarmIncident(AlarmTriggerReason.WRONG_PIN, enteredPin = pin)
        }
        return success
    }

    fun triggerEmergencyPanic() {
        antiTheftEngine.triggerAlarm(AlarmTriggerReason.MANUAL_PANIC)
    }

    fun stopAlarmWithPin(pin: String): Boolean {
        return disarmShieldWithPin(pin)
    }

    private fun recordAlarmIncident(reason: AlarmTriggerReason, enteredPin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val coords = telemetry.value
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val log = IntruderLogEntity(
                triggerType = reason.name,
                enteredPin = enteredPin,
                locationText = "Lat: %.4f, Lon: %.4f".format(coords.latitude, coords.longitude),
                latitude = coords.latitude,
                longitude = coords.longitude,
                timestamp = System.currentTimeMillis()
            )
            repository.insertIntruderLog(log)
        }
    }

    fun saveIntruderPhoto(bitmap: Bitmap, reason: AlarmTriggerReason, enteredPin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val filename = "intruder_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                val coords = telemetry.value
                val log = IntruderLogEntity(
                    triggerType = reason.name,
                    enteredPin = enteredPin,
                    photoPath = file.absolutePath,
                    locationText = "Lat: %.4f, Lon: %.4f".format(coords.latitude, coords.longitude),
                    latitude = coords.latitude,
                    longitude = coords.longitude,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertIntruderLog(log)
            } catch (_: Exception) {}
        }
    }

    fun registerDevice(
        brand: String,
        model: String,
        imei1: String,
        imei2: String,
        serial: String,
        purchaseDate: String,
        ownerName: String,
        notes: String
    ): Boolean {
        if (registeredDevices.value.size >= 10) return false
        viewModelScope.launch(Dispatchers.IO) {
            val device = DeviceEntity(
                brand = brand.trim(),
                model = model.trim(),
                imei1 = imei1.trim(),
                imei2 = imei2.trim(),
                serialNumber = serial.trim(),
                purchaseDate = purchaseDate.trim(),
                ownerName = ownerName.trim(),
                emergencyPhone = emergencyPhone.value,
                notes = notes.trim(),
                status = "SAFE"
            )
            repository.insertDevice(device)
        }
        return true
    }

    fun deleteDevice(device: DeviceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDevice(device)
        }
    }

    fun submitTheftReport(
        deviceModel: String,
        brand: String,
        imei: String,
        theftDate: String,
        locationName: String,
        firNumber: String,
        policeStation: String,
        officerName: String,
        contactEmail: String,
        contactPhone: String,
        rewardAmount: String,
        description: String,
        photoUri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val coords = telemetry.value
            val report = TheftReportEntity(
                deviceModel = deviceModel.trim(),
                brand = brand.trim(),
                imei = imei.trim(),
                theftDate = theftDate.trim(),
                locationName = locationName.trim(),
                latitude = coords.latitude,
                longitude = coords.longitude,
                firNumber = firNumber.trim(),
                policeStation = policeStation.trim(),
                officerName = officerName.trim(),
                contactEmail = contactEmail.trim(),
                contactPhone = contactPhone.trim(),
                rewardAmount = rewardAmount.trim(),
                description = description.trim(),
                evidencePhotoUri = photoUri,
                status = "ACTIVE_SEARCH"
            )
            repository.insertTheftReport(report)
        }
    }

    fun clearIntruderLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearIntruderLogs()
        }
    }

    fun updateSecurityPin(newPin: String) {
        antiTheftEngine.securityPin.value = newPin
    }

    override fun onCleared() {
        super.onCleared()
        antiTheftEngine.cleanup()
        locationTracker.cleanup()
    }
}
