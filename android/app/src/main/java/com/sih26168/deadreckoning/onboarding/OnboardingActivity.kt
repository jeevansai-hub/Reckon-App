package com.sih26168.deadreckoning.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.sih26168.deadreckoning.R

/**
 * Three-slide first-run explainer: GPS+IMU fusion -> AI dead reckoning ->
 * works through tunnels/outages. Shown once; see [AppPrefs.isOnboardingComplete].
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var flipper: ViewFlipper
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView
    private lateinit var dots: List<android.view.View>
    private var pageIndex = 0
    private val pageCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ReckonAI_Light)
        setContentView(R.layout.activity_onboarding)

        flipper = findViewById(R.id.onboardingFlipper)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        dots = listOf(findViewById(R.id.dot1), findViewById(R.id.dot2), findViewById(R.id.dot3))

        tvSkip.setOnClickListener { goToPermissions() }
        btnNext.setOnClickListener {
            if (pageIndex == pageCount - 1) {
                goToPermissions()
            } else {
                pageIndex++
                flipper.showNext()
                updatePageState()
            }
        }
        updatePageState()
    }

    private fun updatePageState() {
        dots.forEachIndexed { i, dot ->
            dot.setBackgroundResource(if (i == pageIndex) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        }
        val isLast = pageIndex == pageCount - 1
        tvSkip.visibility = if (isLast) android.view.View.INVISIBLE else android.view.View.VISIBLE
        btnNext.text = if (isLast) "Get Started" else "Next"
    }

    private fun goToPermissions() {
        startActivity(Intent(this, PermissionsActivity::class.java))
        finish()
    }
}
