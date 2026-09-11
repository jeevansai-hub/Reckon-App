package com.sih26168.deadreckoning.onboarding

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small SharedPreferences wrapper for flags/values that need to survive
 * across the onboarding flow, the live map screen, and Settings/Trip Summary
 * (which run in separate Activities and can't share MainActivity's fields).
 */
object AppPrefs {
    private const val PREFS_NAME = "reckon_ai_prefs"

    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val KEY_SHOW_ACCURACY_CIRCLE = "show_accuracy_circle"
    private const val KEY_MAP_MATCHING_SNAP = "map_matching_snap"
    private const val KEY_AUTO_RECALIBRATION = "auto_recalibration"
    private const val KEY_LAST_MOUNTING_YAW_DEG = "last_mounting_yaw_deg"

    private const val KEY_TRIP_DISTANCE_M = "trip_total_distance_m"
    private const val KEY_TRIP_DURATION_MS = "trip_duration_ms"
    private const val KEY_TRIP_OUTAGE_DURATION_MS = "trip_outage_duration_ms"
    private const val KEY_TRIP_OUTAGE_DISTANCE_M = "trip_outage_distance_m"
    private const val KEY_TRIP_LAST_DRIFT_M = "trip_last_drift_m"
    private const val KEY_TRIP_HAS_DATA = "trip_has_data"
    private const val KEY_TRIP_ACTIVE = "trip_active"
    private const val KEY_TRIP_START_REQUESTED = "trip_start_requested"
    private const val KEY_DISTANCE_UNIT_MILES = "distance_unit_miles"
    private const val KEY_MAP_THEME = "map_theme"

    private const val KEY_OUTAGE_EVENTS_JSON = "outage_events_json"
    // Bounds SharedPreferences growth -- each event is a handful of doubles,
    // 100 is generous for a single trip's worth of GPS-loss cycles.
    private const val MAX_STORED_OUTAGE_EVENTS = 100

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingComplete(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingComplete(context: Context, complete: Boolean) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()
    }

