package com.example.wsamat.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wsamat.data.DatabaseHelper
import com.example.wsamat.data.UserPreferences
import com.example.wsamat.data.repository.ContactRepository
import com.example.wsamat.data.repository.IncidentRepository
import com.example.wsamat.ui.anim.ViewAnimationUtils
import com.example.wsamat.ui.theme.ThemeManager

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var dbHelper: DatabaseHelper
    protected lateinit var userPrefs: UserPreferences
    protected lateinit var contactRepository: ContactRepository
    protected lateinit var incidentRepository: IncidentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DatabaseHelper(this)
        userPrefs = UserPreferences(this)
        contactRepository = ContactRepository(dbHelper)
        incidentRepository = IncidentRepository(dbHelper)

        ThemeManager.applyTheme(userPrefs)
    }

    protected fun navigateTo(targetClass: Class<*>, finishCurrent: Boolean = false, bundleBlock: (Intent.() -> Unit)? = null) {
        val intent = Intent(this, targetClass)
        bundleBlock?.invoke(intent)
        startActivity(intent)
        ViewAnimationUtils.applyActivityTransition(this)
        if (finishCurrent) finish()
    }

    override fun finish() {
        super.finish()
        ViewAnimationUtils.applyActivityTransition(this)
    }
}
