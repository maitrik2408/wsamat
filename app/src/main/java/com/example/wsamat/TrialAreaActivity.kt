package com.example.wsamat

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wsamat.receiver.FakeCallReceiver
import com.example.wsamat.utils.AudioRecorderHelper
import com.example.wsamat.utils.CameraHelper
import com.example.wsamat.utils.LocationHelper
import com.example.wsamat.utils.SirenHelper
import java.io.File

class TrialAreaActivity : AppCompatActivity() {

    private lateinit var cameraHelper: CameraHelper
    private lateinit var audioRecorderHelper: AudioRecorderHelper
    private lateinit var locationHelper: LocationHelper
    private lateinit var sirenHelper: SirenHelper

    private lateinit var tvPhotoStatus: TextView
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var tvAudioStatus: TextView
    private lateinit var btnPlayAudio: Button
    private lateinit var tvLocationStatus: TextView
    private lateinit var btnSiren: Button
    private lateinit var btnWhistle: Button

    private var trialAudioPath: String? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trial_area)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Safety Sandbox & Trial Area"

        cameraHelper = CameraHelper(this)
        audioRecorderHelper = AudioRecorderHelper(this)
        locationHelper = LocationHelper(this)
        sirenHelper = SirenHelper(this)

        tvPhotoStatus = findViewById(R.id.tvTrialPhotoStatus)
        ivPhotoPreview = findViewById(R.id.ivTrialPhotoPreview)
        tvAudioStatus = findViewById(R.id.tvTrialAudioStatus)
        btnPlayAudio = findViewById(R.id.btnTrialPlayAudio)
        tvLocationStatus = findViewById(R.id.tvTrialLocationStatus)
        btnSiren = findViewById(R.id.btnTrialSiren)
        btnWhistle = findViewById(R.id.btnTrialWhistle)

        setupListeners()
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnTrialTestCamera).setOnClickListener {
            tvPhotoStatus.text = "Capturing test photo in background..."
            cameraHelper.captureSilentPhoto { photoPath ->
                if (!photoPath.isNullOrEmpty() && File(photoPath).exists()) {
                    tvPhotoStatus.text = "✓ Test Photo Captured Successfully!\nSaved at: $photoPath"
                    val bitmap = BitmapFactory.decodeFile(photoPath)
                    ivPhotoPreview.setImageBitmap(bitmap)
                    ivPhotoPreview.visibility = View.VISIBLE
                } else {
                    tvPhotoStatus.text = "✗ Camera Capture Failed. Ensure Camera permission is granted."
                }
            }
        }

        findViewById<Button>(R.id.btnTrialRecordAudio).setOnClickListener {
            tvAudioStatus.text = "🎙️ Recording 5-second test audio..."
            btnPlayAudio.isEnabled = false
            trialAudioPath = audioRecorderHelper.startRecording(5)

            Handler(Looper.getMainLooper()).postDelayed({
                tvAudioStatus.text = "✓ 5-second test audio clip recorded!\nTap Play to listen."
                btnPlayAudio.isEnabled = true
            }, 5500)
        }

        btnPlayAudio.setOnClickListener {
            if (!trialAudioPath.isNullOrEmpty() && File(trialAudioPath!!).exists()) {
                try {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(trialAudioPath)
                        prepare()
                        start()
                    }
                    Toast.makeText(this, "Playing recorded test audio...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Unable to play audio file.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.btnTrialFetchLocation).setOnClickListener {
            tvLocationStatus.text = "Fetching current GPS coordinates..."
            locationHelper.fetchCurrentLocation { loc ->
                if (loc != null) {
                    tvLocationStatus.text = "✓ GPS Location Acquired!\nAddress: ${loc.formattedAddress}\nLink: ${loc.mapUrl}"
                } else {
                    tvLocationStatus.text = "✗ GPS Location Acquisition Failed. Ensure Location permission is enabled."
                }
            }
        }

        btnSiren.setOnClickListener {
            if (sirenHelper.isSirenPlaying()) {
                sirenHelper.stopSiren()
                btnSiren.text = "Panic Siren"
            } else {
                sirenHelper.startSiren()
                btnSiren.text = "STOP SIREN"
                Toast.makeText(this, "Loud Siren Playing!", Toast.LENGTH_SHORT).show()
            }
        }

        btnWhistle.setOnClickListener {
            if (sirenHelper.isSirenPlaying()) {
                sirenHelper.stopSiren()
                btnWhistle.text = "3.8kHz Whistle"
            } else {
                sirenHelper.startHighDecibelWhistle()
                btnWhistle.text = "STOP WHISTLE"
                Toast.makeText(this, "High-Decibel Distress Whistle Active!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnTrialFakeCall).setOnClickListener {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, FakeCallReceiver::class.java).apply {
                putExtra("CALLER_NAME", "Mom (Test Call)")
                putExtra("CALLER_NUMBER", "+1 555-0199")
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                2002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerTime = System.currentTimeMillis() + 3000L
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Toast.makeText(this, "Test fake call scheduled in 3 seconds!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnTrialCalculator).setOnClickListener {
            startActivity(Intent(this, CamouflageCalculatorActivity::class.java))
        }
    }

    override fun onPause() {
        if (sirenHelper.isSirenPlaying()) {
            sirenHelper.stopSiren()
        }
        mediaPlayer?.release()
        mediaPlayer = null
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
