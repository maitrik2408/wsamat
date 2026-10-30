package com.kirtiam.wsamat

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kirtiam.wsamat.ui.anim.ViewAnimationUtils
import com.kirtiam.wsamat.ui.base.BaseActivity
import com.google.android.material.card.MaterialCardView

class WelcomeActivity : BaseActivity() {

    private lateinit var layoutLogoContainer: FrameLayout
    private lateinit var viewGlowBg: View
    private lateinit var ivLogo: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvTagline: TextView
    private lateinit var cardFeatures: MaterialCardView
    private lateinit var btnGetStarted: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        layoutLogoContainer = findViewById(R.id.layoutLogoContainer)
        viewGlowBg = findViewById(R.id.viewGlowBg)
        ivLogo = findViewById(R.id.ivWelcomeLogo)
        tvTitle = findViewById(R.id.tvWelcomeTitle)
        tvTagline = findViewById(R.id.tvWelcomeTagline)
        cardFeatures = findViewById(R.id.cardWelcomeFeatures)
        btnGetStarted = findViewById(R.id.btnWelcomeGetStarted)

        ViewAnimationUtils.applyTouchScaleAnimation(btnGetStarted)

        btnGetStarted.setOnClickListener {
            val targetClass = if (userPrefs.isLoggedIn) {
                MainActivity::class.java
            } else {
                AuthActivity::class.java
            }
            navigateTo(targetClass, finishCurrent = true)
        }

        runEntranceAnimations()
    }

    private fun runEntranceAnimations() {
        // Initial hidden state
        layoutLogoContainer.scaleX = 0.2f
        layoutLogoContainer.scaleY = 0.2f
        layoutLogoContainer.alpha = 0f

        tvTitle.translationY = 50f
        tvTitle.alpha = 0f

        tvTagline.translationY = 40f
        tvTagline.alpha = 0f

        cardFeatures.translationY = 60f
        cardFeatures.alpha = 0f

        btnGetStarted.translationY = 80f
        btnGetStarted.alpha = 0f

        // Animate Shield Logo Scale and Overshoot
        layoutLogoContainer.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .alpha(1.0f)
            .setDuration(750)
            .setInterpolator(OvershootInterpolator(1.4f))
            .withEndAction {
                startGlowPulseAnimation()
            }
            .start()

        // Animate Title
        tvTitle.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(300)
            .setDuration(500)
            .start()

        // Animate Tagline
        tvTagline.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(450)
            .setDuration(500)
            .start()

        // Animate Feature Cards
        cardFeatures.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(600)
            .setDuration(550)
            .start()

        // Animate Action Button
        btnGetStarted.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(750)
            .setDuration(600)
            .start()
    }

    private fun startGlowPulseAnimation() {
        viewGlowBg.animate()
            .scaleX(1.25f)
            .scaleY(1.25f)
            .alpha(0.45f)
            .setDuration(1200)
            .withEndAction {
                viewGlowBg.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(0.25f)
                    .setDuration(1200)
                    .withEndAction {
                        if (!isFinishing && !isDestroyed) {
                            startGlowPulseAnimation()
                        }
                    }
                    .start()
            }
            .start()
    }
}
