package com.example.wsamat

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.wsamat.data.DatabaseHelper
import com.example.wsamat.data.Incident
import com.example.wsamat.data.UserPreferences
import com.example.wsamat.utils.AudioRecorderHelper
import com.example.wsamat.utils.CameraHelper
import com.example.wsamat.utils.LocationHelper
import com.example.wsamat.utils.SmsHelper
import com.example.wsamat.utils.WhatsAppHelper

class SosCountdownActivity : AppCompatActivity() {

    private lateinit var tvCountdownText: TextView
    private lateinit var tvTriggerSource: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutCountdownView: LinearLayout
    private lateinit var layoutResultView: LinearLayout
    private lateinit var tvResultLocation: TextView
    private lateinit var tvResultSms: TextView

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userPrefs: UserPreferences
    private lateinit var locationHelper: LocationHelper
    private lateinit var smsHelper: SmsHelper
    private lateinit var cameraHelper: CameraHelper
    private lateinit var audioRecorderHelper: AudioRecorderHelper
    private lateinit var whatsAppHelper: WhatsAppHelper

    private var countDownTimer: CountDownTimer? = null
    private var vibrator: Vibrator? = null
    private var triggerSource: String = "One-tap SOS"
    private var isCancelled = false
    private var currentMapUrl: String = ""
    private var currentPhotoPath: String? = null

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (smsGranted && locationGranted) {
            Toast.makeText(this, "Emergency permissions granted!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLockScreenFlags()
        setContentView(R.layout.activity_sos_countdown)

        triggerSource = intent.getStringExtra("TRIGGER_SOURCE") ?: "One-tap SOS"

        dbHelper = DatabaseHelper(this)
        userPrefs = UserPreferences(this)
        locationHelper = LocationHelper(this)
        smsHelper = SmsHelper(this)
        cameraHelper = CameraHelper(this)
        audioRecorderHelper = AudioRecorderHelper(this)
        whatsAppHelper = WhatsAppHelper(this)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

        tvCountdownText = findViewById(R.id.tvCountdownText)
        tvTriggerSource = findViewById(R.id.tvTriggerSource)
        progressBar = findViewById(R.id.progressBarCountdown)
        layoutCountdownView = findViewById(R.id.layoutCountdownView)
        layoutResultView = findViewById(R.id.layoutResultView)
        tvResultLocation = findViewById(R.id.tvResultLocation)
        tvResultSms = findViewById(R.id.tvResultSms)

        tvTriggerSource.text = "Source: $triggerSource"

        findViewById<Button>(R.id.btnCancelSos).setOnClickListener {
            cancelSos()
        }

        findViewById<Button>(R.id.btnShareWhatsApp).setOnClickListener {
            val contacts = dbHelper.getAllContacts()
            val customNote = userPrefs.emergencyNote
            whatsAppHelper.sendEmergencyWhatsApp(contacts, customNote, currentMapUrl, currentPhotoPath)
        }

        findViewById<Button>(R.id.btnShareLocationText).setOnClickListener {
            shareLocationText()
        }

        findViewById<Button>(R.id.btnDoneSos).setOnClickListener {
            finish()
        }

        checkAndRequestPermissions()
        startCountdown()
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startCountdown() {
        vibratePattern()
        countDownTimer = object : CountDownTimer(7000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000) + 1
                val displaySec = if (seconds > 7) 7 else seconds
                tvCountdownText.text = displaySec.toString()
                val progress = ((millisUntilFinished.toDouble() / 7000.0) * 100).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                if (!isCancelled) {
                    dispatchEmergencyAlerts()
                }
            }
        }.start()
    }

    private fun cancelSos() {
        isCancelled = true
        countDownTimer?.cancel()
        vibrator?.cancel()
        audioRecorderHelper.stopRecording()
        Toast.makeText(this, "Emergency SOS Cancelled.", Toast.LENGTH_SHORT).show()

        dbHelper.addIncident(
            Incident(
                timestamp = System.currentTimeMillis(),
                eventType = triggerSource,
                locationText = "User Cancelled during Countdown",
                mapUrl = "",
                status = "Cancelled"
            )
        )
        finish()
    }

    private fun dispatchEmergencyAlerts() {
        layoutCountdownView.visibility = View.GONE
        layoutResultView.visibility = View.VISIBLE

        tvResultLocation.text = "Acquiring GPS Location & Camera Image..."
        tvResultSms.text = "Preparing Emergency SMS & Audio Recording..."

        val contacts = dbHelper.getAllContacts()

        val audioPath = audioRecorderHelper.startRecording(20)

        cameraHelper.captureSilentPhoto { photoPath ->
            currentPhotoPath = photoPath

            locationHelper.fetchCurrentLocation { loc ->
                val locationText = loc?.formattedAddress ?: "Location unavailable"
                currentMapUrl = loc?.mapUrl ?: ""

                tvResultLocation.text = "Location: $locationText"

                val customNote = userPrefs.emergencyNote
                val isSent = smsHelper.sendEmergencySms(contacts, customNote, currentMapUrl)

                val statusStr = if (isSent) {
                    "Automatically Sent to ${contacts.size} Contact(s)"
                } else {
                    "SMS Dispatched"
                }

                val mediaStatus = buildString {
                    append("Alert Status: $statusStr")
                    if (!photoPath.isNullOrEmpty()) append("\n📷 Photo Captured")
                    if (!audioPath.isNullOrEmpty()) append("\n🎙️ Audio Recording Active (20s)")
                }
                tvResultSms.text = mediaStatus

                dbHelper.addIncident(
                    Incident(
                        timestamp = System.currentTimeMillis(),
                        eventType = triggerSource,
                        locationText = locationText,
                        mapUrl = currentMapUrl,
                        status = statusStr,
                        photoPath = photoPath,
                        audioPath = audioPath
                    )
                )
            }
        }
    }

    private fun shareLocationText() {
        val shareText = if (currentMapUrl.isNotEmpty()) {
            "EMERGENCY! I need immediate help. Here is my location: $currentMapUrl"
        } else {
            "EMERGENCY! I need immediate help."
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share Emergency Location"))
    }

    private fun vibratePattern() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        vibrator?.cancel()
        super.onDestroy()
    }
}
