package com.kirtiam.wsamat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.kirtiam.wsamat.config.AppConfig
import com.kirtiam.wsamat.service.LockScreenSosService
import com.kirtiam.wsamat.ui.anim.ViewAnimationUtils
import com.kirtiam.wsamat.ui.base.BaseActivity
import com.kirtiam.wsamat.utils.LocationHelper
import com.kirtiam.wsamat.utils.SirenHelper
import com.google.android.material.card.MaterialCardView

class MainActivity : BaseActivity() {

    private lateinit var sirenHelper: SirenHelper
    private lateinit var locationHelper: LocationHelper

    private lateinit var tvHeaderGreeting: TextView
    private lateinit var tvProtectionBadge: TextView
    private lateinit var tvStatusContactsCount: TextView
    private lateinit var tvStatusShake: TextView
    private lateinit var tvStatusCheckIn: TextView
    private lateinit var tvSirenStatus: TextView

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { updateDashboardStatus() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sirenHelper = SirenHelper(this)
        locationHelper = LocationHelper(this)

        tvHeaderGreeting = findViewById(R.id.tvHeaderGreeting)
        tvProtectionBadge = findViewById(R.id.tvProtectionBadge)
        tvStatusContactsCount = findViewById(R.id.tvStatusContactsCount)
        tvStatusShake = findViewById(R.id.tvStatusShake)
        tvStatusCheckIn = findViewById(R.id.tvStatusCheckIn)
        tvSirenStatus = findViewById(R.id.tvSirenStatus)

        LockScreenSosService.startService(this)

        setupClickListenersAndAnimations()
        checkFirstRunOnboarding()
    }

    private fun setupClickListenersAndAnimations() {
        val sosBtn = findViewById<Button>(R.id.btnMainSos)
        ViewAnimationUtils.applyTouchScaleAnimation(sosBtn)
        sosBtn.setOnClickListener {
            navigateTo(SosCountdownActivity::class.java) {
                putExtra(AppConfig.EXTRA_TRIGGER_SOURCE, "1-Tap SOS Button")
            }
        }

        val cardTrial = findViewById<MaterialCardView>(R.id.cardTrialArea)
        val btnTrial = findViewById<Button>(R.id.btnOpenTrialArea)
        ViewAnimationUtils.applyTouchScaleAnimation(cardTrial)
        cardTrial.setOnClickListener { navigateTo(TrialAreaActivity::class.java) }
        btnTrial.setOnClickListener { navigateTo(TrialAreaActivity::class.java) }

        val cardSiren = findViewById<MaterialCardView>(R.id.cardQuickSiren)
        ViewAnimationUtils.applyTouchScaleAnimation(cardSiren)
        cardSiren.setOnClickListener { toggleSiren() }

        val card112 = findViewById<MaterialCardView>(R.id.cardQuickCall112)
        ViewAnimationUtils.applyTouchScaleAnimation(card112)
        card112.setOnClickListener { dial112() }

        val cardGps = findViewById<MaterialCardView>(R.id.cardQuickShareGps)
        ViewAnimationUtils.applyTouchScaleAnimation(cardGps)
        cardGps.setOnClickListener { shareLiveGps() }

        val cardContacts = findViewById<MaterialCardView>(R.id.cardTrustedContacts)
        ViewAnimationUtils.applyTouchScaleAnimation(cardContacts)
        cardContacts.setOnClickListener { navigateTo(TrustedContactsActivity::class.java) }

        val cardHelpline = findViewById<MaterialCardView>(R.id.cardEmergencyResources)
        ViewAnimationUtils.applyTouchScaleAnimation(cardHelpline)
        cardHelpline.setOnClickListener { navigateTo(EmergencyResourcesActivity::class.java) }

        val cardCheckin = findViewById<MaterialCardView>(R.id.cardSafetyCheckIn)
        ViewAnimationUtils.applyTouchScaleAnimation(cardCheckin)
        cardCheckin.setOnClickListener { navigateTo(SafetyCheckInActivity::class.java) }

        val cardFakeCall = findViewById<MaterialCardView>(R.id.cardFakeCall)
        ViewAnimationUtils.applyTouchScaleAnimation(cardFakeCall)
        cardFakeCall.setOnClickListener { navigateTo(FakeCallActivity::class.java) }

        val cardHistory = findViewById<MaterialCardView>(R.id.cardIncidentHistory)
        ViewAnimationUtils.applyTouchScaleAnimation(cardHistory)
        cardHistory.setOnClickListener { navigateTo(IncidentHistoryActivity::class.java) }

        val cardProfile = findViewById<MaterialCardView>(R.id.cardProfileSetup)
        ViewAnimationUtils.applyTouchScaleAnimation(cardProfile)
        cardProfile.setOnClickListener { navigateTo(ProfileActivity::class.java) }
    }

