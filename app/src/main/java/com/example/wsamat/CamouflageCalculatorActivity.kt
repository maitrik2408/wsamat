package com.example.wsamat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CamouflageCalculatorActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var tvHistory: TextView

    private var inputBuffer = ""
    private var clearTapCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camouflage_calculator)

        tvDisplay = findViewById(R.id.tvCalcDisplay)
        tvHistory = findViewById(R.id.tvCalcHistory)

        setupButtons()
    }

    private fun setupButtons() {
        val numberButtons = listOf(
            R.id.btnCalc0 to "0", R.id.btnCalc1 to "1", R.id.btnCalc2 to "2",
            R.id.btnCalc3 to "3", R.id.btnCalc4 to "4", R.id.btnCalc5 to "5",
            R.id.btnCalc6 to "6", R.id.btnCalc7 to "7", R.id.btnCalc8 to "8",
            R.id.btnCalc9 to "9", R.id.btnCalcDot to "."
        )

        for ((id, valStr) in numberButtons) {
            findViewById<Button>(id).setOnClickListener {
                clearTapCount = 0
                if (inputBuffer == "0") inputBuffer = ""
                inputBuffer += valStr
                tvDisplay.text = inputBuffer
            }
        }

        val opButtons = listOf(
            R.id.btnCalcPlus to "+", R.id.btnCalcMinus to "-",
            R.id.btnCalcMultiply to "×", R.id.btnCalcDivide to "÷"
        )

        for ((id, op) in opButtons) {
            findViewById<Button>(id).setOnClickListener {
                clearTapCount = 0
                inputBuffer += " $op "
                tvDisplay.text = inputBuffer
            }
        }

        findViewById<Button>(R.id.btnCalcClear).setOnClickListener {
            clearTapCount++
            inputBuffer = ""
            tvDisplay.text = "0"
            tvHistory.text = ""

            if (clearTapCount >= 4) {
                triggerSecretSos()
            }
        }

        findViewById<Button>(R.id.btnCalcEquals).setOnClickListener {
            if (inputBuffer == "9999" || inputBuffer.endsWith("9999")) {
                triggerSecretSos()
                inputBuffer = ""
                tvDisplay.text = "0"
                return@setOnClickListener
            }

            try {
                tvHistory.text = inputBuffer
                val result = evaluateSimpleExpression(inputBuffer)
                tvDisplay.text = result
                inputBuffer = result
            } catch (e: Exception) {
                tvDisplay.text = "Error"
                inputBuffer = ""
            }
        }
    }

    private fun triggerSecretSos() {
        Toast.makeText(this, "Discreet Calculator Emergency SOS Triggered!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SosCountdownActivity::class.java).apply {
            putExtra("TRIGGER_SOURCE", "Camouflage Calculator Secret PIN")
        }
        startActivity(intent)
        finish()
    }

    private fun evaluateSimpleExpression(expr: String): String {
        return try {
            val parts = expr.split(" ")
            if (parts.size == 3) {
                val num1 = parts[0].toDouble()
                val op = parts[1]
                val num2 = parts[2].toDouble()
                val res = when (op) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "×" -> num1 * num2
                    "÷" -> if (num2 != 0.0) num1 / num2 else 0.0
                    else -> 0.0
                }
                if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
            } else {
                "0"
            }
        } catch (e: Exception) {
            "0"
        }
    }
}
