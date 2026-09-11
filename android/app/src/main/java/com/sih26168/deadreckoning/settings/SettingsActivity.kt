package com.sih26168.deadreckoning.settings

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sih26168.deadreckoning.R
import com.sih26168.deadreckoning.onboarding.AppPrefs

/**
 * Settings screen. Toggles that have a real counterpart in MainActivity
 * (map-matching snap, accuracy circle, auto-recalibration) are persisted to
 * AppPrefs and read back by MainActivity at startup. The rest (ZUPT
 * sensitivity, distance units, map theme) are stored preferences only --
 * there is no backing subsystem for them yet.
 */
class SettingsActivity : AppCompatActivity() {

    private val zuptLevels = listOf("Low", "Medium", "High")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ReckonAI_Light)
        setContentView(R.layout.activity_settings)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tvMountingYawValue).text =
            "Auto-detected: ${"%.1f".format(AppPrefs.getLastMountingYawDeg(this))}°"
        findViewById<LinearLayout>(R.id.rowMountingYaw).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Mounting yaw calibration")
                .setMessage(
                    "Reckon AI continuously estimates how your phone is rotated relative to the " +
                        "vehicle's forward axis by correlating GPS-speed-derived acceleration with " +
                        "the IMU while driving. Current estimate: ${"%.1f".format(AppPrefs.getLastMountingYawDeg(this))}°.\n\n" +
                        "This updates automatically -- see Auto-recalibration below."
                )
                .setPositiveButton("Got it", null)
                .show()
        }

        val switchAutoRecalibration = findViewById<Switch>(R.id.switchAutoRecalibration)
        switchAutoRecalibration.isChecked = AppPrefs.isAutoRecalibrationEnabled(this)
        switchAutoRecalibration.setOnCheckedChangeListener { _, checked ->
            AppPrefs.setAutoRecalibrationEnabled(this, checked)
        }

        val switchMapMatching = findViewById<Switch>(R.id.switchMapMatching)
        switchMapMatching.isChecked = AppPrefs.isMapMatchingSnapEnabled(this)
        switchMapMatching.setOnCheckedChangeListener { _, checked ->
            AppPrefs.setMapMatchingSnapEnabled(this, checked)
        }

        val switchAccuracyCircle = findViewById<Switch>(R.id.switchAccuracyCircle)
        switchAccuracyCircle.isChecked = AppPrefs.isShowAccuracyCircleEnabled(this)
        switchAccuracyCircle.setOnCheckedChangeListener { _, checked ->
            AppPrefs.setShowAccuracyCircleEnabled(this, checked)
        }

        val tvZuptValue = findViewById<TextView>(R.id.tvZuptValue)
        val prefs = getSharedPreferences("reckon_ai_prefs", MODE_PRIVATE)
        var zuptIndex = prefs.getInt("zupt_sensitivity_index", 1)
        tvZuptValue.text = zuptLevels[zuptIndex]
        findViewById<LinearLayout>(R.id.rowZupt).setOnClickListener {
            zuptIndex = (zuptIndex + 1) % zuptLevels.size
            tvZuptValue.text = zuptLevels[zuptIndex]
            prefs.edit().putInt("zupt_sensitivity_index", zuptIndex).apply()
        }

        findViewById<LinearLayout>(R.id.rowRecalibrate).setOnClickListener {
            Toast.makeText(this, "Open the map screen while driving to recalibrate", Toast.LENGTH_SHORT).show()
        }

        val tvDistanceUnitsValue = findViewById<TextView>(R.id.tvDistanceUnitsValue)
        val unitOptions = arrayOf("Kilometers", "Miles")
        fun refreshUnitsLabel() {
            tvDistanceUnitsValue.text = if (AppPrefs.isDistanceUnitMiles(this)) "Miles" else "Kilometers"
        }
        refreshUnitsLabel()
        findViewById<LinearLayout>(R.id.rowDistanceUnits).setOnClickListener {
            val current = if (AppPrefs.isDistanceUnitMiles(this)) 1 else 0
            AlertDialog.Builder(this)
                .setTitle("Distance units")
                .setSingleChoiceItems(unitOptions, current) { dialog, which ->
                    AppPrefs.setDistanceUnitMiles(this, which == 1)
                    refreshUnitsLabel()
                    dialog.dismiss()
                }
                .show()
        }

        val tvMapThemeValue = findViewById<TextView>(R.id.tvMapThemeValue)
        val themeOptions = arrayOf("System default", "Light", "Dark")
        tvMapThemeValue.text = AppPrefs.getMapTheme(this)
        findViewById<LinearLayout>(R.id.rowMapTheme).setOnClickListener {
            val current = themeOptions.indexOf(AppPrefs.getMapTheme(this)).coerceAtLeast(0)
            AlertDialog.Builder(this)
                .setTitle("Map theme")
                .setSingleChoiceItems(themeOptions, current) { dialog, which ->
                    AppPrefs.setMapTheme(this, themeOptions[which])
                    tvMapThemeValue.text = themeOptions[which]
                    dialog.dismiss()
                }
                .show()
        }

        findViewById<LinearLayout>(R.id.rowEngineInfo).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sensor fusion engine")
                .setMessage(
                    "Reckon AI fuses GPS with a 6-axis IMU (accelerometer + gyroscope) through an " +
                        "Extended Kalman Filter, with ONNX-based correction and velocity models, " +
                        "zero-velocity updates, and lightweight map-matching for heading correction " +
                        "during GNSS outages."
                )
                .setPositiveButton("Got it", null)
                .show()
        }

        findViewById<LinearLayout>(R.id.rowPrivacy).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Privacy")
                .setMessage("All sensor computation and model inference runs on-device. No sensor telemetry leaves this device.")
                .setPositiveButton("Got it", null)
                .show()
        }
    }
}
