package com.memu.ui.activity


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle

import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style

import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.instruction.Instruction
import com.mapbox.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine
import com.mapbox.services.android.navigation.v5.milestone.Milestone
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener
import com.mapbox.services.android.navigation.v5.milestone.RouteMilestone
import com.mapbox.services.android.navigation.v5.milestone.Trigger
import com.mapbox.services.android.navigation.v5.milestone.TriggerProperty
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.services.android.navigation.v5.navigation.RefreshCallback
import com.mapbox.services.android.navigation.v5.navigation.RefreshError
import com.mapbox.services.android.navigation.v5.navigation.RouteRefresh
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.memu.R

import java.lang.ref.WeakReference
import java.util.Random

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class MockNavigationActivity : AppCompatActivity(), OnMapReadyCallback,
    MapboxMap.OnMapClickListener, ProgressChangeListener, NavigationEventListener,
    MilestoneEventListener, OffRouteListener, RefreshCallback {


    var newLocationFab: FloatingActionButton? = null

    var startRouteButton: Button? = null

    private var mapboxMap: MapboxMap? = null

    // Navigation related variables
    private var locationEngine: LocationEngine? = null
    private var navigation: MapboxNavigation? = null
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var destination: Point? = null
    private var waypoint: Point? = null
    private var routeRefresh: RouteRefresh? = null
    private var isRefreshing = false
    private var mapView: MapView? = null

    private class MyBroadcastReceiver internal constructor(navigation: MapboxNavigation) :
        BroadcastReceiver() {
        private val weakNavigation: WeakReference<MapboxNavigation>

        init {
            this.weakNavigation = WeakReference(navigation)
        }

        override fun onReceive(context: Context, intent: Intent) {
            val navigation = weakNavigation.get()
            navigation!!.stopNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.map_box_access_token))

        setContentView(R.layout.activity_mock_navigation)
        ButterKnife.bind(this)
        routeRefresh = RouteRefresh(Mapbox.getAccessToken(), this)
        mapView = findViewById(R.id.mapView)
        startRouteButton = findViewById(R.id.startRouteButton)
        mapView = findViewById(R.id.mapView)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)

        val context = applicationContext
        val customNotification = CustomNavigationNotification(context)
        val options = MapboxNavigationOptions.builder()
            .navigationNotification(customNotification)
            .build()

        navigation = MapboxNavigation(this, Mapbox.getAccessToken()!!, options)

        navigation!!.addMilestone(
            RouteMilestone.Builder()
                .setIdentifier(BEGIN_ROUTE_MILESTONE)
                .setInstruction(BeginRouteInstruction())
                .setTrigger(
                    Trigger.all(
                        Trigger.lt(TriggerProperty.STEP_INDEX, 3),
                        Trigger.gt(TriggerProperty.STEP_DISTANCE_TOTAL_METERS, 200),
                        Trigger.gte(TriggerProperty.STEP_DISTANCE_TRAVELED_METERS, 75)
                    )
                ).build()
        )
        customNotification.register(MyBroadcastReceiver(navigation!!), context)
        startRouteButton!!.setOnClickListener { onStartRouteClick() }
