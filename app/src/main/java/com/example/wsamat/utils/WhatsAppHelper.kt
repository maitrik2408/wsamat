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
        photoPath: String?
    ): Boolean {
        val fullMessage = buildString {
            append("🚨 EMERGENCY ALERT! 🚨\n")
            append(customMessage)
            if (mapUrl.isNotEmpty()) {
                append("\n\n📍 Live GPS Location: ")
                append(mapUrl)
            }
        }

        val photoFile = if (!photoPath.isNullOrEmpty()) File(photoPath) else null
        val photoUri: Uri? = if (photoFile != null && photoFile.exists()) {
            try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
            } catch (e: Exception) {
                Log.e("WhatsAppHelper", "Error getting FileProvider URI for photo", e)
                null
            }
        } else {
            null
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            if (photoUri != null) {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, fullMessage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val whatsappInstalled = isPackageInstalled("com.whatsapp") || isPackageInstalled("com.whatsapp.w4b")

        if (whatsappInstalled) {
            val packageName = if (isPackageInstalled("com.whatsapp")) "com.whatsapp" else "com.whatsapp.w4b"
            intent.setPackage(packageName)
            try {
                context.startActivity(intent)
                Toast.makeText(context, "Opening WhatsApp with Emergency Alert & Photo...", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                Log.e("WhatsAppHelper", "Failed to launch WhatsApp directly", e)
            }
        }

        return try {
            val chooser = Intent.createChooser(intent, "Send Emergency Alert & Photo via").apply {
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
