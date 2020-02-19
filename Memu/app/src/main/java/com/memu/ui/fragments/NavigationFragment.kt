package com.memu.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.iapps.gon.etc.callback.NotifyListener

import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox

import com.mapbox.services.android.navigation.ui.v5.NavigationView
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import com.memu.R
import com.memu.callback.SimplifiedCallback
import com.memu.ui.BaseFragment

import retrofit2.Call
import retrofit2.Response

class NavigationFragment : BaseFragment(), OnNavigationReadyCallback, NavigationListener,
    ProgressChangeListener {

    private var navigationView: NavigationView? = null
    var currentRoute: DirectionsRoute? = null
    var alert: LinearLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_embedded_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateNightMode()
        navigationView = view.findViewById(R.id.navigation_view_fragment)
        alert = view.findViewById(R.id.alert)
        navigationView!!.onCreate(savedInstanceState)
        navigationView!!.initialize(this)
        alert?.setOnClickListener {
            showAlertsDialog()
        }
    }

    fun showAlertsDialog() {
        showAlertsDialog(object : NotifyListener {
            override fun onButtonClicked(which: Int) { } }
        )
    }
    override fun onStart() {
        super.onStart()
        navigationView!!.onStart()
    }

    override fun onResume() {
        super.onResume()
        navigationView!!.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navigationView!!.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            navigationView!!.onRestoreInstanceState(savedInstanceState)
        }
    }

    override fun onPause() {
        super.onPause()
        navigationView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        navigationView!!.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        navigationView!!.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        navigationView!!.onDestroy()
    }

    override fun onNavigationReady(isRunning: Boolean) {
        val origin = Point.fromLngLat(ORIGIN_LONGITUDE, ORIGIN_LATITUDE)
        val destination = Point.fromLngLat(DESTINATION_LONGITUDE, DESTINATION_LATITUDE)
        fetchRoute(origin, destination)
    }

    override fun onCancelNavigation() {
        navigationView!!.stopNavigation()
        stopNavigation()
    }

    override fun onNavigationFinished() {
        // no-op
    }

    override fun onNavigationRunning() {
        // no-op
    }

    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
        val isInTunnel = routeProgress.inTunnel()
        val wasInTunnel = wasInTunnel()
        if (isInTunnel) {
            if (!wasInTunnel) {
                updateWasInTunnel(true)
                updateCurrentNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        } else {
            if (wasInTunnel) {
                updateWasInTunnel(false)
                updateCurrentNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
            }
        }
    }

    private fun updateNightMode() {
        if (wasNavigationStopped()) {
            updateWasNavigationStopped(false)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
            activity!!.recreate()
        }
    }

    private fun fetchRoute(origin: Point, destination: Point) {
        NavigationRoute.builder(context)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(origin)
            .destination(destination)
            .build()
            .getRoute(object : SimplifiedCallback() {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    currentRoute = response.body()!!.routes()[0]
                    startNavigation()
                }
            })
    }

    private fun startNavigation() {
        if (currentRoute == null) {
            return
        }
        val options = NavigationViewOptions.builder()
            .directionsRoute(currentRoute)
            .shouldSimulateRoute(false)
            .build()
        navigationView!!.findViewById<View>(R.id.feedbackFab).visibility = View.GONE
        navigationView!!.startNavigation(options)
    }

    private fun stopNavigation() {
        val activity = activity
        /*if (activity != null && activity instanceof FragmentNavigationActivity) {
            FragmentNavigationActivity fragmentNavigationActivity = (FragmentNavigationActivity) activity;
            fragmentNavigationActivity.showPlaceholderFragment();
            fragmentNavigationActivity.showNavigationFab();
            updateWasNavigationStopped(true);
            updateWasInTunnel(false);
        }*/
    }

    private fun wasInTunnel(): Boolean {
        val context = activity
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(context!!.getString(R.string.was_in_tunnel), false)
    }

    private fun updateWasInTunnel(wasInTunnel: Boolean) {
        val context = activity
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putBoolean(context!!.getString(R.string.was_in_tunnel), wasInTunnel)
        editor.apply()
    }

    private fun updateCurrentNightMode(nightMode: Int) {
        AppCompatDelegate.setDefaultNightMode(nightMode)
        activity!!.recreate()
    }

    private fun wasNavigationStopped(): Boolean {
        val context = activity
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(getString(R.string.was_navigation_stopped), false)
    }

    fun updateWasNavigationStopped(wasNavigationStopped: Boolean) {
        val context = activity
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putBoolean(getString(R.string.was_navigation_stopped), wasNavigationStopped)
        editor.apply()
    }

    companion object {

        private val ORIGIN_LONGITUDE = -3.714873
        private val ORIGIN_LATITUDE = 40.397389
        private val DESTINATION_LONGITUDE = -3.712331
        private val DESTINATION_LATITUDE = 40.401686
    }
}
