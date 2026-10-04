package com.example.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class VibrationIntensity(val title: String, val description: String) {
    TERRIFYING("Rung giật mình cực mạnh", "Xung rung gắt liên hoàn làm người dùng giật thót tim"),
    REALISTIC("Rung chấn động chân thực", "Mô phỏng lực nổ và va đập vật lý của kính bể"),
    SUBTLE("Rung vừa phải", "Rung nhẹ phản hồi xúc giác")
}

class VibrationManager(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun triggerVibration(intensity: VibrationIntensity = VibrationIntensity.TERRIFYING) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (intensity) {
                    VibrationIntensity.TERRIFYING -> {
                        // Sharp burst: [pause, pulse1, pause, pulse2, pause, pulse3]
                        val timings = longArrayOf(0, 90, 40, 140, 30, 220)
                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                        vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    VibrationIntensity.REALISTIC -> {
                        val timings = longArrayOf(0, 110, 50, 80)
                        val amplitudes = intArrayOf(0, 240, 0, 180)
                        vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    VibrationIntensity.SUBTLE -> {
                        vib.vibrate(VibrationEffect.createOneShot(100, 180))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (intensity) {
                    VibrationIntensity.TERRIFYING -> vib.vibrate(longArrayOf(0, 100, 50, 150, 40, 220), -1)
                    VibrationIntensity.REALISTIC -> vib.vibrate(longArrayOf(0, 120, 60, 90), -1)
                    VibrationIntensity.SUBTLE -> vib.vibrate(100)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerDoubleBackHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
