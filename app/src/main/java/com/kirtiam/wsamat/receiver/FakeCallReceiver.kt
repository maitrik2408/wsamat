package com.kirtiam.wsamat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FakeCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val callerName = intent?.getStringExtra("CALLER_NAME") ?: "Mom"
        val callerNumber = intent?.getStringExtra("CALLER_NUMBER") ?: "+1 555-0199"

        val callIntent = Intent().setClassName(context, "com.kirtiam.wsamat.FakeIncomingCallActivity").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("CALLER_NAME", callerName)
            putExtra("CALLER_NUMBER", callerNumber)
        }
        context.startActivity(callIntent)
    }
}
