package com.smartboard.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticsManager {
    fun keyTap(context: Context, enabled: Boolean) {
        if (!enabled) return
        vibrate(context, light = true)
    }

    fun specialKey(context: Context, enabled: Boolean) {
        if (!enabled) return
        vibrate(context, light = false)
    }

    private fun vibrate(context: Context, light: Boolean) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = if (light) {
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            } else {
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(if (light) 20L else 35L)
        }
    }
}
