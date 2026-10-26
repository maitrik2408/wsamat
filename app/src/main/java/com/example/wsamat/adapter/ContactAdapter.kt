package com.example.wsamat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wsamat.R
import com.example.wsamat.data.Contact

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onDeleteClick: (Contact) -> Unit,
    private val onCallClick: (Contact) -> Unit,
    private val onItemClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvContactPhone)
        val tvPrimaryBadge: TextView = itemView.findViewById(R.id.tvPrimaryBadge)
        val btnCall: ImageButton = itemView.findViewById(R.id.btnCallContactDirect)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteContact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phone
        holder.tvPrimaryBadge.visibility = if (contact.isPrimary) View.VISIBLE else View.GONE

        holder.btnCall.setOnClickListener {
            onCallClick(contact)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(contact)
        }

        holder.itemView.setOnClickListener {
            onItemClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateData(newContacts: List<Contact>) {
        this.contacts = newContacts
        notifyDataSetChanged()
    }
}
