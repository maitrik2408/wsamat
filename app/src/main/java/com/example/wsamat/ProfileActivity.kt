package com.example.wsamat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wsamat.service.LockScreenSosService
import com.example.wsamat.ui.anim.ViewAnimationUtils
import com.example.wsamat.ui.base.BaseActivity
import com.example.wsamat.utils.AppIconManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : BaseActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmergencyNote: TextInputEditText

    private lateinit var rgAppIconDisguise: RadioGroup
    private lateinit var rbIconDefault: RadioButton
    private lateinit var rbIconCalculator: RadioButton
    private lateinit var rbIconCompass: RadioButton
    private lateinit var rbIconClock: RadioButton

    private lateinit var rgThemeMode: RadioGroup
    private lateinit var rbThemeSystem: RadioButton
    private lateinit var rbThemeLight: RadioButton
    private lateinit var rbThemeDark: RadioButton

    private lateinit var switchLockScreenSos: SwitchMaterial
    private lateinit var rgLockScreenMode: RadioGroup
    private lateinit var rbInstantSos: RadioButton
    private lateinit var rbCountdownSos: RadioButton
    private lateinit var etLockScreenButtonText: TextInputEditText
    private lateinit var btnSaveLockScreenSettings: MaterialButton

    private lateinit var tvLocationStatus: TextView
    private lateinit var tvSmsStatus: TextView
    private lateinit var tvCameraStatus: TextView
    private lateinit var tvAudioStatus: TextView
    private lateinit var tvContactsStatus: TextView
    private lateinit var tvCallStatus: TextView
    private lateinit var tvNotificationStatus: TextView

    private lateinit var btnGrantLocation: Button
    private lateinit var btnGrantSms: Button
    private lateinit var btnGrantCamera: Button
    private lateinit var btnGrantAudio: Button
    private lateinit var btnGrantContacts: Button
    private lateinit var btnGrantCall: Button
    private lateinit var btnGrantNotification: Button

    private var isBindingData = false

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { updatePermissionStatuses() }

    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionStatuses() }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionStatuses() }

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionStatuses() }

    private val requestContactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionStatuses() }

    private val requestCallPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionStatuses() }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionStatuses() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "User Profile & Setup"

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etEmergencyNote = findViewById(R.id.etEmergencyNote)

        rgAppIconDisguise = findViewById(R.id.rgAppIconDisguise)
        rbIconDefault = findViewById(R.id.rbIconDefault)
        rbIconCalculator = findViewById(R.id.rbIconCalculator)
        rbIconCompass = findViewById(R.id.rbIconCompass)
        rbIconClock = findViewById(R.id.rbIconClock)

        rgThemeMode = findViewById(R.id.rgThemeMode)
        rbThemeSystem = findViewById(R.id.rbThemeSystem)
        rbThemeLight = findViewById(R.id.rbThemeLight)
        rbThemeDark = findViewById(R.id.rbThemeDark)

        switchLockScreenSos = findViewById(R.id.switchLockScreenSos)
        rgLockScreenMode = findViewById(R.id.rgLockScreenMode)
        rbInstantSos = findViewById(R.id.rbInstantSos)
        rbCountdownSos = findViewById(R.id.rbCountdownSos)
        etLockScreenButtonText = findViewById(R.id.etLockScreenButtonText)
        btnSaveLockScreenSettings = findViewById(R.id.btnSaveLockScreenSettings)

        tvLocationStatus = findViewById(R.id.tvLocationStatus)
        tvSmsStatus = findViewById(R.id.tvSmsStatus)
        tvCameraStatus = findViewById(R.id.tvCameraStatus)
        tvAudioStatus = findViewById(R.id.tvAudioStatus)
        tvContactsStatus = findViewById(R.id.tvContactsStatus)
        tvCallStatus = findViewById(R.id.tvCallStatus)
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus)

        btnGrantLocation = findViewById(R.id.btnGrantLocation)
        btnGrantSms = findViewById(R.id.btnGrantSms)
        btnGrantCamera = findViewById(R.id.btnGrantCamera)
        btnGrantAudio = findViewById(R.id.btnGrantAudio)
        btnGrantContacts = findViewById(R.id.btnGrantContacts)
        btnGrantCall = findViewById(R.id.btnGrantCall)
        btnGrantNotification = findViewById(R.id.btnGrantNotification)

        setupListeners()
        loadProfileData()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatuses()
    }

    private fun loadProfileData() {
        isBindingData = true
        try {
            etName.setText(userPrefs.userName)
            etPhone.setText(userPrefs.userPhone)
            etEmergencyNote.setText(userPrefs.emergencyNote)

            when (AppIconManager.getSavedIconMode(this)) {
                AppIconManager.IconMode.CALCULATOR -> rbIconCalculator.isChecked = true
                AppIconManager.IconMode.COMPASS -> rbIconCompass.isChecked = true
                AppIconManager.IconMode.CLOCK -> rbIconClock.isChecked = true
                else -> rbIconDefault.isChecked = true
            }

            when (userPrefs.themeMode) {
                "LIGHT" -> rbThemeLight.isChecked = true
                "DARK" -> rbThemeDark.isChecked = true
                else -> rbThemeSystem.isChecked = true
            }

            switchLockScreenSos.isChecked = userPrefs.isLockScreenSosEnabled
            if (userPrefs.lockScreenSosMode == "COUNTDOWN") {
                rbCountdownSos.isChecked = true
            } else {
                rbInstantSos.isChecked = true
            }
            etLockScreenButtonText.setText(userPrefs.lockScreenButtonText)
        } finally {
            isBindingData = false
        }
    }

    private fun setupListeners() {
        val btnPrivacy = findViewById<MaterialButton>(R.id.btnPrivacyPolicy)
        ViewAnimationUtils.applyTouchScaleAnimation(btnPrivacy)
        btnPrivacy.setOnClickListener {
            navigateTo(PrivacyPolicyActivity::class.java)
        }

        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        ViewAnimationUtils.applyTouchScaleAnimation(btnLogout)
        btnLogout.setOnClickListener {
            userPrefs.isLoggedIn = false
            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
            navigateTo(AuthActivity::class.java, finishCurrent = true)
        }

        rgAppIconDisguise.setOnCheckedChangeListener { _, checkedId ->
            if (isBindingData) return@setOnCheckedChangeListener
            val targetMode = when (checkedId) {
                R.id.rbIconCalculator -> AppIconManager.IconMode.CALCULATOR
                R.id.rbIconCompass -> AppIconManager.IconMode.COMPASS
                R.id.rbIconClock -> AppIconManager.IconMode.CLOCK
                else -> AppIconManager.IconMode.DEFAULT
            }
            AppIconManager.applyIconMode(this, targetMode)
            Toast.makeText(this, "App icon disguised as ${targetMode.labelName}. Launcher will update shortly.", Toast.LENGTH_LONG).show()
        }

        rgThemeMode.setOnCheckedChangeListener { _, checkedId ->
            if (isBindingData) return@setOnCheckedChangeListener
            val chosenTheme = when (checkedId) {
                R.id.rbThemeLight -> "LIGHT"
                R.id.rbThemeDark -> "DARK"
                else -> "SYSTEM"
            }
            userPrefs.themeMode = chosenTheme

            when (chosenTheme) {
                "LIGHT" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "DARK" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            userPrefs.userName = etName.text.toString().trim()
            userPrefs.userPhone = etPhone.text.toString().trim()
            userPrefs.emergencyNote = etEmergencyNote.text.toString().trim()
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }

        btnSaveLockScreenSettings.setOnClickListener {
            userPrefs.isLockScreenSosEnabled = switchLockScreenSos.isChecked
            userPrefs.lockScreenSosMode = if (rbCountdownSos.isChecked) "COUNTDOWN" else "INSTANT"
            val buttonText = etLockScreenButtonText.text.toString().trim()
            userPrefs.lockScreenButtonText = buttonText.ifEmpty { "🚨 DIRECT SOS MESSAGE" }

            if (userPrefs.isLockScreenSosEnabled) {
                LockScreenSosService.startService(this)
            } else {
                LockScreenSosService.stopService(this)
            }

            Toast.makeText(this, "Lock screen SOS settings saved!", Toast.LENGTH_SHORT).show()
        }

        btnGrantLocation.setOnClickListener {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        btnGrantSms.setOnClickListener {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                openAppSettings()
            } else {
                requestSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }
        }

        btnGrantCamera.setOnClickListener {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                openAppSettings()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnGrantAudio.setOnClickListener {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                openAppSettings()
            } else {
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        btnGrantContacts.setOnClickListener {
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        btnGrantCall.setOnClickListener {
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }

        btnGrantNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Toast.makeText(this, "Notification permission granted automatically on this Android version.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
            Toast.makeText(this, "Please allow permissions in App Permissions settings.", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to open app settings automatically.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePermissionStatuses() {
        val hasLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        tvLocationStatus.text = if (hasLocation) "Status: Granted ✓" else "Status: Denied ✗"
        btnGrantLocation.isEnabled = !hasLocation

        val hasSms = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        tvSmsStatus.text = if (hasSms) "Status: Granted ✓" else "Status: Denied ✗"
        btnGrantSms.isEnabled = !hasSms

        val hasCamera = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        tvCameraStatus.text = if (hasCamera) "Status: Granted ✓" else "Status: Denied ✗"
        btnGrantCamera.isEnabled = !hasCamera

        val hasAudio = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        tvAudioStatus.text = if (hasAudio) "Status: Granted ✓" else "Status: Denied ✗"
        btnGrantAudio.isEnabled = !hasAudio

        val hasContacts = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        tvContactsStatus.text = if (hasContacts) "Status: Granted ✓" else "Status: Denied ✗"
        btnGrantContacts.isEnabled = !hasContacts

        val hasCall = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        tvCallStatus.text = if (hasCall) "Status: Granted ✓" else "Status: Denied ✗"
        btnGrantCall.isEnabled = !hasCall

        val hasNotification = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        tvNotificationStatus.text = if (hasNotification) "Status: Granted ✓" else "Status: Denied ✗"
        btnGrantNotification.isEnabled = !hasNotification
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
