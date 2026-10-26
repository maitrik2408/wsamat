package com.example.wsamat.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wsamat.R
import com.example.wsamat.data.Incident
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidentAdapter(
    private var incidents: List<Incident>,
    private val onMapClick: (Incident) -> Unit,
    private val onAudioClick: (Incident) -> Unit = {},
    private val onWhatsAppClick: (Incident) -> Unit = {}
) : RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder>() {

    class IncidentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tvIncidentType)
        val tvStatus: TextView = itemView.findViewById(R.id.tvIncidentStatus)
        val tvTime: TextView = itemView.findViewById(R.id.tvIncidentTimestamp)
        val tvLocation: TextView = itemView.findViewById(R.id.tvIncidentLocation)
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivCapturedPhoto)
        val btnMap: Button = itemView.findViewById(R.id.btnViewMap)
        val btnAudio: Button = itemView.findViewById(R.id.btnPlayAudio)
        val btnWhatsApp: Button = itemView.findViewById(R.id.btnShareWhatsApp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_incident, parent, false)
        return IncidentViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        val incident = incidents[position]
        holder.tvType.text = incident.eventType
        holder.tvStatus.text = incident.status.uppercase(Locale.getDefault())

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.tvTime.text = dateFormat.format(Date(incident.timestamp))

        holder.tvLocation.text = incident.locationText

        if (!incident.photoPath.isNullOrEmpty() && File(incident.photoPath).exists()) {
            val bitmap = BitmapFactory.decodeFile(incident.photoPath)
            holder.ivPhoto.setImageBitmap(bitmap)
            holder.ivPhoto.visibility = View.VISIBLE
        } else {
            holder.ivPhoto.visibility = View.GONE
        }

        if (!incident.audioPath.isNullOrEmpty() && File(incident.audioPath).exists()) {
            holder.btnAudio.visibility = View.VISIBLE
            holder.btnAudio.setOnClickListener { onAudioClick(incident) }
        } else {
            holder.btnAudio.visibility = View.GONE
        }

        if (incident.mapUrl.isNotEmpty()) {
            holder.btnMap.visibility = View.VISIBLE
            holder.btnMap.setOnClickListener { onMapClick(incident) }
        } else {
            holder.btnMap.visibility = View.GONE
        }

        holder.btnWhatsApp.setOnClickListener { onWhatsAppClick(incident) }
    }

    override fun getItemCount(): Int = incidents.size

    fun updateData(newIncidents: List<Incident>) {
        this.incidents = newIncidents
        notifyDataSetChanged()
    }
}
