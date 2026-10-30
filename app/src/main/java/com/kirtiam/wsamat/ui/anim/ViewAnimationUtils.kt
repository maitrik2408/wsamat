package com.kirtiam.wsamat.ui.anim

import android.app.Activity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.kirtiam.wsamat.R

object ViewAnimationUtils {

    fun applyTouchScaleAnimation(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .setDuration(80)
                        .start()
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .start()
                }
            }
            false
        }
    }

    fun triggerHapticFeedback(view: View) {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyActivityTransition(activity: Activity, isBackward: Boolean = false) {
        @Suppress("DEPRECATION")
        if (isBackward) {
            activity.overridePendingTransition(R.anim.slide_in_left_3d, R.anim.slide_out_right_3d)
        } else {
            activity.overridePendingTransition(R.anim.slide_in_right_3d, R.anim.slide_out_left_3d)
        }
    }
}
