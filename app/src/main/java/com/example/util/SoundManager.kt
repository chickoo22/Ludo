package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundManager(context: Context) {
    private val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val toneGenerator = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    } catch (e: Exception) {
        null
    }

    fun playDiceRollSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            vibrate(50)
        } catch (e: Exception) {}
    }

    fun playMoveSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_1, 80)
            vibrate(30)
        } catch (e: Exception) {}
    }

    fun playCaptureSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 300)
            vibrate(120)
        } catch (e: Exception) {}
    }

    fun playWinSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 600)
            vibrate(250)
        } catch (e: Exception) {}
    }

    private fun vibration(milliseconds: Long) {
        vibrate(milliseconds)
    }

    private fun vibrate(ms: Long) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ms)
            }
        } catch (e: Exception) {}
    }
}
