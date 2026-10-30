package com.kirtiam.wsamat.data.repository

import com.kirtiam.wsamat.data.Contact
import com.kirtiam.wsamat.data.DatabaseHelper
import com.kirtiam.wsamat.data.model.Result

class ContactRepository(private val dbHelper: DatabaseHelper) {

    fun getAllContacts(): Result<List<Contact>> {
        return try {
            val contacts = dbHelper.getAllContacts()
            Result.Success(contacts)
        } catch (e: Exception) {
            Result.Error(e, "Failed to retrieve contacts from database")
        }
    }

    fun addContact(contact: Contact): Result<Long> {
        return try {
            val id = dbHelper.addContact(contact)
            if (id > 0) Result.Success(id) else Result.Error(Exception("Failed to insert contact"))
        } catch (e: Exception) {
            Result.Error(e, "Failed to add contact")
        }
    }

    fun deleteContact(id: Long): Result<Boolean> {
        return try {
            val success = dbHelper.deleteContact(id)
            if (success) Result.Success(true) else Result.Error(Exception("Failed to delete contact"))
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete contact")
        }
    }

    fun getContactCount(): Int {
        return try {
            dbHelper.getContactCount()
        } catch (e: Exception) {
            0
        }
    }
}
