package com.example.wsamat

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wsamat.data.UserPreferences
import com.example.wsamat.service.SafetyCheckInService
import java.util.Locale

class SafetyCheckInActivity : AppCompatActivity() {

    private lateinit var userPrefs: UserPreferences
    private lateinit var tvStatusTitle: TextView
    private lateinit var tvTimerDisplay: TextView
    private lateinit var btnImSafe: Button
    private lateinit var btnStartTimer: Button
    private lateinit var rgDuration: RadioGroup
    private lateinit var cardTimerSetup: View

    private var uiTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_check_in)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Safety Check-in"

        userPrefs = UserPreferences(this)

        tvStatusTitle = findViewById(R.id.tvCheckInStatusTitle)
        tvTimerDisplay = findViewById(R.id.tvCheckInTimerDisplay)
        btnImSafe = findViewById(R.id.btnImSafe)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        rgDuration = findViewById(R.id.rgDuration)
        cardTimerSetup = findViewById(R.id.cardTimerSetup)

        btnStartTimer.setOnClickListener { startCheckInTimer() }
        btnImSafe.setOnClickListener { cancelCheckInTimer() }
    }

    override fun onResume() {
        super.onResume()
        updateUiState()
    }

    private fun startCheckInTimer() {
        val selectedRbId = rgDuration.checkedRadioButtonId
        val minutes = when (selectedRbId) {
            R.id.rb30Min -> 30
            R.id.rb60Min -> 60
            else -> 15
        }

        val targetTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        userPrefs.isSafetyCheckInActive = true
        userPrefs.checkInTargetTimeMillis = targetTime

        SafetyCheckInService.startService(this)
        Toast.makeText(this, "Safety check-in timer set for $minutes minutes!", Toast.LENGTH_SHORT).show()
        updateUiState()
    }

    private fun cancelCheckInTimer() {
        SafetyCheckInService.stopService(this)
        Toast.makeText(this, "Check-in confirmed! Glad you are safe.", Toast.LENGTH_SHORT).show()
        updateUiState()
    }

    private fun updateUiState() {
        uiTimer?.cancel()

        if (userPrefs.isSafetyCheckInActive) {
            val remaining = userPrefs.checkInTargetTimeMillis - System.currentTimeMillis()
            if (remaining > 0) {
                tvStatusTitle.text = "Check-in Active!"
                btnImSafe.visibility = View.VISIBLE
                cardTimerSetup.visibility = View.GONE

                startUiCountdown(remaining)
            } else {
                resetInactiveState()
            }
        } else {
            resetInactiveState()
        }
    }

    private fun resetInactiveState() {
        tvStatusTitle.text = "No Timer Active"
        tvTimerDisplay.text = "00:00"
        btnImSafe.visibility = View.GONE
        cardTimerSetup.visibility = View.VISIBLE
    }

    private fun startUiCountdown(durationMillis: Long) {
        uiTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 1000) / 60
                val secs = (millisUntilFinished / 1000) % 60
                tvTimerDisplay.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
            }

            override fun onFinish() {
                resetInactiveState()
            }
        }.start()
    }

    override fun onDestroy() {
        uiTimer?.cancel()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
