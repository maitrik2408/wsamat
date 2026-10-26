package com.example.wsamat.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createContactsTable = """
            CREATE TABLE $TABLE_CONTACTS (
                $COL_CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CONTACT_NAME TEXT NOT NULL,
                $COL_CONTACT_PHONE TEXT NOT NULL,
                $COL_CONTACT_IS_PRIMARY INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createIncidentsTable = """
            CREATE TABLE $TABLE_INCIDENTS (
                $COL_INCIDENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_INCIDENT_TIMESTAMP INTEGER NOT NULL,
                $COL_INCIDENT_EVENT_TYPE TEXT NOT NULL,
                $COL_INCIDENT_LOCATION_TEXT TEXT,
                $COL_INCIDENT_MAP_URL TEXT,
                $COL_INCIDENT_STATUS TEXT NOT NULL,
                $COL_INCIDENT_PHOTO_PATH TEXT,
                $COL_INCIDENT_AUDIO_PATH TEXT
            )
        """.trimIndent()

        db.execSQL(createContactsTable)
        db.execSQL(createIncidentsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_INCIDENTS ADD COLUMN $COL_INCIDENT_PHOTO_PATH TEXT")
                db.execSQL("ALTER TABLE $TABLE_INCIDENTS ADD COLUMN $COL_INCIDENT_AUDIO_PATH TEXT")
            } catch (e: Exception) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_INCIDENTS")
                onCreate(db)
            }
        } else {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_INCIDENTS")
            onCreate(db)
        }
    }

    // --- Contacts CRUD Operations ---

    fun addContact(contact: Contact): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CONTACT_NAME, contact.name)
            put(COL_CONTACT_PHONE, contact.phone)
            put(COL_CONTACT_IS_PRIMARY, if (contact.isPrimary) 1 else 0)
        }
        val id = db.insert(TABLE_CONTACTS, null, values)
        db.close()
        return id
    }

    fun updateContact(contact: Contact): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CONTACT_NAME, contact.name)
            put(COL_CONTACT_PHONE, contact.phone)
            put(COL_CONTACT_IS_PRIMARY, if (contact.isPrimary) 1 else 0)
        }
        val rows = db.update(
            TABLE_CONTACTS,
            values,
            "$COL_CONTACT_ID = ?",
            arrayOf(contact.id.toString())
        )
        db.close()
        return rows > 0
    }

    fun deleteContact(id: Long): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_CONTACTS, "$COL_CONTACT_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows > 0
    }

    fun getAllContacts(): List<Contact> {
        val contactsList = mutableListOf<Contact>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CONTACTS,
            null, null, null, null, null,
            "$COL_CONTACT_IS_PRIMARY DESC, $COL_CONTACT_NAME ASC"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIdx = c.getColumnIndexOrThrow(COL_CONTACT_ID)
                val nameIdx = c.getColumnIndexOrThrow(COL_CONTACT_NAME)
                val phoneIdx = c.getColumnIndexOrThrow(COL_CONTACT_PHONE)
                val primaryIdx = c.getColumnIndexOrThrow(COL_CONTACT_IS_PRIMARY)

                do {
                    val contact = Contact(
                        id = c.getLong(idIdx),
                        name = c.getString(nameIdx),
                        phone = c.getString(phoneIdx),
                        isPrimary = c.getInt(primaryIdx) == 1
                    )
                    contactsList.add(contact)
                } while (c.moveToNext())
            }
        }
        db.close()
        return contactsList
    }

    fun getContactCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CONTACTS", null)
        var count = 0
        cursor.use { c ->
            if (c.moveToFirst()) {
                count = c.getInt(0)
            }
        }
        db.close()
        return count
    }

    // --- Incidents CRUD Operations ---

    fun addIncident(incident: Incident): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_INCIDENT_TIMESTAMP, incident.timestamp)
            put(COL_INCIDENT_EVENT_TYPE, incident.eventType)
            put(COL_INCIDENT_LOCATION_TEXT, incident.locationText)
            put(COL_INCIDENT_MAP_URL, incident.mapUrl)
            put(COL_INCIDENT_STATUS, incident.status)
            put(COL_INCIDENT_PHOTO_PATH, incident.photoPath)
            put(COL_INCIDENT_AUDIO_PATH, incident.audioPath)
        }
        val id = db.insert(TABLE_INCIDENTS, null, values)
        db.close()
        return id
    }

    fun getAllIncidents(): List<Incident> {
        val incidents = mutableListOf<Incident>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_INCIDENTS,
            null, null, null, null, null,
            "$COL_INCIDENT_TIMESTAMP DESC"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIdx = c.getColumnIndexOrThrow(COL_INCIDENT_ID)
                val timeIdx = c.getColumnIndexOrThrow(COL_INCIDENT_TIMESTAMP)
                val typeIdx = c.getColumnIndexOrThrow(COL_INCIDENT_EVENT_TYPE)
                val locIdx = c.getColumnIndexOrThrow(COL_INCIDENT_LOCATION_TEXT)
                val mapIdx = c.getColumnIndexOrThrow(COL_INCIDENT_MAP_URL)
                val statusIdx = c.getColumnIndexOrThrow(COL_INCIDENT_STATUS)
                val photoIdx = c.getColumnIndex(COL_INCIDENT_PHOTO_PATH)
                val audioIdx = c.getColumnIndex(COL_INCIDENT_AUDIO_PATH)

                do {
                    val photoPath = if (photoIdx >= 0) c.getString(photoIdx) else null
                    val audioPath = if (audioIdx >= 0) c.getString(audioIdx) else null

                    val incident = Incident(
                        id = c.getLong(idIdx),
                        timestamp = c.getLong(timeIdx),
                        eventType = c.getString(typeIdx),
                        locationText = c.getString(locIdx) ?: "Unknown Location",
                        mapUrl = c.getString(mapIdx) ?: "",
                        status = c.getString(statusIdx),
                        photoPath = photoPath,
                        audioPath = audioPath
                    )
                    incidents.add(incident)
                } while (c.moveToNext())
            }
        }
        db.close()
        return incidents
    }

    fun clearIncidents(): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_INCIDENTS, null, null)
        db.close()
        return rows > 0
    }

    companion object {
        private const val DATABASE_NAME = "wsamat.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_CONTACTS = "contacts"
        private const val COL_CONTACT_ID = "id"
        private const val COL_CONTACT_NAME = "name"
        private const val COL_CONTACT_PHONE = "phone"
        private const val COL_CONTACT_IS_PRIMARY = "is_primary"

        private const val TABLE_INCIDENTS = "incidents"
        private const val COL_INCIDENT_ID = "id"
        private const val COL_INCIDENT_TIMESTAMP = "timestamp"
        private const val COL_INCIDENT_EVENT_TYPE = "event_type"
        private const val COL_INCIDENT_LOCATION_TEXT = "location_text"
        private const val COL_INCIDENT_MAP_URL = "map_url"
        private const val COL_INCIDENT_STATUS = "status"
        private const val COL_INCIDENT_PHOTO_PATH = "photo_path"
        private const val COL_INCIDENT_AUDIO_PATH = "audio_path"
    }
}
