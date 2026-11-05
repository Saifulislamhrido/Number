package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playClick() {
        if (!isSoundEnabled) return
        vibrate(20)
        scope.launch {
            generateTone(frequency = 880.0, durationMs = 40, volume = 0.4f)
        }
    }

    fun playMerge() {
        if (!isSoundEnabled) return
        vibrate(40)
        scope.launch {
            // Rising chime
            generateTone(frequency = 523.25, durationMs = 60, volume = 0.5f) // C5
            generateTone(frequency = 659.25, durationMs = 60, volume = 0.5f) // E5
            generateTone(frequency = 783.99, durationMs = 90, volume = 0.6f) // G5
        }
    }

    fun playCorrect() {
        if (!isSoundEnabled) return
        vibrate(30)
        scope.launch {
            generateTone(frequency = 659.25, durationMs = 80, volume = 0.5f)
            delay(40)
            generateTone(frequency = 880.00, durationMs = 120, volume = 0.6f)
        }
    }

    fun playWrong() {
        if (!isSoundEnabled) return
        vibrate(80)
        scope.launch {
            generateTone(frequency = 220.0, durationMs = 150, volume = 0.5f)
        }
    }

    fun playLevelComplete() {
        if (!isSoundEnabled) return
        vibrate(100)
        scope.launch {
            // Victory Fanfare
            val notes = listOf(523.25, 659.25, 783.99, 1046.50)
            for (note in notes) {
                generateTone(frequency = note, durationMs = 100, volume = 0.6f)
                delay(30)
            }
        }
    }

    fun playCoin() {
        if (!isSoundEnabled) return
        vibrate(25)
        scope.launch {
            generateTone(frequency = 987.77, durationMs = 60, volume = 0.5f) // B5
            generateTone(frequency = 1318.51, durationMs = 100, volume = 0.6f) // E6
        }
    }

    fun playMove() {
        if (!isSoundEnabled) return
        vibrate(15)
        scope.launch {
            generateTone(frequency = 440.0, durationMs = 30, volume = 0.3f)
        }
    }

    fun startMusic() {
        if (!isMusicEnabled) return
        if (musicJob?.isActive == true) return

        musicJob = scope.launch {
            // Pleasant ambient lofi game loop
            val chordProgression = listOf(
                listOf(261.63, 329.63, 392.00), // C Maj
                listOf(220.00, 261.63, 329.63), // A min
                listOf(349.23, 440.00, 523.25), // F Maj
                listOf(392.00, 493.88, 587.33)  // G Maj
            )

            while (isActive && isMusicEnabled) {
                for (chord in chordProgression) {
                    if (!isActive || !isMusicEnabled) break
                    for (note in chord) {
                        if (!isActive || !isMusicEnabled) break
                        generateTone(frequency = note, durationMs = 250, volume = 0.08f)
                        delay(280)
                    }
                    delay(300)
                }
            }
        }
    }

    fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun vibrate(millis: Long) {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (_: Exception) {
            // Ignore if vibration unavailable
        }
    }

    private fun generateTone(frequency: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate / 1000)
            val buffer = ShortArray(numSamples)
            val decaySamples = (numSamples * 0.25).toInt().coerceAtLeast(1)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val rawSin = sin(2.0 * Math.PI * frequency * time)
                // envelope fade out
                val envelope = if (i > numSamples - decaySamples) {
                    (numSamples - i).toFloat() / decaySamples
                } else {
                    1.0f
                }
                buffer[i] = (rawSin * 32767 * volume * envelope).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // AudioTrack failure fallback
        }
    }
}
