package com.memu.ui.activity


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

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
import com.memu.etc.GPSTracker

import java.lang.ref.WeakReference
import java.util.Random

import com.memu.ui.BaseFragment
import com.memu.ui.fragments.MapFragment
import com.memu.webservices.PostGetAlertListViewModel
import com.memu.webservices.PostMApFeedAddViewModel
import com.memu.webservices.PostMApFeedDataViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_mock_navigation.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.net.URL

class MockNavigationFragment(
    var desrpoint: Point,
    var originpoint: Point) : BaseFragment(), OnMapReadyCallback, ProgressChangeListener, NavigationEventListener,
    MilestoneEventListener, OffRouteListener, RefreshCallback {
    var currentRoute: DirectionsRoute? = null
    private var locationComponent: LocationComponent? = null
    private var permissionsManager: PermissionsManager? = null

    var startRouteButton: Button? = null

    private var mapboxMap: MapboxMap? = null
    lateinit var postGetAlertListViewModel: PostGetAlertListViewModel
    lateinit var postMApFeedDataViewModel: PostMApFeedDataViewModel
    lateinit var postMApFeedAddViewModel: PostMApFeedAddViewModel

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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(com.memu.R.layout.activity_mock_navigation, container, false)
        return v
    }

    fun showAlertsDialog() {
        System.out.println("showAlertsDialog map_feeds "+postGetAlertListViewModel.obj?.map_feeds!!)
        showAlertsDialog(postGetAlertListViewModel.obj?.map_feeds!!,object : NotifyListener {
            override fun onButtonClicked(which: Int) {
                postMApFeedAddViewModel.loadData(postGetAlertListViewModel.obj?.map_feeds!!.get(which).id)
            } }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGetAlertsAPIObserver()
        setMapFeedsDataAPIObserver()
        setAddAlertAPIObserver()
        routeRefresh = RouteRefresh(Mapbox.getAccessToken(), this)
        mapView = v?.findViewById(R.id.mapView)
        startRouteButton = v?.findViewById(R.id.startRouteButton)
        mapView = v?.findViewById(R.id.mapView)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        postGetAlertListViewModel.loadData()
        postMApFeedDataViewModel.loadData()
        val context = activity!!
        val customNotification = CustomNavigationNotification(context)
        val options = MapboxNavigationOptions.builder()
            .navigationNotification(customNotification)
            .build()

        navigation = MapboxNavigation(activity!!, Mapbox.getAccessToken()!!, options)
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
        alert.setOnClickListener {
            showAlertsDialog()
        }
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
            (locationEngine as ReplayRouteLocationEngine).updateSpeed(40)

            (locationEngine as ReplayRouteLocationEngine).moveTo(Point.fromLngLat(
                originpoint.latitude(),
                originpoint.longitude()
            ))

           // navigation!!.locationEngine = locationEngine!!
            mapboxMap!!.locationComponent.isLocationComponentEnabled = true
            navigation!!.startNavigation(route!!)
        }
    }


    private fun newOrigin() {
        val gpsTracker = GPSTracker(activity!!)
        if (mapboxMap != null) {
            val latLng = LatLng(originpoint.latitude(),originpoint.longitude())
            (locationEngine as ReplayRouteLocationEngine).assignLastLocation(
                Point.fromLngLat(latLng.longitude, latLng.latitude)
            )
            mapboxMap!!.addMarker(MarkerOptions().position(latLng))
            if(gpsTracker.canGetLocation()) {
                val currentLoc = LatLng(gpsTracker.latitude,gpsTracker.longitude)
                mapboxMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17.0))
            }
        }
    }

    @SuppressLint("MissingPermission", "WrongConstant")
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(activity!!, style)
            locationComponent.renderMode = RenderMode.GPS
            locationComponent.isLocationComponentEnabled = false
            navigationMapRoute = NavigationMapRoute(navigation, mapView!!, mapboxMap)
            locationEngine = ReplayRouteLocationEngine()
            newOrigin()
            onMapClick()
        }
    }

    fun addMarkers() {

        try {
            for (x in 0 until postMApFeedDataViewModel.obj?.list?.size!!) {
                val latLng = LatLng(
                    postMApFeedDataViewModel.obj?.list?.get(x)?.lattitude?.toDouble()!!,
                    postMApFeedDataViewModel.obj?.list?.get(x)?.longitude?.toDouble()!!)
                val title =  postMApFeedDataViewModel.obj?.list?.get(x)?.feed_name
                Picasso.get().load(postMApFeedDataViewModel.obj?.list?.get(x)?.logo).into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        val bmp = BaseHelper.ScaleBitmap(bitmap,120)
                        // loaded bitmap is here (bitmap)
                        val iconFactory = IconFactory.getInstance(activity!!);
                        val icon = iconFactory.fromBitmap(bmp!!);

                        mapboxMap!!.addMarker(MarkerOptions()
                            .position(latLng)
                            .title(title)
                            .icon(icon))
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })

            }
        } catch (e : java.lang.Exception){

        }

    }
    private fun enableLocationComponent(@io.reactivex.annotations.NonNull loadedMapStyle: Style?) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(activity)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap!!.locationComponent
            locationComponent!!.activateLocationComponent(activity!!, loadedMapStyle!!)
            locationComponent!!.isLocationComponentEnabled = true
            // Set the component's camera mode
            locationComponent!!.cameraMode = CameraMode.TRACKING
            locationComponent!!.zoomWhileTracking(12.0);
        }
    }

    fun onMapClick() {

        if (destination == null) {
            destination = desrpoint

        } else if (waypoint == null) {
            waypoint = Point.fromLngLat(desrpoint.longitude(), desrpoint.latitude())
        } else {
            Toast.makeText(activity!!, "Only 2 waypoints supported", Toast.LENGTH_LONG).show()
        }
        val point = LatLng(destination!!.latitude(), destination!!.longitude())
        mapboxMap!!.addMarker(MarkerOptions().position(point))
        calculateRoute()
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

        val navigationRouteBuilder = NavigationRoute.builder(activity!!)
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
                        this@MockNavigationFragment.route = currentRoute
                        navigationMapRoute!!.addRoute(this@MockNavigationFragment.route)
                        onStartRouteClick()
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
      //  Toast.makeText(activity!!, "off-route called", Toast.LENGTH_LONG).show()
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
    fun setGetAlertsAPIObserver() {
        postGetAlertListViewModel = ViewModelProviders.of(this).get(PostGetAlertListViewModel::class.java).apply {
            this@MockNavigationFragment.let { thisFragReference ->
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
            this@MockNavigationFragment.let { thisFragReference ->
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
                            try {
                                addMarkers()
                            } catch (e  :java.lang.Exception) {

                            }
                        }
                    }
                })
            }
        }
    }
    fun setAddAlertAPIObserver() {
        postMApFeedAddViewModel = ViewModelProviders.of(this).get(PostMApFeedAddViewModel::class.java).apply {
            this@MockNavigationFragment.let { thisFragReference ->
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
                            showNotifyDialog(
                                "Thank you", postMApFeedAddViewModel.obj?.message!!,
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) { }
                                }
                            )

                            postMApFeedDataViewModel.loadData()

                        }
                    }
                })
            }
        }
    }

    companion object {

        private val BEGIN_ROUTE_MILESTONE = 1001
        private val TWENTY_FIVE_METERS = 25.0
        private val TAG = "DirectionsActivity"
        val SOURCE_ID = "SOURCE_ID"
        private val ICON_ID = "ICON_ID"
        val LAYER_ID = "LAYER_ID"
        fun getRandomLatLng(bbox: DoubleArray): LatLng {
            val random = Random()

            val randomLat = bbox[1] + (bbox[3] - bbox[1]) * random.nextDouble()
            val randomLon = bbox[0] + (bbox[2] - bbox[0]) * random.nextDouble()

            return LatLng(randomLat, randomLon)
        }
    }
}
