package com.kirtiam.wsamat

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class FakeIncomingCallActivity : AppCompatActivity() {

    private lateinit var layoutIncomingCall: LinearLayout
    private lateinit var layoutActiveCall: LinearLayout
    private lateinit var tvIncomingName: TextView
    private lateinit var tvIncomingNumber: TextView
    private lateinit var tvActiveName: TextView
    private lateinit var tvActiveNumber: TextView
    private lateinit var tvActiveTimer: TextView

    private var ringtone: Ringtone? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var secondsInCall = 0
    private var callTimerRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_incoming_call)

        val callerName = intent.getStringExtra("CALLER_NAME") ?: "Mom"
        val callerNumber = intent.getStringExtra("CALLER_NUMBER") ?: "+1 555-0199"

        layoutIncomingCall = findViewById(R.id.layoutIncomingCall)
        layoutActiveCall = findViewById(R.id.layoutActiveCall)
        tvIncomingName = findViewById(R.id.tvIncomingName)
        tvIncomingNumber = findViewById(R.id.tvIncomingNumber)
        tvActiveName = findViewById(R.id.tvActiveCallerName)
        tvActiveNumber = findViewById(R.id.tvActiveCallerNumber)
        tvActiveTimer = findViewById(R.id.tvActiveCallTimer)

        tvIncomingName.text = callerName
        tvIncomingNumber.text = callerNumber
        tvActiveName.text = callerName
        tvActiveNumber.text = callerNumber

        findViewById<Button>(R.id.btnAcceptCall).setOnClickListener {
            acceptCall()
        }

        findViewById<Button>(R.id.btnDeclineCall).setOnClickListener {
            endCall()
        }

        findViewById<Button>(R.id.btnEndCall).setOnClickListener {
            endCall()
        }

        playRingtone()
    }

    private fun playRingtone() {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun acceptCall() {
        ringtone?.stop()
        layoutIncomingCall.visibility = View.GONE
        layoutActiveCall.visibility = View.VISIBLE

        callTimerRunnable = object : Runnable {
            override fun run() {
                secondsInCall++
                val mins = secondsInCall / 60
                val secs = secondsInCall % 60
                tvActiveTimer.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(callTimerRunnable!!, 1000)
    }

    private fun endCall() {
        ringtone?.stop()
        callTimerRunnable?.let { handler.removeCallbacks(it) }
        finish()
    }

    override fun onDestroy() {
        ringtone?.stop()
        callTimerRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }
}
