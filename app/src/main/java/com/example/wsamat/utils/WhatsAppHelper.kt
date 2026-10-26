package com.example.wsamat.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.wsamat.data.Contact
import java.io.File

class WhatsAppHelper(private val context: Context) {

    fun sendEmergencyWhatsApp(
        contacts: List<Contact>,
        customMessage: String,
        mapUrl: String,
        photoPath: String?,
        audioPath: String? = null
    ): Boolean {
        val fullMessage = buildString {
            append("🚨 EMERGENCY ALERT! 🚨\n")
            append(customMessage)
            if (mapUrl.isNotEmpty()) {
                append("\n\n📍 Live GPS Location: ")
                append(mapUrl)
            }
        }

        val mediaUris = ArrayList<Uri>()

        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    mediaUris.add(uri)
                } catch (e: Exception) {
                    Log.e("WhatsAppHelper", "Error getting FileProvider URI for photo", e)
                }
            }
        }

        if (!audioPath.isNullOrEmpty()) {
            val file = File(audioPath)
            if (file.exists()) {
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    mediaUris.add(uri)
                } catch (e: Exception) {
                    Log.e("WhatsAppHelper", "Error getting FileProvider URI for audio", e)
                }
            }
        }

        val intent = if (mediaUris.size > 1) {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, mediaUris)
                putExtra(Intent.EXTRA_TEXT, fullMessage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else if (mediaUris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = if (!photoPath.isNullOrEmpty()) "image/*" else "audio/*"
                putExtra(Intent.EXTRA_STREAM, mediaUris[0])
                putExtra(Intent.EXTRA_TEXT, fullMessage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, fullMessage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        val whatsappInstalled = isPackageInstalled("com.whatsapp") || isPackageInstalled("com.whatsapp.w4b")

        if (whatsappInstalled) {
            val packageName = if (isPackageInstalled("com.whatsapp")) "com.whatsapp" else "com.whatsapp.w4b"
            intent.setPackage(packageName)
            try {
                context.startActivity(intent)
                Toast.makeText(context, "Opening WhatsApp with Emergency Alert & Evidence...", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                Log.e("WhatsAppHelper", "Failed to launch WhatsApp directly", e)
            }
        }

        return try {
            val chooser = Intent.createChooser(intent, "Send Emergency Alert & Evidence via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share via WhatsApp or messaging app.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun sendDirectWhatsAppToPhone(
        phone: String,
        message: String,
        mapUrl: String
    ): Boolean {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val fullMessage = if (mapUrl.isNotEmpty()) "$message\nLocation: $mapUrl" else message

        return try {
            val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(fullMessage)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open WhatsApp for $cleanPhone", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
