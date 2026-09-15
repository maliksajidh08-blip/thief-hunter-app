package com.example.security

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceTelemetry(
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val altitudeMeters: Double = 16.0,
    val accuracyMeters: Float = 4.5f,
    val speedKmh: Float = 0.0f,
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val connectionType: String = "WiFi (Protected)",
    val isOnline: Boolean = true,
    val lastUpdated: String = "Just now"
)

class LocationTracker(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _telemetry = MutableStateFlow(DeviceTelemetry())
    val telemetry = _telemetry.asStateFlow()

    private var isListeningLocation = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            updateBattery(intent)
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(batteryReceiver, filter)
        updateBattery(initialIntent)
        updateNetwork()
    }

    private fun updateBattery(intent: Intent?) {
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 85
        _telemetry.value = _telemetry.value.copy(
            batteryPercent = percent,
            isCharging = isCharging
        )
    }

    fun updateNetwork() {
        var isOnline = false
        var connType = "Offline"

        try {
            val network = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            if (capabilities != null) {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    isOnline = true
                    connType = when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi (High-Speed)"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 5G/LTE"
                        else -> "Connected"
                    }
                }
            }
        } catch (_: Exception) {}

        _telemetry.value = _telemetry.value.copy(
            isOnline = isOnline,
            connectionType = connType
        )
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (isListeningLocation || locationManager == null) return
        try {
            val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (gpsEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    1.0f,
                    this
                )
                isListeningLocation = true
            } else if (networkEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    1.0f,
                    this
                )
                isListeningLocation = true
            }

            // Get last known
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastGps?.let { onLocationChanged(it) }
        } catch (_: SecurityException) {
            // Permissions not yet granted
        }
    }

    fun stopLocationUpdates() {
        if (!isListeningLocation) return
        locationManager?.removeUpdates(this)
        isListeningLocation = false
    }

    override fun onLocationChanged(loc: Location) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(loc.time))
        _telemetry.value = _telemetry.value.copy(
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitudeMeters = loc.altitude,
            accuracyMeters = loc.accuracy,
            speedKmh = loc.speed * 3.6f,
            lastUpdated = timeStr
        )
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    fun cleanup() {
        stopLocationUpdates()
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }
}
