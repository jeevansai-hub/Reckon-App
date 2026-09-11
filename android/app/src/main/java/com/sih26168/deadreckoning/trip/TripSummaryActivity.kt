package com.sih26168.deadreckoning.trip

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sih26168.deadreckoning.R
import com.sih26168.deadreckoning.onboarding.AppPrefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Post-trip stats screen. Reads the snapshot MainActivity persists via
 * AppPrefs.saveTripStats() -- see MainActivity.persistTripStatsSnapshot().
 * "Start New Trip" sets a one-shot flag MainActivity consumes in onResume()
 * to reset its running counters; "End Trip" just flips the active flag so
 * this screen freezes into a "trip ended" summary state.
 */
class TripSummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ReckonAI_Light)
        setContentView(R.layout.activity_trip_summary)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnStartEndTrip).setOnClickListener { onStartEndTripClicked() }
    }

    override fun onResume() {
        super.onResume()
        renderTripStatusCard()
        renderStats()
        renderOutageEvents()
    }

    private fun onStartEndTripClicked() {
        if (AppPrefs.isTripActive(this)) {
            AppPrefs.setTripActive(this, false)
        } else {
            AppPrefs.requestNewTrip(this)
            AppPrefs.setTripActive(this, true)
            // A fresh trip starts with a clean outage-event history -- last
            // trip's GPS-loss cycles shouldn't bleed into this one's list.
            AppPrefs.clearOutageEvents(this)
        }
        renderTripStatusCard()
        renderOutageEvents()
    }

    /**
     * Fills the "GPS Outage Events" section with one card per Simulate GPS
     * Loss -> Restore GPS Signal cycle (AppPrefs.getOutageEvents(), most
     * recent first). Rebuilds the list each call since it's cheap and this
     * only runs on resume/trip-toggle, not on a hot path.
     */
    private fun renderOutageEvents() {
        val container = findViewById<LinearLayout>(R.id.llOutageEventsList)
        val emptyView = findViewById<View>(R.id.tvNoOutageEvents)
        container.removeAllViews()

        val events = AppPrefs.getOutageEvents(this)
        if (events.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            container.visibility = View.GONE
            return
        }
        emptyView.visibility = View.GONE
        container.visibility = View.VISIBLE

        val miles = AppPrefs.isDistanceUnitMiles(this)
        val unitLabel = if (miles) "mi" else "km"
        fun metersToUnitStr(m: Float): String {
            val v = if (miles) m / 1609.344 else m / 1000.0
            return "%.3f %s".format(v, unitLabel)
        }
        val timeFmt = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())

        for (event in events) {
            val item = layoutInflater.inflate(R.layout.item_outage_event, container, false)
            item.findViewById<TextView>(R.id.tvEventTimestamp).text = timeFmt.format(Date(event.startTimeMs))
            item.findViewById<TextView>(R.id.tvEventDuration).text = "%.1fs".format(event.durationMs / 1000.0)
            item.findViewById<TextView>(R.id.tvEventGpsKm).text = metersToUnitStr(event.gpsDistanceM)
            item.findViewById<TextView>(R.id.tvEventAiKm).text = metersToUnitStr(event.aiDistanceM)
            item.findViewById<TextView>(R.id.tvEventDeltaM).text =
                "%.0f m".format(abs(event.aiDistanceM - event.gpsDistanceM))
            item.findViewById<TextView>(R.id.tvEventStartCoord).text =
                "Start: %.6f, %.6f".format(event.startLat, event.startLon)
            item.findViewById<TextView>(R.id.tvEventGpsEndCoord).text =
                "GPS End: %.6f, %.6f".format(event.endGpsLat, event.endGpsLon)
            item.findViewById<TextView>(R.id.tvEventAiEndCoord).text =
                "AI-DR End: %.6f, %.6f".format(event.endAiLat, event.endAiLon)
            item.findViewById<TextView>(R.id.tvEventCorrection).text =
                "Correction distance: %.1f m".format(event.correctionDistanceM)
            container.addView(item)
        }
    }

    private fun renderTripStatusCard() {
        val active = AppPrefs.isTripActive(this)
        findViewById<TextView>(R.id.tvTripStatus).text = if (active) "Trip in progress" else "No active trip"
        findViewById<TextView>(R.id.tvTripStatusSubtitle).text = if (active) {
            "Recording distance, outages & drift on the live map"
        } else {
            "Start a trip to begin recording stats"
        }
        findViewById<Button>(R.id.btnStartEndTrip).apply {
            text = if (active) "End Trip" else "Start New Trip"
            setBackgroundResource(if (active) R.drawable.bg_pill_outline_blue else R.drawable.bg_pill_blue)
            setTextColor(resources.getColor(if (active) R.color.gm_primary_dark else android.R.color.white, theme))
        }
    }

    private fun renderStats() {
        val stats = AppPrefs.getTripStats(this)
        if (!stats.hasData) {
            findViewById<View>(R.id.tvEmptyState).visibility = View.VISIBLE
            findViewById<View>(R.id.contentGroup).visibility = View.GONE
            return
        }
        findViewById<View>(R.id.tvEmptyState).visibility = View.GONE
        findViewById<View>(R.id.contentGroup).visibility = View.VISIBLE

        val miles = AppPrefs.isDistanceUnitMiles(this)
        val unitLabel = if (miles) "mi" else "km"
        fun metersToUnit(m: Float) = if (miles) m / 1609.344 else m / 1000.0

        val totalInUnit = metersToUnit(stats.totalDistanceM)
        val durationMin = (stats.durationMs / 60000.0).roundToInt()
        val outageSeconds = stats.outageDurationMs / 1000.0
        val gpsDistanceM = max(0f, stats.totalDistanceM - stats.outageDistanceM)
        val onGpsPct = if (stats.durationMs > 0) {
            100.0 * (stats.durationMs - stats.outageDurationMs) / stats.durationMs
        } else 100.0

        findViewById<TextView>(R.id.tvTotalDistance).text = "%.1f %s".format(totalInUnit, unitLabel)
        findViewById<TextView>(R.id.tvDuration).text = "$durationMin min"
        findViewById<TextView>(R.id.tvOutageTime).text = if (outageSeconds >= 60) {
            "%d min %d s".format((outageSeconds / 60).toInt(), (outageSeconds % 60).toInt())
        } else {
            "%.0f s".format(outageSeconds)
        }
        findViewById<TextView>(R.id.tvOnGpsPct).text = "%.1f%%".format(onGpsPct.coerceIn(0.0, 100.0))
        findViewById<TextView>(R.id.tvAvgDrift).text = "%.1f m".format(stats.lastDriftM)

        val tvAccuracy = findViewById<TextView>(R.id.tvAccuracyLabel)
        val accuracyLabel: String
        val accuracyColor: Int
        when {
            stats.lastDriftM < 5f -> { accuracyLabel = "Excellent"; accuracyColor = R.color.gm_success_green }
            stats.lastDriftM < 15f -> { accuracyLabel = "Good"; accuracyColor = R.color.gm_primary_dark }
            else -> { accuracyLabel = "Fair"; accuracyColor = R.color.gm_dr_orange }
        }
        tvAccuracy.text = accuracyLabel
        tvAccuracy.setTextColor(resources.getColor(accuracyColor, theme))

        val orangeFraction = if (stats.totalDistanceM > 0) {
            (stats.outageDistanceM / stats.totalDistanceM).coerceIn(0f, 1f)
        } else 0f
        val orangeSegment = findViewById<View>(R.id.coverageOrangeSegment)
        orangeSegment.post {
            val parentWidth = (orangeSegment.parent as View).width
            orangeSegment.layoutParams.width = (parentWidth * orangeFraction).toInt().coerceAtLeast(0)
            orangeSegment.requestLayout()
        }
        findViewById<TextView>(R.id.tvCoverageCaption).text =
            "%.1f %s GPS-tracked · %.1f %s AI dead-reckoning".format(
                metersToUnit(gpsDistanceM), unitLabel, metersToUnit(stats.outageDistanceM), unitLabel
            )

        findViewById<Button>(R.id.btnExport).setOnClickListener {
            val summary = buildString {
                appendLine("Reckon AI - Trip Summary")
                appendLine("Total distance: %.2f %s".format(totalInUnit, unitLabel))
                appendLine("Duration: $durationMin min")
                appendLine("GPS outage time: %.0f s".format(outageSeconds))
                appendLine("Trip on GPS: %.1f%%".format(onGpsPct))
                appendLine("Avg drift during outage: %.1f m".format(stats.lastDriftM))
                appendLine("Overall accuracy: $accuracyLabel")

                // Full per-cycle breakdown -- same numbers as the "GPS Outage
                // Events" cards on this screen, so the exported text and the
                // on-screen list can never disagree.
                val events = AppPrefs.getOutageEvents(this@TripSummaryActivity)
                appendLine()
                appendLine("GPS Outage Events (${events.size}):")
                if (events.isEmpty()) {
                    appendLine("  None recorded this trip.")
                } else {
                    val eventTimeFmt = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault())
                    events.forEachIndexed { idx, event ->
                        appendLine("  #${idx + 1} - ${eventTimeFmt.format(Date(event.startTimeMs))} (${"%.1f".format(event.durationMs / 1000.0)}s)")
                        appendLine("     Real GPS path: %.3f %s".format(metersToUnit(event.gpsDistanceM), unitLabel))
                        appendLine("     AI-DR path:    %.3f %s".format(metersToUnit(event.aiDistanceM), unitLabel))
                        appendLine("     Path delta:    %.1f m".format(abs(event.aiDistanceM - event.gpsDistanceM)))
                        appendLine("     Start:         %.6f, %.6f".format(event.startLat, event.startLon))
                        appendLine("     GPS end:       %.6f, %.6f".format(event.endGpsLat, event.endGpsLon))
                        appendLine("     AI-DR end:     %.6f, %.6f".format(event.endAiLat, event.endAiLon))
                        appendLine("     Correction:    %.1f m".format(event.correctionDistanceM))
                    }
                }
            }
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Reckon AI Trip Summary")
                putExtra(Intent.EXTRA_TEXT, summary)
            }
            startActivity(Intent.createChooser(sendIntent, "Export trip data"))
        }
    }
}
