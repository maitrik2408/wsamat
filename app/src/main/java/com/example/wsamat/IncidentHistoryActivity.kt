package com.example.wsamat

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wsamat.adapter.IncidentAdapter
import com.example.wsamat.data.Incident
import com.example.wsamat.data.model.Result
import com.example.wsamat.ui.base.BaseActivity
import com.example.wsamat.utils.WhatsAppHelper
import java.io.File

class IncidentHistoryActivity : BaseActivity() {

    private lateinit var adapter: IncidentAdapter
    private lateinit var rvIncidents: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var whatsAppHelper: WhatsAppHelper

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incident_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Incident History & Evidence"

        whatsAppHelper = WhatsAppHelper(this)

        rvIncidents = findViewById(R.id.rvIncidents)
        tvEmpty = findViewById(R.id.tvEmptyHistory)

        rvIncidents.layoutManager = LinearLayoutManager(this)
        adapter = IncidentAdapter(
            incidents = emptyList(),
            onMapClick = { incident -> openMapUrl(incident.mapUrl) },
            onAudioClick = { incident -> playIncidentAudio(incident) },
            onWhatsAppClick = { incident -> shareIncidentWhatsApp(incident) }
        )
        rvIncidents.adapter = adapter

        findViewById<Button>(R.id.btnClearHistory).setOnClickListener {
            showClearConfirmDialog()
        }

        loadHistory()
    }

    private fun loadHistory() {
        val result = incidentRepository.getAllIncidents()
        val incidents = if (result is Result.Success) result.data else emptyList()
        adapter.updateData(incidents)

        if (incidents.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvIncidents.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvIncidents.visibility = View.VISIBLE
        }
    }

    private fun openMapUrl(mapUrl: String) {
        if (mapUrl.isEmpty()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open map link.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playIncidentAudio(incident: Incident) {
        val audioPath = incident.audioPath
        if (!audioPath.isNullOrEmpty() && File(audioPath).exists()) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioPath)
                    prepare()
                    start()
                }
                Toast.makeText(this, "Playing recorded emergency audio evidence...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to play audio recording.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Audio recording file not found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareIncidentWhatsApp(incident: Incident) {
        val contacts = contactRepository.getAllContacts().let {
            if (it is Result.Success) it.data else emptyList()
        }
        val note = "Incident Log: ${incident.eventType} - ${incident.locationText}"
        whatsAppHelper.sendEmergencyWhatsApp(contacts, note, incident.mapUrl, incident.photoPath, incident.audioPath)
    }

    private fun showClearConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all recorded incident logs?")
            .setPositiveButton("Clear All") { _, _ ->
                incidentRepository.clearIncidents()
                Toast.makeText(this, "Incident log cleared", Toast.LENGTH_SHORT).show()
                loadHistory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onPause() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
