package com.sih26168.deadreckoning.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sih26168.deadreckoning.R
import com.sih26168.deadreckoning.ui.MainActivity

/**
 * App entry point / launcher activity. Shows the boot checklist briefly,
 * then routes to onboarding (first launch only) or straight to the live map.
 */
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DELAY_MS = 1600L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ReckonAI_Light)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isFinishing) return@postDelayed
            val next = if (AppPrefs.isOnboardingComplete(this)) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(next)
            finish()
        }, SPLASH_DELAY_MS)
    }
}
