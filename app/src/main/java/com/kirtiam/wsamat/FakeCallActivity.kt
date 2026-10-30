package com.kirtiam.wsamat

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kirtiam.wsamat.data.UserPreferences
import com.kirtiam.wsamat.receiver.FakeCallReceiver
import com.kirtiam.wsamat.service.ShakeService
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class FakeCallActivity : AppCompatActivity() {

    private lateinit var userPrefs: UserPreferences
    private lateinit var etCallerName: TextInputEditText
    private lateinit var etCallerNumber: TextInputEditText
    private lateinit var rgDelay: RadioGroup
    private lateinit var switchShake: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_call)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Fake Call & Gesture"

        userPrefs = UserPreferences(this)

        etCallerName = findViewById(R.id.etFakeCallerName)
        etCallerNumber = findViewById(R.id.etFakeCallerNumber)
        rgDelay = findViewById(R.id.rgFakeCallDelay)
        switchShake = findViewById(R.id.switchShake)

        etCallerName.setText(userPrefs.fakeCallerName)
        etCallerNumber.setText(userPrefs.fakeCallerNumber)
        switchShake.isChecked = userPrefs.isShakeEnabled

        findViewById<Button>(R.id.btnScheduleFakeCall).setOnClickListener {
            scheduleFakeCall()
        }

        switchShake.setOnCheckedChangeListener { _, isChecked ->
            userPrefs.isShakeEnabled = isChecked
            if (isChecked) {
                ShakeService.startService(this)
                Toast.makeText(this, "Shake detection enabled!", Toast.LENGTH_SHORT).show()
            } else {
                ShakeService.stopService(this)
                Toast.makeText(this, "Shake detection disabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleFakeCall() {
        val callerName = etCallerName.text.toString().trim().ifEmpty { "Mom" }
        val callerNumber = etCallerNumber.text.toString().trim().ifEmpty { "+1 555-0199" }

        userPrefs.fakeCallerName = callerName
        userPrefs.fakeCallerNumber = callerNumber

        val delaySeconds = when (rgDelay.checkedRadioButtonId) {
            R.id.rbDelay5 -> 5
            R.id.rbDelay15 -> 15
            R.id.rbDelay30 -> 30
            else -> 0
        }

        if (delaySeconds == 0) {
            // Launch immediately
            val intent = Intent(this, FakeIncomingCallActivity::class.java).apply {
                putExtra("CALLER_NAME", callerName)
                putExtra("CALLER_NUMBER", callerNumber)
            }
            startActivity(intent)
        } else {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, FakeCallReceiver::class.java).apply {
                putExtra("CALLER_NAME", callerName)
                putExtra("CALLER_NUMBER", callerNumber)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L)
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

            Toast.makeText(this, "Fake call scheduled in $delaySeconds seconds!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
