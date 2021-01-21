package com.memu.ui.fragments

import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.iapps.gon.etc.callback.NotifyListener

import com.mapbox.api.directions.v5.models.DirectionsRoute

import com.mapbox.services.android.navigation.ui.v5.NavigationView
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import com.memu.R
import com.memu.modules.mapFeeds.MapFeed
import com.memu.ui.BaseFragment
import com.memu.webservices.PostMApFeedDataViewModel
import com.memu.webservices.PostGetAlertListViewModel
import com.memu.webservices.PostMApFeedAddViewModel
import com.memu.webservices.PostStartNavigationViewModel
import kotlinx.android.synthetic.main.activity_embedded_navigation.*

class NavigationFragment : BaseFragment(), OnNavigationReadyCallback, NavigationListener,
    ProgressChangeListener {

    private var navigationView: NavigationView? = null
    var currentRoute: DirectionsRoute? = null
    var alert: LinearLayout? = null
    lateinit var postGetAlertListViewModel: PostGetAlertListViewModel
    lateinit var postMApFeedDataViewModel: PostMApFeedDataViewModel
    lateinit var postMApFeedAddViewModel: PostMApFeedAddViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_embedded_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateNightMode()
        setGetAlertsAPIObserver()
        setMapFeedsDataAPIObserver()
        setAddAlertAPIObserver()
        navigationView = view.findViewById(R.id.navigation_view_fragment)
        alert = view.findViewById(R.id.alert)
        navigationView!!.onCreate(savedInstanceState)
        navigationView!!.initialize(this)
        postGetAlertListViewModel.loadData()
        postMApFeedDataViewModel.loadData()
        alert?.setOnClickListener {
            showAlertsDialog()
        }
    }

    private fun updateNightMode() {
        if (wasNavigationStopped()) {
            updateWasNavigationStopped(false)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
            activity!!.recreate()
        }
    }

    fun showAlertsDialog() {
        System.out.println("showAlertsDialog map_feeds "+postGetAlertListViewModel.obj?.map_feeds!!)
        showAlertsDialog(postGetAlertListViewModel.obj?.map_feeds!! as ArrayList<MapFeed> ,object : NotifyListener {
            override fun onButtonClicked(which: Int) {
                postMApFeedAddViewModel.loadData(postGetAlertListViewModel.obj?.map_feeds!!.get(which).id)
            } }
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
        startNavigation(currentRoute!!)
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


    private fun startNavigation(directionsRoute: DirectionsRoute) {
        val options = NavigationViewOptions.builder()
            .navigationListener(this)
            .directionsRoute(directionsRoute)

            .shouldSimulateRoute(false)


        navigationView?.findViewById<View>(R.id.feedbackFab)?.visibility = View.GONE
        navigationView?.startNavigation(options.build())
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

    fun setGetAlertsAPIObserver() {
        postGetAlertListViewModel = ViewModelProviders.of(this).get(PostGetAlertListViewModel::class.java).apply {
            this@NavigationFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostGetAlertListViewModel.NEXT_STEP -> {

                        }
                    }
                })
            }
        }
    }
    fun setMapFeedsDataAPIObserver() {
        postMApFeedDataViewModel = ViewModelProviders.of(this).get(PostMApFeedDataViewModel::class.java).apply {
            this@NavigationFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostMApFeedDataViewModel.NEXT_STEP -> {

                        }
                    }
                })
            }
        }
    }
    fun setAddAlertAPIObserver() {
        postMApFeedAddViewModel = ViewModelProviders.of(this).get(PostMApFeedAddViewModel::class.java).apply {
            this@NavigationFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostMApFeedAddViewModel.NEXT_STEP -> {

                        }
                    }
                })
            }
        }
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


}
