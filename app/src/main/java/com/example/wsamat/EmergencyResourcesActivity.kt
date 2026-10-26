package com.example.wsamat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EmergencyResourcesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_resources)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Emergency Resources"

        findViewById<Button>(R.id.btnCall112).setOnClickListener { dialNumber("112") }
        findViewById<Button>(R.id.btnCall1091).setOnClickListener { dialNumber("1091") }
        findViewById<Button>(R.id.btnCall100).setOnClickListener { dialNumber("100") }
        findViewById<Button>(R.id.btnCall102).setOnClickListener { dialNumber("102") }
        findViewById<Button>(R.id.btnCall1930).setOnClickListener { dialNumber("1930") }
    }

    private fun dialNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to launch phone dialer.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
