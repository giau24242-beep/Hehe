package com.example.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.audio.SoundEffectsManager
import com.example.audio.SoundType
import com.example.haptic.VibrationIntensity
import com.example.haptic.VibrationManager
import com.example.model.CrackImpact
import com.example.model.CrackState
import com.example.model.PrankSettings
import com.example.service.CrackOverlayService
import com.example.util.CrackGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object PrankManager {
    private var soundEffectsManager: SoundEffectsManager? = null
    private var vibrationManager: VibrationManager? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _settings = MutableStateFlow(PrankSettings())
    val settings: StateFlow<PrankSettings> = _settings.asStateFlow()

    private val _testCrackState = MutableStateFlow(CrackState())
    val testCrackState: StateFlow<CrackState> = _testCrackState.asStateFlow()

    private val _isOverlayRunning = MutableStateFlow(false)
    val isOverlayRunning: StateFlow<Boolean> = _isOverlayRunning.asStateFlow()

    fun initialize(context: Context) {
        if (soundEffectsManager == null) {
            soundEffectsManager = SoundEffectsManager(context.applicationContext)
        }
        if (vibrationManager == null) {
            vibrationManager = VibrationManager(context.applicationContext)
        }
    }

    fun getSoundManager(context: Context): SoundEffectsManager {
        if (soundEffectsManager == null) {
            soundEffectsManager = SoundEffectsManager(context.applicationContext)
        }
        return soundEffectsManager!!
    }

    fun getVibrationManager(context: Context): VibrationManager {
        if (vibrationManager == null) {
            vibrationManager = VibrationManager(context.applicationContext)
        }
        return vibrationManager!!
    }

    fun updateSettings(newSettings: PrankSettings) {
        _settings.value = newSettings
    }

    fun updateSoundType(soundType: SoundType) {
        _settings.update { it.copy(soundType = soundType) }
    }

    fun updateVibrationIntensity(intensity: VibrationIntensity) {
        _settings.update { it.copy(vibrationIntensity = intensity) }
    }

    fun toggleSound(enabled: Boolean) {
        _settings.update { it.copy(soundEnabled = enabled) }
    }

    fun toggleVibration(enabled: Boolean) {
        _settings.update { it.copy(vibrationEnabled = enabled) }
    }

    fun toggleSpiderweb(enabled: Boolean) {
        _settings.update { it.copy(spiderwebEnabled = enabled) }
    }

    fun toggleInkBleed(enabled: Boolean) {
        _settings.update { it.copy(inkBleedEnabled = enabled) }
    }

    fun toggleRgbGlitch(enabled: Boolean) {
        _settings.update { it.copy(rgbGlitchEnabled = enabled) }
    }

    fun toggleFlicker(enabled: Boolean) {
        _settings.update { it.copy(flickerGlitchEnabled = enabled) }
    }

    fun setOverlayRunning(running: Boolean) {
        _isOverlayRunning.value = running
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun startOverlayService(context: Context) {
        val intent = Intent(context, CrackOverlayService::class.java).apply {
            action = CrackOverlayService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopOverlayService(context: Context) {
        val intent = Intent(context, CrackOverlayService::class.java).apply {
            action = CrackOverlayService.ACTION_STOP
        }
        context.startService(intent)
    }

    // In-app test / preview interactive crack generator
    fun addTestImpact(x: Float, y: Float, width: Float, height: Float, context: Context) {
        val current = _testCrackState.value
        val newTapCount = current.totalTapCount + 1
        val config = _settings.value

        // Trigger sound & vibration
        if (config.soundEnabled) {
            getSoundManager(context).playCrackSound(config.soundType, config.soundVolume)
        }
        if (config.vibrationEnabled) {
            getVibrationManager(context).triggerVibration(config.vibrationIntensity)
        }

        val nearbyCount = current.impacts.count {
            val dx = it.touchX - x
            val dy = it.touchY - y
            (dx * dx + dy * dy) < 180f * 180f
        }

        val impact = CrackGenerator.createImpact(
            touchX = x,
            touchY = y,
            screenWidth = width,
            screenHeight = height,
            tapCount = newTapCount,
            existingNearbyCount = nearbyCount,
            settings = config
        )

        val globalGlitches = if (config.rgbGlitchEnabled) {
            CrackGenerator.generateGlobalGlitchLines(width, height, newTapCount)
        } else emptyList()

        val isFlicker = config.flickerGlitchEnabled && (newTapCount >= 6 && newTapCount % 3 == 0)

        _testCrackState.value = CrackState(
            impacts = current.impacts + impact,
            totalTapCount = newTapCount,
            globalGlitchLines = globalGlitches,
            isFlickering = isFlicker
        )
    }

    fun clearTestCracks() {
        _testCrackState.value = CrackState()
    }
}
