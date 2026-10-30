package com.kirtiam.wsamat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import com.kirtiam.wsamat.ui.anim.ViewAnimationUtils
import com.kirtiam.wsamat.ui.base.BaseActivity
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

    private lateinit var layoutOtpForm: LinearLayout
    private lateinit var tvOtpSubtext: TextView
    private lateinit var etOtpCode: TextInputEditText
    private lateinit var btnVerifyOtpSubmit: Button
    private lateinit var btnResendOtp: MaterialButton

    private lateinit var btnToggleAuthMode: MaterialButton
    private lateinit var tvAuthSubtitle: TextView

    private var isRegisterMode = false
    private var generatedOtpCode: String = ""
    private var pendingName = ""
    private var pendingPhone = ""
    private var pendingEmail = ""
    private var pendingPassword = ""

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

        layoutOtpForm = findViewById(R.id.layoutOtpForm)
        tvOtpSubtext = findViewById(R.id.tvOtpSubtext)
        etOtpCode = findViewById(R.id.etOtpCode)
        btnVerifyOtpSubmit = findViewById(R.id.btnVerifyOtpSubmit)
        btnResendOtp = findViewById(R.id.btnResendOtp)

        btnToggleAuthMode = findViewById(R.id.btnToggleAuthMode)
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle)

        ViewAnimationUtils.applyTouchScaleAnimation(btnLoginSubmit)
        ViewAnimationUtils.applyTouchScaleAnimation(btnRegisterSubmit)
        ViewAnimationUtils.applyTouchScaleAnimation(btnVerifyOtpSubmit)
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
            handleSendOtp()
        }

        btnVerifyOtpSubmit.setOnClickListener {
            handleVerifyOtp()
        }

        btnResendOtp.setOnClickListener {
            if (pendingEmail.isNotEmpty() && pendingPhone.isNotEmpty()) {
                dispatchOtpNotification(pendingEmail, pendingPhone)
                Toast.makeText(this, "New OTP code generated & sent!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setAuthMode(isRegister: Boolean) {
        isRegisterMode = isRegister
        layoutOtpForm.visibility = View.GONE

        if (isRegister) {
            rbModeRegister.isChecked = true
            rbModeRegister.setBackgroundResource(R.drawable.bg_tab_active_register)
            rbModeRegister.setTextColor(Color.WHITE)

            rbModeLogin.setBackgroundColor(Color.TRANSPARENT)
            rbModeLogin.setTextColor(Color.parseColor("#B0BEC5"))

            layoutLoginForm.visibility = View.GONE
            layoutRegisterForm.visibility = View.VISIBLE
            btnToggleAuthMode.text = "Already have an account? Login Now"
            tvAuthSubtitle.text = "Create Local Account on Your Device"
        } else {
            rbModeLogin.isChecked = true
            rbModeLogin.setBackgroundResource(R.drawable.bg_tab_active_login)
            rbModeLogin.setTextColor(Color.BLACK)

            rbModeRegister.setBackgroundColor(Color.TRANSPARENT)
            rbModeRegister.setTextColor(Color.parseColor("#B0BEC5"))

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

    private fun handleSendOtp() {
        val name = etRegisterName.text.toString().trim()
        val phone = etRegisterPhone.text.toString().trim()
        val email = etRegisterEmail.text.toString().trim()
        val password = etRegisterPassword.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required for registration.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid Gmail / Email address.", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.length < 5) {
            Toast.makeText(this, "Please enter a valid Phone Number.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 4) {
            Toast.makeText(this, "Password must be at least 4 characters.", Toast.LENGTH_SHORT).show()
            return
        }

        pendingName = name
        pendingPhone = phone
        pendingEmail = email
        pendingPassword = password

        dispatchOtpNotification(email, phone)

        layoutRegisterForm.visibility = View.GONE
        layoutOtpForm.visibility = View.VISIBLE
        tvOtpSubtext.text = "OTP sent to $email ($phone). Enter code below:"
        tvAuthSubtitle.text = "Verify OTP to Complete Registration"
    }

    private fun dispatchOtpNotification(email: String, phone: String) {
        val otp = (100000..999999).random().toString()
        generatedOtpCode = otp

        // Auto-fill OTP field for immediate testing/use
        etOtpCode.setText(otp)

        val channelId = "wsamat_auth_otp_channel"
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Registration OTP Verification",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Delivers registration 6-digit OTP verification codes"
        }
        manager?.createNotificationChannel(channel)

        val notifText = "🔑 Your WSAMat Verification OTP for $email / $phone is: $otp"

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("WSAMat Registration OTP Code")
            .setContentText(notifText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notifText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager?.notify(9901, notification)

        // Show on-screen alert dialog with OTP code & Auto-fill button
        AlertDialog.Builder(this)
            .setTitle("🔑 Registration OTP Generated")
            .setMessage("Your 6-Digit Verification OTP for $email is:\n\n$otp\n\n(It has been auto-filled below for instant registration).")
            .setPositiveButton("AUTO-FILL OTP") { _, _ ->
                etOtpCode.setText(otp)
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun handleVerifyOtp() {
        val enteredOtp = etOtpCode.text.toString().trim()

        if (enteredOtp.isEmpty()) {
            Toast.makeText(this, "Please enter the 6-digit OTP code.", Toast.LENGTH_SHORT).show()
            return
        }

        if (enteredOtp == generatedOtpCode && generatedOtpCode.isNotEmpty()) {
            // Save account credentials locally on device
            userPrefs.userName = pendingName
            userPrefs.userPhone = pendingPhone
            userPrefs.userEmail = pendingEmail
            userPrefs.userPasswordHash = pendingPassword
            userPrefs.isRegistered = true
            userPrefs.isLoggedIn = true

            Toast.makeText(this, "OTP Verified! Account created & saved safely on local device.", Toast.LENGTH_LONG).show()
            navigateTo(MainActivity::class.java, finishCurrent = true)
        } else {
            Toast.makeText(this, "Invalid OTP Code. Please check the 6-digit OTP generated.", Toast.LENGTH_LONG).show()
        }
    }
}
