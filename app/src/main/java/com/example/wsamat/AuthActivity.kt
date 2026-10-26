package com.example.wsamat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.example.wsamat.ui.anim.ViewAnimationUtils
import com.example.wsamat.ui.base.BaseActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AuthActivity : BaseActivity() {

    private lateinit var rgAuthMode: RadioGroup
    private lateinit var rbModeLogin: RadioButton
    private lateinit var rbModeRegister: RadioButton

    private lateinit var layoutLoginForm: LinearLayout
    private lateinit var etLoginEmail: TextInputEditText
    private lateinit var etLoginPassword: TextInputEditText
    private lateinit var btnLoginSubmit: Button

    private lateinit var layoutRegisterForm: LinearLayout
    private lateinit var etRegisterName: TextInputEditText
    private lateinit var etRegisterPhone: TextInputEditText
    private lateinit var etRegisterEmail: TextInputEditText
    private lateinit var etRegisterPassword: TextInputEditText
    private lateinit var btnRegisterSubmit: Button

    private lateinit var btnToggleAuthMode: MaterialButton
    private lateinit var tvAuthSubtitle: TextView

    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        rgAuthMode = findViewById(R.id.rgAuthMode)
        rbModeLogin = findViewById(R.id.rbModeLogin)
        rbModeRegister = findViewById(R.id.rbModeRegister)

        layoutLoginForm = findViewById(R.id.layoutLoginForm)
        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLoginSubmit = findViewById(R.id.btnLoginSubmit)

        layoutRegisterForm = findViewById(R.id.layoutRegisterForm)
        etRegisterName = findViewById(R.id.etRegisterName)
        etRegisterPhone = findViewById(R.id.etRegisterPhone)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit)

        btnToggleAuthMode = findViewById(R.id.btnToggleAuthMode)
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle)

        ViewAnimationUtils.applyTouchScaleAnimation(btnLoginSubmit)
        ViewAnimationUtils.applyTouchScaleAnimation(btnRegisterSubmit)
        ViewAnimationUtils.applyTouchScaleAnimation(btnToggleAuthMode)

        // Pre-fill existing credentials if available
        if (userPrefs.userEmail.isNotEmpty()) {
            etLoginEmail.setText(userPrefs.userEmail)
        }

        // Set initial mode based on whether user is registered
        if (!userPrefs.isRegistered) {
            setAuthMode(isRegister = true)
        } else {
            setAuthMode(isRegister = false)
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        rgAuthMode.setOnCheckedChangeListener { _, checkedId ->
            setAuthMode(isRegister = (checkedId == R.id.rbModeRegister))
        }

        btnToggleAuthMode.setOnClickListener {
            setAuthMode(isRegister = !isRegisterMode)
        }

        btnLoginSubmit.setOnClickListener {
            handleLogin()
        }

        btnRegisterSubmit.setOnClickListener {
            handleRegister()
        }
    }

    private fun setAuthMode(isRegister: Boolean) {
        isRegisterMode = isRegister
        if (isRegister) {
            rbModeRegister.isChecked = true
            layoutLoginForm.visibility = View.GONE
            layoutRegisterForm.visibility = View.VISIBLE
            btnToggleAuthMode.text = "Already have an account? Login Now"
            tvAuthSubtitle.text = "Create Local Account on Your Device"
        } else {
            rbModeLogin.isChecked = true
            layoutRegisterForm.visibility = View.GONE
            layoutLoginForm.visibility = View.VISIBLE
            btnToggleAuthMode.text = "Don't have an account? Register Now"
            tvAuthSubtitle.text = "Secure Local Device Login"
        }
    }

    private fun handleLogin() {
        val email = etLoginEmail.text.toString().trim()
        val password = etLoginPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and password.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!userPrefs.isRegistered) {
            Toast.makeText(this, "No local account found. Please register first.", Toast.LENGTH_LONG).show()
            setAuthMode(isRegister = true)
            return
        }

        val savedEmail = userPrefs.userEmail
        val savedPassword = userPrefs.userPasswordHash

        if (email.equals(savedEmail, ignoreCase = true) && password == savedPassword) {
            userPrefs.isLoggedIn = true
            Toast.makeText(this, "Welcome back, ${userPrefs.userName}!", Toast.LENGTH_SHORT).show()
            navigateTo(MainActivity::class.java, finishCurrent = true)
        } else {
            Toast.makeText(this, "Invalid email or password. Please check your credentials.", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleRegister() {
        val name = etRegisterName.text.toString().trim()
        val phone = etRegisterPhone.text.toString().trim()
        val email = etRegisterEmail.text.toString().trim()
        val password = etRegisterPassword.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required for registration.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 4) {
            Toast.makeText(this, "Password must be at least 4 characters.", Toast.LENGTH_SHORT).show()
            return
        }

        // Save account credentials locally
        userPrefs.userName = name
        userPrefs.userPhone = phone
        userPrefs.userEmail = email
        userPrefs.userPasswordHash = password
        userPrefs.isRegistered = true
        userPrefs.isLoggedIn = true

        Toast.makeText(this, "Account created successfully on local device!", Toast.LENGTH_SHORT).show()
        navigateTo(MainActivity::class.java, finishCurrent = true)
    }
}
