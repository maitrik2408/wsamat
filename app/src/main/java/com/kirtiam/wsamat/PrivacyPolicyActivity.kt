package com.kirtiam.wsamat

import android.os.Bundle
import com.kirtiam.wsamat.ui.base.BaseActivity

class PrivacyPolicyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Privacy Policy & Disclosures"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
