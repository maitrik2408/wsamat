package com.example.wsamat.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.wsamat.data.Contact

class SmsHelper(private val context: Context) {

    fun sendEmergencySms(
        contacts: List<Contact>,
        customMessage: String,
        mapUrl: String,
        hasPhoto: Boolean = false,
        hasAudio: Boolean = false
    ): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastSmsDispatchTime < SMS_DEBOUNCE_INTERVAL_MS) {
            Log.d("SmsHelper", "SMS dispatch debounced to prevent duplicate sending loop.")
            return true
        }

        if (contacts.isEmpty()) {
            Toast.makeText(context, "No trusted contacts configured!", Toast.LENGTH_SHORT).show()
            return false
        }

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val evidenceText = buildString {
            if (hasPhoto && hasAudio) append("\n[Media Evidence: Photo & Audio Recorded]")
            else if (hasPhoto) append("\n[Media Evidence: Photo Recorded]")
            else if (hasAudio) append("\n[Media Evidence: Audio Recorded]")
        }

        val fullMessage = buildString {
            append(customMessage)
            if (mapUrl.isNotEmpty()) {
                append("\nLocation: ")
                append(mapUrl)
            }
            append(evidenceText)
        }

        if (!hasSmsPermission) {
            Toast.makeText(context, "SEND_SMS Permission required for direct dispatch. Opening SMS app...", Toast.LENGTH_LONG).show()
            launchSmsIntent(contacts, fullMessage)
            return false
        }

        var sentCount = 0

        for (contact in contacts) {
            val rawPhone = contact.phone.trim()
            val cleanPhone = cleanPhoneNumber(rawPhone)

            if (cleanPhone.length >= 3) {
                try {
                    val smsManager = getSmsManager()
                    val parts = smsManager.divideMessage(fullMessage)
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(cleanPhone, null, fullMessage, null, null)
                    }
                    sentCount++
                } catch (e: Exception) {
                    Log.e("SmsHelper", "Failed to send SMS to $cleanPhone via system SmsManager", e)
                    try {
                        @Suppress("DEPRECATION")
                        val defaultSms = SmsManager.getDefault()
                        val parts = defaultSms.divideMessage(fullMessage)
                        if (parts.size > 1) {
                            defaultSms.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                        } else {
                            defaultSms.sendTextMessage(cleanPhone, null, fullMessage, null, null)
                        }
                        sentCount++
                    } catch (e2: Exception) {
                        Log.e("SmsHelper", "Fallback default SmsManager also failed for $cleanPhone", e2)
                    }
                }
            }
        }

        if (sentCount > 0) {
            lastSmsDispatchTime = System.currentTimeMillis()
            Toast.makeText(
                context,
                "Emergency SMS automatically sent to $sentCount contact(s)!",
                Toast.LENGTH_LONG
            ).show()
            return true
        } else {
            Toast.makeText(context, "Direct SMS dispatch failed. Opening SMS app...", Toast.LENGTH_SHORT).show()
            launchSmsIntent(contacts, fullMessage)
            return false
        }
    }

    private fun getSmsManager(): SmsManager {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val sysSmsManager = context.getSystemService(SmsManager::class.java)
                if (sysSmsManager != null) {
                    val defaultSubId = SmsManager.getDefaultSmsSubscriptionId()
                    if (defaultSubId != SubscriptionManager.DEFAULT_SUBSCRIPTION_ID && defaultSubId > 0) {
                        sysSmsManager.createForSubscriptionId(defaultSubId)
                    } else {
                        sysSmsManager
                    }
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    private fun cleanPhoneNumber(phone: String): String {
        var cleaned = phone.replace(Regex("[^0-9+]"), "")
        if (cleaned.startsWith("00")) {
            cleaned = "+" + cleaned.substring(2)
        }
        return cleaned
    }

    private fun launchSmsIntent(contacts: List<Contact>, message: String) {
        val phoneNumbers = contacts.map { cleanPhoneNumber(it.phone) }
            .filter { it.isNotEmpty() }
            .joinToString(";")

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumbers")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch system SMS app.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private var lastSmsDispatchTime = 0L
        private const val SMS_DEBOUNCE_INTERVAL_MS = 10000L
    }
}
