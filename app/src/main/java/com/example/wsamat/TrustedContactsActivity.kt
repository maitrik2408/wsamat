package com.example.wsamat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wsamat.adapter.ContactAdapter
import com.example.wsamat.data.Contact
import com.example.wsamat.data.DatabaseHelper
import com.google.android.material.button.MaterialButton

class TrustedContactsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ContactAdapter
    private lateinit var rvContacts: RecyclerView
    private lateinit var tvEmpty: TextView

    private val requestContactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchPhonePicker()
        } else {
            Toast.makeText(this, "Contacts permission is required to pick contacts from phonebook.", Toast.LENGTH_LONG).show()
        }
    }

    private val pickPhoneContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            if (contactUri != null) {
                processPickedPhoneUri(contactUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trusted_contacts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Trusted Contacts"

        dbHelper = DatabaseHelper(this)

        rvContacts = findViewById(R.id.rvContacts)
        tvEmpty = findViewById(R.id.tvEmptyContacts)

        rvContacts.layoutManager = LinearLayoutManager(this)
        adapter = ContactAdapter(
            contacts = emptyList(),
            onDeleteClick = { contact -> showDeleteConfirmDialog(contact) },
            onCallClick = { contact -> dialContact(contact.phone) },
            onItemClick = { contact -> showAddOrEditContactDialog(contact) }
        )
        rvContacts.adapter = adapter

        findViewById<MaterialButton>(R.id.btnAddManual).setOnClickListener {
            showAddOrEditContactDialog(null)
        }

        findViewById<MaterialButton>(R.id.btnPickFromContacts).setOnClickListener {
            checkAndPickContact()
        }

        loadContacts()
    }

    private fun checkAndPickContact() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchPhonePicker()
        } else {
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun launchPhonePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            pickPhoneContactLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open phonebook contacts picker.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processPickedPhoneUri(uri: Uri) {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    val rawName = if (nameIdx >= 0) cursor.getString(nameIdx) else "Contact"
                    val rawPhone = if (numberIdx >= 0) cursor.getString(numberIdx) else ""

                    val name = if (rawName.isNullOrBlank()) "Contact" else rawName.trim()
                    val phone = cleanPhoneNumber(rawPhone)

                    if (phone.isNotEmpty()) {
                        val contact = Contact(name = name, phone = phone, isPrimary = dbHelper.getAllContacts().isEmpty())
                        dbHelper.addContact(contact)
                        Toast.makeText(this, "Added $name ($phone)!", Toast.LENGTH_SHORT).show()
                        loadContacts()
                    } else {
                        Toast.makeText(this, "No valid phone number found in selected contact.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to read contact details from phonebook.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadContacts() {
        val contacts = dbHelper.getAllContacts()
        adapter.updateData(contacts)
        if (contacts.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvContacts.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvContacts.visibility = View.VISIBLE
        }
    }

    private fun dialContact(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to launch dialer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddOrEditContactDialog(existingContact: Contact?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val etName = dialogView.findViewById<EditText>(R.id.etDialogName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etDialogPhone)
        val cbPrimary = dialogView.findViewById<CheckBox>(R.id.cbDialogPrimary)

        if (existingContact != null) {
            etName.setText(existingContact.name)
            etPhone.setText(existingContact.phone)
            cbPrimary.isChecked = existingContact.isPrimary
        }

        val title = if (existingContact == null) "Add Trusted Contact" else "Edit Contact"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val rawPhone = etPhone.text.toString().trim()
                val isPrimary = cbPrimary.isChecked

                if (name.isEmpty() || rawPhone.isEmpty()) {
                    Toast.makeText(this, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    val phone = cleanPhoneNumber(rawPhone)
                    if (phone.isEmpty() || !isValidPhoneNumber(phone)) {
                        Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                    } else {
                        if (existingContact == null) {
                            val contact = Contact(name = name, phone = phone, isPrimary = isPrimary)
                            dbHelper.addContact(contact)
                            Toast.makeText(this, "Contact added!", Toast.LENGTH_SHORT).show()
                        } else {
                            val contact = existingContact.copy(name = name, phone = phone, isPrimary = isPrimary)
                            dbHelper.updateContact(contact)
                            Toast.makeText(this, "Contact updated!", Toast.LENGTH_SHORT).show()
                        }
                        loadContacts()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle("Remove Contact")
            .setMessage("Remove ${contact.name} from trusted emergency contacts?")
            .setPositiveButton("Remove") { _, _ ->
                dbHelper.deleteContact(contact.id)
                Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show()
                loadContacts()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun cleanPhoneNumber(phone: String): String {
        val clean = phone.replace(Regex("[^0-9+]"), "")
        return if (clean.startsWith("00")) "+" + clean.substring(2) else clean
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        return cleanPhone.length >= 4
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
