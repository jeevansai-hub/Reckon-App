package com.sih26168.deadreckoning.onboarding

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.sih26168.deadreckoning.R
import com.sih26168.deadreckoning.ui.MainActivity

/**
 * Google-style rationale screen shown once before the real Android runtime
 * permission dialogs. MainActivity re-checks/re-requests these on its own
 * (see checkLocationPermissionsAndStart), so it's safe to proceed to it
 * regardless of what the user picks here.
 */
class PermissionsActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ReckonAI_Light)
        setContentView(R.layout.activity_permissions)

        val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        findViewById<Button>(R.id.btnAllowAll).setOnClickListener {
            ActivityCompat.requestPermissions(this, locationPermissions, REQUEST_CODE)
        }
        findViewById<Button>(R.id.btnWhileUsing).setOnClickListener {
            ActivityCompat.requestPermissions(this, locationPermissions, REQUEST_CODE)
        }
        findViewById<Button>(R.id.btnNotNow).setOnClickListener {
            finishOnboarding()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        AppPrefs.setOnboardingComplete(this, true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
