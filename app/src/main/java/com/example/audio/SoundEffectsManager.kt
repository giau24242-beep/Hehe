package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundType(val title: String, val description: String) {
    HEAVY_SHATTER("Vỡ kính cực mạnh", "Âm thanh kính cường lực nổ tung với mảnh vụn rơi"),
    CRISP_CRACK("Nứt giòn tan", "Tiếng nứt mạng nhện đanh thép, giòn rụm"),
    CAR_GLASS("Kính ô tô vỡ vụn", "Âm thanh kính nhiều lớp nổ lộp bộp liên hồi"),
    ELECTRIC_GLITCH("Nổ kính chập mạch", "Kính vỡ kèm tia lửa điện xẹt xẹt")
}

class SoundEffectsManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()
    private val sampleRate = 44100

    // Pre-synthesized audio buffers for instant zero-latency playback
    private var heavyShatterBuffer: ShortArray? = null
    private var crispCrackBuffer: ShortArray? = null
    private var carGlassBuffer: ShortArray? = null
    private var electricGlitchBuffer: ShortArray? = null

    init {
        scope.launch {
            heavyShatterBuffer = generateHeavyShatter()
            crispCrackBuffer = generateCrispCrack()
            carGlassBuffer = generateCarGlass()
            electricGlitchBuffer = generateElectricGlitch()
        }
    }

    /**
     * Synthesizes a loud, terrifying glass shatter sound:
     * - Hard transient attack (initial hammer strike / crack)
     * - Resonant high-Q filters simulating glass resonance frequencies (3.2kHz, 5.1kHz, 8.4kHz)
     * - Cascade of secondary fracture micro-bursts (splintering glass shards)
     * - Low-frequency cabinet flex thump (110Hz)
     */
    private fun generateHeavyShatter(): ShortArray {
        val durationMs = 850
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate

            // 1. Initial explosive transient spike (0 to 25ms)
            val transientEnv = if (t < 0.025) {
                (1.0 - t / 0.025) * (random.nextDouble() * 2.0 - 1.0)
            } else 0.0

            // 2. High-tension resonant glass fracture frequencies
            val f1 = sin(2.0 * PI * 3420.0 * t) * exp(-14.0 * t)
            val f2 = sin(2.0 * PI * 5180.0 * t) * exp(-19.0 * t)
            val f3 = sin(2.0 * PI * 8240.0 * t) * exp(-28.0 * t)
            val f4 = sin(2.0 * PI * 11500.0 * t) * exp(-35.0 * t)
            val glassTinkles = (f1 * 0.35 + f2 * 0.30 + f3 * 0.20 + f4 * 0.15)

            // 3. Dense glass crunch noise (filtered white noise decay)
            val noise = (random.nextDouble() * 2.0 - 1.0) * exp(-9.0 * t)

            // 4. Secondary micro-shatters (splintering glass cascade)
            var secondaryShatters = 0.0
            val splinterTimes = doubleArrayOf(0.04, 0.09, 0.15, 0.22, 0.31, 0.42)
            for (st in splinterTimes) {
                if (t >= st && t < st + 0.06) {
                    val dt = t - st
                    val microEnv = exp(-35.0 * dt)
                    val microFreq = sin(2.0 * PI * (4500.0 + random.nextInt(4000)) * dt)
                    secondaryShatters += (microFreq * 0.4 + (random.nextDouble() * 2 - 1) * 0.6) * microEnv
                }
            }

            // 5. Deep impact bass thump (display/phone body impact)
            val lowThump = sin(2.0 * PI * 95.0 * t) * exp(-18.0 * t) * 0.45

            // Total composite sound
            var sample = (transientEnv * 0.55 + noise * 0.45 + glassTinkles * 0.40 + secondaryShatters * 0.35 + lowThump * 0.30)
            sample = sample.coerceIn(-1.0, 1.0)
            buffer[i] = (sample * 32767.0).toInt().toShort()
        }
        return buffer
    }

    private fun generateCrispCrack(): ShortArray {
        val durationMs = 600
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val attack = if (t < 0.015) 1.0 - t / 0.015 else 0.0
            val resonance = sin(2.0 * PI * 4400.0 * t) * exp(-20.0 * t) +
                    sin(2.0 * PI * 7200.0 * t) * exp(-25.0 * t)
            val crackle = (random.nextDouble() * 2.0 - 1.0) * exp(-16.0 * t)
            val thump = sin(2.0 * PI * 130.0 * t) * exp(-22.0 * t) * 0.3

            var sample = (attack * 0.6 + crackle * 0.4 + resonance * 0.35 + thump)
            sample = sample.coerceIn(-1.0, 1.0)
            buffer[i] = (sample * 32767.0).toInt().toShort()
        }
        return buffer
    }

    private fun generateCarGlass(): ShortArray {
        val durationMs = 950
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val burst = (random.nextDouble() * 2.0 - 1.0) * exp(-7.5 * t)
            val bell = sin(2.0 * PI * 2900.0 * t) * exp(-11.0 * t) * 0.4 +
                    sin(2.0 * PI * 6100.0 * t) * exp(-18.0 * t) * 0.3
            // Dense raining shards
            var rain = 0.0
            if (t > 0.05) {
                rain = (random.nextDouble() * 2.0 - 1.0) * sin(2.0 * PI * (3000 + (t * 2000).toInt() % 4000) * t) * exp(-5.0 * t) * 0.35
            }
            var sample = (burst * 0.5 + bell + rain)
            sample = sample.coerceIn(-1.0, 1.0)
            buffer[i] = (sample * 32767.0).toInt().toShort()
        }
        return buffer
    }

    private fun generateElectricGlitch(): ShortArray {
        val durationMs = 700
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val glass = (random.nextDouble() * 2.0 - 1.0) * exp(-12.0 * t)
            val electric = (sin(2.0 * PI * 120.0 * t) * 0.4 + sin(2.0 * PI * 240.0 * t) * 0.3) *
                    if (random.nextFloat() > 0.3f) 1.0 else -1.0 * exp(-8.0 * t)
            val zap = sin(2.0 * PI * (800.0 + sin(2.0 * PI * 50.0 * t) * 600.0) * t) * exp(-10.0 * t) * 0.35

            var sample = (glass * 0.45 + electric * 0.4 + zap * 0.3)
            sample = sample.coerceIn(-1.0, 1.0)
            buffer[i] = (sample * 32767.0).toInt().toShort()
        }
        return buffer
    }

    /**
     * Plays the selected crack sound with zero latency.
     */
    fun playCrackSound(type: SoundType = SoundType.HEAVY_SHATTER, volume: Float = 1.0f) {
        scope.launch {
            val buffer = when (type) {
                SoundType.HEAVY_SHATTER -> heavyShatterBuffer ?: generateHeavyShatter()
                SoundType.CRISP_CRACK -> crispCrackBuffer ?: generateCrispCrack()
                SoundType.CAR_GLASS -> carGlassBuffer ?: generateCarGlass()
                SoundType.ELECTRIC_GLITCH -> electricGlitchBuffer ?: generateElectricGlitch()
            }

            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, buffer.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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

                audioTrack.setVolume(volume.coerceIn(0f, 1f))
                audioTrack.play()
                audioTrack.write(buffer, 0, buffer.size)

                // Wait for playback and release
                val waitTimeMs = (buffer.size.toDouble() / sampleRate * 1000).toLong() + 50
                kotlinx.coroutines.delay(waitTimeMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
