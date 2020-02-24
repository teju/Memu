package com.memu.ui.activity

import android.content.SharedPreferences
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager

import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.TextView

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.services.android.navigation.ui.v5.NavigationView
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback
import com.mapbox.services.android.navigation.ui.v5.listeners.BannerInstructionsListener
import com.mapbox.services.android.navigation.ui.v5.listeners.InstructionListListener
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import com.mapbox.services.android.navigation.ui.v5.listeners.SpeechAnnouncementListener
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechAnnouncement
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import com.memu.R
import com.memu.callback.SimplifiedCallback

import retrofit2.Call
import retrofit2.Response

class EmbeddedNavigationActivity : AppCompatActivity(), OnNavigationReadyCallback,
    NavigationListener, ProgressChangeListener, InstructionListListener, SpeechAnnouncementListener,
    BannerInstructionsListener {

    private var navigationView: NavigationView? = null
    private var spacer: View? = null
    private var speedWidget: TextView? = null
    private val fabNightModeToggle: FloatingActionButton? = null

    private var bottomSheetVisible = true
    private var instructionListShown = false

    private val currentNightMode: Int
        get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar)
        initNightMode()
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.map_box_access_token))

        setContentView(R.layout.activity_embedded_navigation)
        navigationView = findViewById(R.id.navigation_view_fragment)
        //fabNightModeToggle = findViewById(R.id.fabToggleNightMode);
        speedWidget = findViewById(R.id.speed_limit)
        spacer = findViewById(R.id.spacer)
        setSpeedWidgetAnchor(R.id.summaryBottomSheet)

        val initialPosition = CameraPosition.Builder()
            .target(LatLng(ORIGIN.latitude(), ORIGIN.longitude()))
            .zoom(INITIAL_ZOOM.toDouble())
            .build()
        navigationView!!.onCreate(savedInstanceState)
        navigationView!!.initialize(this, initialPosition)
    }

    override fun onNavigationReady(isRunning: Boolean) {
        fetchRoute()
    }

    public override fun onStart() {
        super.onStart()
        navigationView!!.onStart()
    }

    public override fun onResume() {
        super.onResume()
        navigationView!!.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        navigationView!!.onLowMemory()
    }

    override fun onBackPressed() {
        // If the navigation view didn't need to do anything, call super
        if (!navigationView!!.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navigationView!!.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        navigationView!!.onRestoreInstanceState(savedInstanceState)
    }

    public override fun onPause() {
        super.onPause()
        navigationView!!.onPause()
    }

    public override fun onStop() {
        super.onStop()
        navigationView!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigationView!!.onDestroy()
        if (isFinishing) {
            saveNightModeToPreferences(AppCompatDelegate.MODE_NIGHT_AUTO)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        }
    }

    override fun onCancelNavigation() {
        // Navigation canceled, finish the activity
        finish()
    }

    override fun onNavigationFinished() {
        // Intentionally empty
    }

    override fun onNavigationRunning() {
        // Intentionally empty
    }

    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
        setSpeed(location)
    }

    override fun onInstructionListVisibilityChanged(shown: Boolean) {
        instructionListShown = shown
        speedWidget!!.visibility = if (shown) View.GONE else View.VISIBLE
        if (instructionListShown) {
            fabNightModeToggle!!.hide()
        } else if (bottomSheetVisible) {
            fabNightModeToggle!!.show()
        }
    }

    override fun willVoice(announcement: SpeechAnnouncement): SpeechAnnouncement {
        return SpeechAnnouncement.builder().announcement("All announcements will be the same.")
            .build()
    }

    override fun willDisplay(instructions: BannerInstructions): BannerInstructions {
        return instructions
    }

    private fun startNavigation(directionsRoute: DirectionsRoute) {
        val options = NavigationViewOptions.builder()
            .navigationListener(this)
            .directionsRoute(directionsRoute)
            .shouldSimulateRoute(true)
            .progressChangeListener(this)
            .instructionListListener(this)
            .speechAnnouncementListener(this)
            .waynameChipEnabled(false)
            .bannerInstructionsListener(this)
        setBottomSheetCallback(options)
        setupNightModeFab()
        navigationView!!.findViewById<View>(R.id.feedbackFab).visibility = View.GONE
        navigationView!!.startNavigation(options.build())
    }

    private fun fetchRoute() {
        NavigationRoute.builder(this)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(ORIGIN)
            .destination(DESTINATION)
            .alternatives(true)
            .build()
            .getRoute(object : SimplifiedCallback() {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    val directionsRoute = response.body()!!.routes()[0]
                    startNavigation(directionsRoute)
                }
            })
    }

    /**
     * Sets the anchor of the spacer for the speed widget, thus setting the anchor for the speed widget
     * (The speed widget is anchored to the spacer, which is there because padding between items and
     * their anchors in CoordinatorLayouts is finicky.
     *
     * @param res resource for view of which to anchor the spacer
     */
    private fun setSpeedWidgetAnchor(@IdRes res: Int) {
        val layoutParams = spacer!!.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.anchorId = res
        spacer!!.layoutParams = layoutParams
    }

    private fun setBottomSheetCallback(options: NavigationViewOptions.Builder) {
        options.bottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        bottomSheetVisible = false
                        fabNightModeToggle!!.hide()
                        setSpeedWidgetAnchor(R.id.recenterBtn)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> bottomSheetVisible = true
                    BottomSheetBehavior.STATE_SETTLING -> if (!bottomSheetVisible) {
                        // View needs to be anchored to the bottom sheet before it is finished expanding
                        // because of the animation
                        fabNightModeToggle!!.show()
                        setSpeedWidgetAnchor(R.id.summaryBottomSheet)
                    }
                    else -> return
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun setupNightModeFab() {
        //        fabNightModeToggle.setOnClickListener(view -> toggleNightMode());
    }

    private fun toggleNightMode() {
        val currentNightMode = currentNightMode
        alternateNightMode(currentNightMode)
    }

    private fun initNightMode() {
        val nightMode = retrieveNightModeFromPreferences()
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun alternateNightMode(currentNightMode: Int) {
        val newNightMode: Int
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            newNightMode = AppCompatDelegate.MODE_NIGHT_NO
        } else {
            newNightMode = AppCompatDelegate.MODE_NIGHT_YES
        }
        saveNightModeToPreferences(newNightMode)
        recreate()
    }

    private fun retrieveNightModeFromPreferences(): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getInt(
            getString(R.string.current_night_mode),
            AppCompatDelegate.MODE_NIGHT_AUTO
        )
    }

    private fun saveNightModeToPreferences(nightMode: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putInt(getString(R.string.current_night_mode), nightMode)
        editor.apply()
    }

    private fun setSpeed(location: Location) {
        val string = String.format("%d\nMPH", (location.speed * 2.2369).toInt())
        val mphTextSize = resources.getDimensionPixelSize(R.dimen.mph_text_size)
        val speedTextSize = resources.getDimensionPixelSize(R.dimen.speed_text_size)

        val spannableString = SpannableString(string)
        spannableString.setSpan(
            AbsoluteSizeSpan(mphTextSize),
            string.length - 4, string.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        spannableString.setSpan(
            AbsoluteSizeSpan(speedTextSize),
            0, string.length - 3, Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        speedWidget!!.text = spannableString
        if (!instructionListShown) {
            speedWidget!!.visibility = View.VISIBLE
        }
    }

    companion object {

        private val ORIGIN = Point.fromLngLat(-77.03194990754128, 38.909664963450105)
        private val DESTINATION = Point.fromLngLat(-77.0270025730133, 38.91057077063121)
        private val INITIAL_ZOOM = 16
    }
}
