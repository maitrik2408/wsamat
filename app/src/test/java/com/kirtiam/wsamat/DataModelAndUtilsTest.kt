package com.kirtiam.wsamat

import com.kirtiam.wsamat.data.Contact
import com.kirtiam.wsamat.data.Incident
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataModelAndUtilsTest {

    @Test
    fun testContactDataModel() {
        val contact = Contact(id = 1, name = "Alice", phone = "+1234567890", isPrimary = true)
        assertEquals(1L, contact.id)
        assertEquals("Alice", contact.name)
        assertEquals("+1234567890", contact.phone)
        assertTrue(contact.isPrimary)
    }

    @Test
    fun testIncidentDataModel() {
        val incident = Incident(
            id = 10,
            timestamp = 1700000000000L,
            eventType = "1-Tap SOS",
            locationText = "Lat: 28.6139, Lng: 77.2090",
            mapUrl = "https://maps.google.com/?q=28.6139,77.2090",
            status = "Dispatched"
        )
        assertEquals(10L, incident.id)
        assertEquals("1-Tap SOS", incident.eventType)
        assertEquals("Dispatched", incident.status)
        assertTrue(incident.mapUrl.contains("28.6139"))
    }

    @Test
    fun testGoogleMapsUrlGeneration() {
        val lat = 28.6139
        val lng = 77.2090
        val mapUrl = "https://maps.google.com/?q=$lat,$lng"
        assertEquals("https://maps.google.com/?q=28.6139,77.209", mapUrl)
    }

    @Test
    fun testPhoneNumberClean() {
        val rawPhone = "+1 (555) 019-2831"
        val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
        assertEquals("+15550192831", cleanPhone)
        assertFalse(cleanPhone.contains(" "))
    }
}