    fun isShowAccuracyCircleEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_ACCURACY_CIRCLE, true)

    fun setShowAccuracyCircleEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_ACCURACY_CIRCLE, enabled).apply()
    }

    fun isMapMatchingSnapEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MAP_MATCHING_SNAP, true)

    fun setMapMatchingSnapEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MAP_MATCHING_SNAP, enabled).apply()
    }

    fun isAutoRecalibrationEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_RECALIBRATION, true)

    fun setAutoRecalibrationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_RECALIBRATION, enabled).apply()
    }

    fun getLastMountingYawDeg(context: Context): Float =
        prefs(context).getFloat(KEY_LAST_MOUNTING_YAW_DEG, 0f)

    fun setLastMountingYawDeg(context: Context, degrees: Float) {
        prefs(context).edit().putFloat(KEY_LAST_MOUNTING_YAW_DEG, degrees).apply()
    }

    data class TripStats(
        val totalDistanceM: Float,
        val durationMs: Long,
        val outageDurationMs: Long,
        val outageDistanceM: Float,
        val lastDriftM: Float,
        val hasData: Boolean
    )

    fun saveTripStats(
        context: Context,
        totalDistanceM: Float,
        durationMs: Long,
        outageDurationMs: Long,
        outageDistanceM: Float,
        lastDriftM: Float
    ) {
        prefs(context).edit()
            .putFloat(KEY_TRIP_DISTANCE_M, totalDistanceM)
            .putLong(KEY_TRIP_DURATION_MS, durationMs)
            .putLong(KEY_TRIP_OUTAGE_DURATION_MS, outageDurationMs)
            .putFloat(KEY_TRIP_OUTAGE_DISTANCE_M, outageDistanceM)
            .putFloat(KEY_TRIP_LAST_DRIFT_M, lastDriftM)
            .putBoolean(KEY_TRIP_HAS_DATA, true)
            .apply()
    }

    fun getTripStats(context: Context): TripStats {
        val p = prefs(context)
        return TripStats(
            totalDistanceM = p.getFloat(KEY_TRIP_DISTANCE_M, 0f),
            durationMs = p.getLong(KEY_TRIP_DURATION_MS, 0L),
            outageDurationMs = p.getLong(KEY_TRIP_OUTAGE_DURATION_MS, 0L),
            outageDistanceM = p.getFloat(KEY_TRIP_OUTAGE_DISTANCE_M, 0f),
            lastDriftM = p.getFloat(KEY_TRIP_LAST_DRIFT_M, 0f),
            hasData = p.getBoolean(KEY_TRIP_HAS_DATA, false)
        )
    }

    /** Whether a trip is currently "active" per the Start/End Trip flow in TripSummaryActivity. */
    fun isTripActive(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TRIP_ACTIVE, false)

    fun setTripActive(context: Context, active: Boolean) {
        prefs(context).edit().putBoolean(KEY_TRIP_ACTIVE, active).apply()
    }

    /**
     * One-shot flag: TripSummaryActivity's "Start New Trip" button sets this;
     * MainActivity checks it in onResume() and, if set, resets its running
     * distance/duration/outage counters for a fresh trip.
     */
    fun requestNewTrip(context: Context) {
        prefs(context).edit().putBoolean(KEY_TRIP_START_REQUESTED, true).apply()
    }

    fun consumeNewTripRequest(context: Context): Boolean {
        val p = prefs(context)
        val requested = p.getBoolean(KEY_TRIP_START_REQUESTED, false)
        if (requested) p.edit().putBoolean(KEY_TRIP_START_REQUESTED, false).apply()
        return requested
    }

    fun isDistanceUnitMiles(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DISTANCE_UNIT_MILES, false)

    fun setDistanceUnitMiles(context: Context, miles: Boolean) {
        prefs(context).edit().putBoolean(KEY_DISTANCE_UNIT_MILES, miles).apply()
    }

    fun getMapTheme(context: Context): String =
        prefs(context).getString(KEY_MAP_THEME, "System default") ?: "System default"

    fun setMapTheme(context: Context, theme: String) {
        prefs(context).edit().putString(KEY_MAP_THEME, theme).apply()
    }

    /**
     * One full "Simulate GPS Loss" -> "Restore GPS Signal" cycle, as recorded
     * by MainActivity.toggleGpsOutageMode()'s exit branch. gpsDistanceM/
     * aiDistanceM are path lengths (sum of consecutive-fix/sample distances),
     * not point-to-point separation -- see outageGpsDistanceTraveledM's doc
     * comment in MainActivity for why that distinction matters here.
     */
    data class OutageEvent(
        val startTimeMs: Long,
        val endTimeMs: Long,
        val durationMs: Long,
        val startLat: Double,
        val startLon: Double,
        val endGpsLat: Double,
        val endGpsLon: Double,
        val endAiLat: Double,
        val endAiLon: Double,
        val gpsDistanceM: Float,
        val aiDistanceM: Float,
        val correctionDistanceM: Float
    )

    /** Appends one completed outage event, trimming to the oldest MAX_STORED_OUTAGE_EVENTS if needed. */
    fun addOutageEvent(context: Context, event: OutageEvent) {
        val p = prefs(context)
        val arr = JSONArray(p.getString(KEY_OUTAGE_EVENTS_JSON, "[]"))
        arr.put(JSONObject().apply {
            put("startTimeMs", event.startTimeMs)
            put("endTimeMs", event.endTimeMs)
            put("durationMs", event.durationMs)
            put("startLat", event.startLat)
            put("startLon", event.startLon)
            put("endGpsLat", event.endGpsLat)
            put("endGpsLon", event.endGpsLon)
            put("endAiLat", event.endAiLat)
            put("endAiLon", event.endAiLon)
            put("gpsDistanceM", event.gpsDistanceM)
            put("aiDistanceM", event.aiDistanceM)
            put("correctionDistanceM", event.correctionDistanceM)
        })
        val trimmed = if (arr.length() > MAX_STORED_OUTAGE_EVENTS) {
            JSONArray().apply {
                for (i in (arr.length() - MAX_STORED_OUTAGE_EVENTS) until arr.length()) put(arr.get(i))
            }
        } else arr
        p.edit().putString(KEY_OUTAGE_EVENTS_JSON, trimmed.toString()).apply()
    }

    /** Most recent first. */
    fun getOutageEvents(context: Context): List<OutageEvent> {
        val arr = JSONArray(prefs(context).getString(KEY_OUTAGE_EVENTS_JSON, "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            OutageEvent(
                startTimeMs = o.getLong("startTimeMs"),
                endTimeMs = o.getLong("endTimeMs"),
                durationMs = o.getLong("durationMs"),
                startLat = o.getDouble("startLat"),
                startLon = o.getDouble("startLon"),
                endGpsLat = o.getDouble("endGpsLat"),
                endGpsLon = o.getDouble("endGpsLon"),
                endAiLat = o.getDouble("endAiLat"),
                endAiLon = o.getDouble("endAiLon"),
                gpsDistanceM = o.getDouble("gpsDistanceM").toFloat(),
                aiDistanceM = o.getDouble("aiDistanceM").toFloat(),
                correctionDistanceM = o.getDouble("correctionDistanceM").toFloat()
            )
        }.sortedByDescending { it.startTimeMs }
    }

    fun clearOutageEvents(context: Context) {
        prefs(context).edit().remove(KEY_OUTAGE_EVENTS_JSON).apply()
    }
}
