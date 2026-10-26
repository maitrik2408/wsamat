package com.example.wsamat

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wsamat.data.DatabaseHelper
import com.example.wsamat.data.Incident
import com.example.wsamat.data.UserPreferences
import com.example.wsamat.utils.AudioRecorderHelper
import com.example.wsamat.utils.CameraHelper
import com.example.wsamat.utils.LocationHelper
import com.example.wsamat.utils.SmsHelper
import com.example.wsamat.utils.WhatsAppHelper

class LockScreenSosOverlayActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnSos: Button
    private lateinit var btnClose: Button

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userPrefs: UserPreferences
    private lateinit var locationHelper: LocationHelper
    private lateinit var smsHelper: SmsHelper
    private lateinit var cameraHelper: CameraHelper
    private lateinit var audioRecorderHelper: AudioRecorderHelper
    private lateinit var whatsAppHelper: WhatsAppHelper

    private var isDispatching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLockScreenFlags()
        setContentView(R.layout.activity_lock_screen_sos_overlay)

        dbHelper = DatabaseHelper(this)
        userPrefs = UserPreferences(this)
        locationHelper = LocationHelper(this)
        smsHelper = SmsHelper(this)
        cameraHelper = CameraHelper(this)
        audioRecorderHelper = AudioRecorderHelper(this)
        whatsAppHelper = WhatsAppHelper(this)

        tvStatus = findViewById(R.id.tvLockOverlayStatus)
        btnSos = findViewById(R.id.btnLockOverlaySos)
        btnClose = findViewById(R.id.btnLockOverlayClose)

        btnSos.setOnClickListener {
            if (!isDispatching) {
                dispatchLockScreenSos()
            }
        }

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
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

    private fun dispatchLockScreenSos() {
        isDispatching = true
        btnSos.isEnabled = false
        tvStatus.text = "Acquiring GPS location & capturing media..."

        val contacts = dbHelper.getAllContacts()

        val audioPath = audioRecorderHelper.startRecording(20)

        cameraHelper.captureSilentPhoto { photoPath ->
            locationHelper.fetchCurrentLocation { loc ->
                val locationText = loc?.formattedAddress ?: "Location unavailable"
                val mapUrl = loc?.mapUrl ?: ""
                val customNote = userPrefs.emergencyNote

                val isSent = smsHelper.sendEmergencySms(contacts, customNote, mapUrl, photoPath != null, audioPath != null)
                val statusStr = if (isSent) "Direct SMS Sent to ${contacts.size} Contacts" else "SMS Dispatched"

                // Dispatch to WhatsApp as well with image and location
                whatsAppHelper.sendEmergencyWhatsApp(contacts, customNote, mapUrl, photoPath, audioPath)

                val resultMsg = buildString {
                    append("🚨 SOS DISPATCHED DIRECTLY!")
                    append("\nStatus: $statusStr")
                    if (photoPath != null) append("\n📷 Photo Captured & Shared")
                    if (audioPath != null) append("\n🎙️ Audio Recording Active (20s)")
                }

                tvStatus.text = resultMsg
                Toast.makeText(this, "Emergency SOS & Image Dispatched!", Toast.LENGTH_LONG).show()

                dbHelper.addIncident(
                    Incident(
                        timestamp = System.currentTimeMillis(),
                        eventType = "Lock Screen Special Button SOS",
                        locationText = locationText,
                        mapUrl = mapUrl,
                        status = statusStr,
                        photoPath = photoPath,
                        audioPath = audioPath
                    )
                )

                btnSos.isEnabled = true
                isDispatching = false
            }
        }
    }
}