    private fun checkFirstRunOnboarding() {
        val contactCount = contactRepository.getContactCount()
        if (userPrefs.isFirstRun || contactCount == 0) {
            AlertDialog.Builder(this)
                .setTitle("🚨 Initial Safety Setup Recommended")
                .setMessage("To ensure your safety, WSAMat recommends saving at least one Trusted Emergency Contact and granting essential safety permissions.")
                .setCancelable(true)
                .setPositiveButton("Add Contact Now") { _, _ ->
                    navigateTo(TrustedContactsActivity::class.java)
                    promptEssentialPermissions()
                }
                .setNegativeButton("Skip for Now", null)
                .show()
            userPrefs.isFirstRun = false
        }
    }

    private fun promptEssentialPermissions() {
        requestPermissionsLauncher.launch(AppConfig.ESSENTIAL_PERMISSIONS)
    }

    private fun toggleSiren() {
        if (sirenHelper.isSirenPlaying()) {
            sirenHelper.stopSiren()
            tvSirenStatus.text = "Panic Siren"
            Toast.makeText(this, "Siren stopped.", Toast.LENGTH_SHORT).show()
        } else {
            sirenHelper.startSiren()
            tvSirenStatus.text = "STOP SIREN"
            Toast.makeText(this, "Loud Panic Siren Started!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dial112() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${AppConfig.HOTLINE_NATIONAL_EMERGENCY}"))
        startActivity(intent)
    }

    private fun shareLiveGps() {
        Toast.makeText(this, "Fetching current location...", Toast.LENGTH_SHORT).show()
        locationHelper.fetchCurrentLocation { loc ->
            val shareText = if (loc != null) {
                "EMERGENCY ALERT! Here is my live GPS location: ${loc.mapUrl}"
            } else {
                "EMERGENCY ALERT! I need help immediately. Please check on me."
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Live Location via"))
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboardStatus()
        LockScreenSosService.startService(this)
    }

    override fun onPause() {
        if (sirenHelper.isSirenPlaying()) {
            sirenHelper.stopSiren()
            tvSirenStatus.text = "Panic Siren"
        }
        super.onPause()
    }

    private fun updateDashboardStatus() {
        val userName = userPrefs.userName
        tvHeaderGreeting.text = "Hello, $userName!"

        val contactCount = contactRepository.getContactCount()
        tvStatusContactsCount.text = contactCount.toString()

        val isShakeActive = userPrefs.isShakeEnabled
        tvStatusShake.text = if (isShakeActive) "ON" else "OFF"
        tvStatusShake.setTextColor(
            if (isShakeActive) ContextCompat.getColor(this, android.R.color.holo_green_dark)
            else ContextCompat.getColor(this, android.R.color.darker_gray)
        )

        val isCheckInActive = userPrefs.isSafetyCheckInActive
        tvStatusCheckIn.text = if (isCheckInActive) "ON" else "OFF"
        tvStatusCheckIn.setTextColor(
            if (isCheckInActive) ContextCompat.getColor(this, android.R.color.holo_green_dark)
            else ContextCompat.getColor(this, android.R.color.darker_gray)
        )

        tvProtectionBadge.text = "PROTECTED"
        tvProtectionBadge.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
    }
}
