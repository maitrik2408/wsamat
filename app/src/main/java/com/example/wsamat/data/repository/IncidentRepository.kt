package com.example.wsamat.data.repository

import com.example.wsamat.data.DatabaseHelper
import com.example.wsamat.data.Incident
import com.example.wsamat.data.model.Result

class IncidentRepository(private val dbHelper: DatabaseHelper) {

    fun getAllIncidents(): Result<List<Incident>> {
        return try {
            val incidents = dbHelper.getAllIncidents()
            Result.Success(incidents)
        } catch (e: Exception) {
            Result.Error(e, "Failed to load incident history")
        }
    }

    fun addIncident(incident: Incident): Result<Long> {
        return try {
            val id = dbHelper.addIncident(incident)
            if (id > 0) Result.Success(id) else Result.Error(Exception("Failed to log incident"))
        } catch (e: Exception) {
            Result.Error(e, "Failed to record incident")
        }
    }

    fun clearIncidents(): Result<Boolean> {
        return try {
            dbHelper.clearIncidents()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e, "Failed to clear incident history")
        }
    }
}
