package com.example.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.BatteryManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import com.example.localization.AppLanguage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.sin
import kotlin.math.sqrt

enum class AlarmTriggerReason(val label: String) {
    MANUAL_PANIC("Manual Emergency SOS"),
    MOTION("Motion Movement Detected"),
    POCKET("Pocket/Bag Breach Detected"),
    CHARGER("Charger Cable Disconnected"),
    WRONG_PIN("Wrong Security PIN Attempt")
}

class AntiTheftEngine(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var vibrator: Vibrator? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // State flows
    private val _isArmed = MutableStateFlow(false)
    val isArmed = _isArmed.asStateFlow()

    private val _isAlarmActive = MutableStateFlow(false)
    val isAlarmActive = _isAlarmActive.asStateFlow()

    private val _activeReason = MutableStateFlow<AlarmTriggerReason?>(null)
    val activeReason = _activeReason.asStateFlow()

    // Feature toggles
    var motionProtectionEnabled = MutableStateFlow(true)
    var pocketProtectionEnabled = MutableStateFlow(true)
    var chargerAlarmEnabled = MutableStateFlow(true)
    var intruderSelfieEnabled = MutableStateFlow(true)
    var voiceAlarmEnabled = MutableStateFlow(true)
    var strobeEnabled = MutableStateFlow(true)

    // Sensitivity (Low=18f, Medium=13f, High=9.5f)
    var motionThreshold = 12.5f

    // Current PIN
    var securityPin = MutableStateFlow("1234")

    // Event broadcast for intruder capture
    private val _alarmEvents = MutableSharedFlow<AlarmTriggerReason>()
    val alarmEvents = _alarmEvents.asSharedFlow()

    // Sensor baseline tracking
    private var lastAccelX = 0f
    private var lastAccelY = 0f
    private var lastAccelZ = 0f
    private var hasBaseline = false
    private var wasInPocket = false

    // Siren AudioTrack
    private var sirenJob: Job? = null
    private var ttsJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Charger receiver
    private var isPowerReceiverRegistered = false
    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (!_isArmed.value || !chargerAlarmEnabled.value) return
            if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
                triggerAlarm(AlarmTriggerReason.CHARGER)
            }
        }
    }

    init {
        initVibrator()
        initTts()
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    isTtsReady = true
                }
            }
        } catch (_: Exception) {
            isTtsReady = false
        }
    }

    fun setLanguage(language: AppLanguage) {
        if (!isTtsReady || tts == null) return
        val loc = when (language) {
            AppLanguage.ENGLISH -> Locale.US
            AppLanguage.URDU -> Locale("ur", "PK")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.ARABIC -> Locale("ar", "SA")
            AppLanguage.SPANISH -> Locale("es", "ES")
        }
        try {
            val res = tts?.setLanguage(loc)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
        } catch (_: Exception) {}
    }

    fun armShield() {
        _isArmed.value = true
        hasBaseline = false
        wasInPocket = false

        // Register sensors
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        proximitySensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Register power receiver
        if (!isPowerReceiverRegistered) {
            val filter = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
            context.registerReceiver(powerReceiver, filter)
            isPowerReceiverRegistered = true
        }
    }

    fun disarmShield(enteredPin: String): Boolean {
        if (enteredPin == securityPin.value) {
            _isArmed.value = false
            stopAlarm()
            unregisterSensors()
            return true
        } else {
            // Wrong PIN Trap
            triggerAlarm(AlarmTriggerReason.WRONG_PIN)
            return false
        }
    }

    fun triggerAlarm(reason: AlarmTriggerReason) {
        if (_isAlarmActive.value) return
        _isAlarmActive.value = true
        _activeReason.value = reason

        scope.launch {
            _alarmEvents.emit(reason)
        }

        startAudioSiren()
        startVibration()
        if (voiceAlarmEnabled.value) {
            startVoiceAlert(reason)
        }
    }

    fun stopAlarm() {
        _isAlarmActive.value = false
        _activeReason.value = null
        sirenJob?.cancel()
        sirenJob = null
        ttsJob?.cancel()
        ttsJob = null
        vibrator?.cancel()
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    private fun startAudioSiren() {
        sirenJob?.cancel()
        sirenJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, 8192)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            try {
                audioTrack.play()
                val chunk = ShortArray(4096)
                var phase = 0.0
                var step = 0

                while (isActive && _isAlarmActive.value) {
                    // Two-tone European / police siren oscillating between 900Hz and 1450Hz
                    val freq = if ((step / 20) % 2 == 0) 900.0 else 1450.0
                    val delta = 2.0 * Math.PI * freq / sampleRate

                    for (i in chunk.indices) {
                        phase += delta
                        if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                        chunk[i] = (sin(phase) * 32767 * 0.95).toInt().toShort()
                    }
                    audioTrack.write(chunk, 0, chunk.size)
                    step++
                    delay(20)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        }
    }

    private fun startVoiceAlert(reason: AlarmTriggerReason) {
        ttsJob?.cancel()
        ttsJob = scope.launch(Dispatchers.Main) {
            delay(1500)
            if (!isActive || !_isAlarmActive.value || !isTtsReady || tts == null) return@launch
            val speechText = when (reason) {
                AlarmTriggerReason.MOTION -> "Warning! Unauthorized movement detected! Put the phone down!"
                AlarmTriggerReason.POCKET -> "Alert! Pickpocket detection triggered! Siren active!"
                AlarmTriggerReason.CHARGER -> "Alert! Charging cable detached! Enter security PIN!"
                AlarmTriggerReason.WRONG_PIN -> "Security breach! Intruder photo captured!"
                AlarmTriggerReason.MANUAL_PANIC -> "Emergency assistance requested! Location broadcast active!"
            }
            try {
                tts?.speak(speechText, TextToSpeech.QUEUE_ADD, null, "thief_hunter_alert")
            } catch (_: Exception) {}
        }
    }

    private fun startVibration() {
        scope.launch {
            val pattern = longArrayOf(0, 500, 200, 500, 200, 700)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            } catch (_: Exception) {}
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !_isArmed.value) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                if (!motionProtectionEnabled.value) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                if (!hasBaseline) {
                    lastAccelX = x
                    lastAccelY = y
                    lastAccelZ = z
                    hasBaseline = true
                    return
                }

                val dx = x - lastAccelX
                val dy = y - lastAccelY
                val dz = z - lastAccelZ
                val delta = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()

                lastAccelX = x
                lastAccelY = y
                lastAccelZ = z

                if (delta > motionThreshold) {
                    triggerAlarm(AlarmTriggerReason.MOTION)
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                if (!pocketProtectionEnabled.value) return
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange

                val isNear = distance < maxRange && distance < 4.0f
                if (isNear) {
                    wasInPocket = true
                } else if (wasInPocket && distance >= 4.0f) {
                    // Pulled out of pocket/bag!
                    triggerAlarm(AlarmTriggerReason.POCKET)
                    wasInPocket = false
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun cleanup() {
        stopAlarm()
        unregisterSensors()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
    }

    private fun unregisterSensors() {
        sensorManager?.unregisterListener(this)
        if (isPowerReceiverRegistered) {
            try {
                context.unregisterReceiver(powerReceiver)
            } catch (_: Exception) {}
            isPowerReceiverRegistered = false
        }
    }
}