//        newLocationFab!!.setOnClickListener { onNewLocationClick() }
    }


    fun onStartRouteClick() {
        val isValidNavigation = navigation != null
        val isValidRoute = route != null && route!!.distance()!! > TWENTY_FIVE_METERS
        if (isValidNavigation && isValidRoute) {

            // Hide the start button
            startRouteButton!!.visibility = View.INVISIBLE

            // Attach all of our navigation listeners.
            navigation!!.addNavigationEventListener(this)
            navigation!!.addProgressChangeListener(this)
            navigation!!.addMilestoneEventListener(this)
            navigation!!.addOffRouteListener(this)
            (locationEngine as ReplayRouteLocationEngine).assign(route)
            navigation!!.locationEngine = locationEngine!!
            mapboxMap!!.locationComponent.isLocationComponentEnabled = true
            navigation!!.startNavigation(route!!)
            mapboxMap!!.removeOnMapClickListener(this)
        }
    }

    fun onNewLocationClick() {
        newOrigin()
    }

    private fun newOrigin() {
        if (mapboxMap != null) {
            val latLng = getRandomLatLng(doubleArrayOf(-77.1825, 38.7825, -76.9790, 39.0157))
            (locationEngine as ReplayRouteLocationEngine).assignLastLocation(
                Point.fromLngLat(latLng.longitude, latLng.latitude)
            )
            mapboxMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0))
        }
    }

    @SuppressLint("MissingPermission", "WrongConstant")
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        this.mapboxMap!!.addOnMapClickListener(this)
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(this, style)
            locationComponent.renderMode = RenderMode.GPS
            locationComponent.isLocationComponentEnabled = false
            navigationMapRoute = NavigationMapRoute(navigation, mapView!!, mapboxMap)
            Snackbar.make(
                findViewById(R.id.container), "Tap map to place waypoint",
                BaseTransientBottomBar.LENGTH_LONG
            ).show()
            locationEngine = ReplayRouteLocationEngine()
            newOrigin()
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        if (destination == null) {
            destination = Point.fromLngLat(point.longitude, point.latitude)
        } else if (waypoint == null) {
            waypoint = Point.fromLngLat(point.longitude, point.latitude)
        } else {
            Toast.makeText(this, "Only 2 waypoints supported", Toast.LENGTH_LONG).show()
        }
        mapboxMap!!.addMarker(MarkerOptions().position(point))
        calculateRoute()
        return false
    }

    @SuppressLint("MissingPermission")
    private fun calculateRoute() {
        locationEngine!!.getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult) {
                findRouteWith(result)
            }

            override fun onFailure(exception: Exception) {
                Timber.e(exception)
            }
        })
    }

    private fun findRouteWith(result: LocationEngineResult) {
        val userLocation = result.lastLocation
        if (userLocation == null) {
            Timber.d("calculateRoute: User location is null, therefore, origin can't be set.")
            return
        }
        val origin = Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        if (TurfMeasurement.distance(origin, destination!!, TurfConstants.UNIT_METERS) < 50) {
            startRouteButton?.visibility = View.GONE
            return
        }

        val navigationRouteBuilder = NavigationRoute.builder(this)
            .accessToken(Mapbox.getAccessToken()!!)
        navigationRouteBuilder.origin(origin)
        navigationRouteBuilder.destination(destination!!)
        if (waypoint != null) {
            navigationRouteBuilder.addWaypoint(waypoint!!)
        }
        navigationRouteBuilder.enableRefresh(true)
        navigationRouteBuilder.build().getRoute(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                Timber.d("Url: %s", call.request().url().toString())
                if (response.body() != null) {
                    if (!response.body()!!.routes().isEmpty()) {
                        this@MockNavigationActivity.route = response.body()!!.routes()[0]
                        navigationMapRoute!!.addRoutes(response.body()!!.routes())
                        startRouteButton?.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                Timber.e(throwable, "onFailure: navigation.getRoute()")
            }
        })
    }

    /*
     * Navigation listeners
     */

    override fun onMilestoneEvent(
        routeProgress: RouteProgress,
        instruction: String,
        milestone: Milestone
    ) {
        Timber.d("Milestone Event Occurred with id: %d", milestone.identifier)
        Timber.d("Voice instruction: %s", instruction)
    }

    override fun onRunning(running: Boolean) {
        if (running) {
            Timber.d("onRunning: Started")
        } else {
            Timber.d("onRunning: Stopped")
        }
    }

    override fun userOffRoute(location: Location) {
        Toast.makeText(this, "off-route called", Toast.LENGTH_LONG).show()
    }

    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
        mapboxMap!!.locationComponent.forceLocationUpdate(location)
        if (!isRefreshing) {
            isRefreshing = true
            routeRefresh!!.refresh(routeProgress)
        }
        Timber.d(
            "onProgressChange: fraction of route traveled: %f",
            routeProgress.fractionTraveled()
        )
    }

    /*
     * Activity lifecycle methods
     */

    public override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigation!!.onDestroy()
        if (mapboxMap != null) {
            mapboxMap!!.removeOnMapClickListener(this)
        }
        mapView!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onRefresh(directionsRoute: DirectionsRoute) {
        navigation!!.startNavigation(directionsRoute)
        isRefreshing = false
    }

    override fun onError(error: RefreshError) {
        isRefreshing = false
    }

    private class BeginRouteInstruction : Instruction() {

        override fun buildInstruction(routeProgress: RouteProgress): String {
            return "Have a safe trip!"
        }
    }

    companion object {

        private val BEGIN_ROUTE_MILESTONE = 1001
        private val TWENTY_FIVE_METERS = 25.0

        fun getRandomLatLng(bbox: DoubleArray): LatLng {
            val random = Random()

            val randomLat = bbox[1] + (bbox[3] - bbox[1]) * random.nextDouble()
            val randomLon = bbox[0] + (bbox[2] - bbox[0]) * random.nextDouble()

            return LatLng(randomLat, randomLon)
        }
    }
}
