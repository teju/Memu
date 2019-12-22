package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment

import java.util.*
import android.widget.*
import androidx.lifecycle.Observer
import androidx.annotation.NonNull
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.location.modes.CameraMode
import android.R.style
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.PermissionListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.memu.bgTasks.LocationBroadCastReceiver
import com.memu.etc.GPSTracker
import com.memu.etc.Keys
import com.memu.etc.UserInfoManager
import com.memu.webservices.*
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.map_fragment.*
import kotlinx.android.synthetic.main.map_fragment.ld
import kotlinx.android.synthetic.main.map_fragment.time

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.IOException


class MapFragment : BaseFragment() , View.OnClickListener, OnMapReadyCallback, MapboxMap.OnMapClickListener,
    PermissionsListener {

    private var originPoint: Point? = null
    var src: CarmenFeature? = null
    var dest: CarmenFeature? = null
    var trip_rider_id: String? = ""
    private var gpsTracker: GPSTracker? = null
    private var mapboxMap: MapboxMap? = null
    // variables for adding location layer
    private var permissionsManager: PermissionsManager? = null
    private var locationComponent: LocationComponent? = null
    // variables for calculating and drawing a route
    private var currentRoute: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    // variables needed to initialize navigation
    lateinit var postnviteRideGiversViewModel : PostnviteRideGiversViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(com.memu.R.layout.map_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setInviteRideGiversAPIObserver()
        find_riders.setOnClickListener(this)
        gpsTracker = GPSTracker(activity)
        if(gpsTracker?.canGetLocation()!!) {
            mapView!!.getMapAsync(this)
        }

        when(Keys.MAPTYPE) {
            Keys.SHORTESTROUTE -> {
                shortes_route_result.visibility = View.VISIBLE
                find_riders.visibility = View.GONE
                sos.visibility = View.GONE
            }
            Keys.POOLING -> {
                postnviteRideGiversViewModel.loadData(trip_rider_id!!)
                shortes_route_result.visibility = View.GONE
                startButton.visibility = View.GONE
                find_riders.visibility = View.VISIBLE
                sos.visibility = View.VISIBLE

            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.find_riders ->
                if(postnviteRideGiversViewModel.obj?.rider_list != null) {
                    showMatchingRiders(postnviteRideGiversViewModel.obj?.rider_list!!,
                        object : NotifyListener {
                            override fun onButtonClicked(which: Int) {
                            }
                        })
                }
        }
    }

    override fun onMapReady(@io.reactivex.annotations.NonNull mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        if(Keys.MAPTYPE == Keys.SHORTESTROUTE) {
            mapboxMap.setStyle(getString(R.string.navigation_guidance_day)) { style ->
                enableLocationComponent(style)
                addDestinationIconSymbolLayer(style)
                mapboxMap.addOnMapClickListener(this@MapFragment)
                startButton!!.setOnClickListener {
                    val simulateRoute = true
                    val options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(simulateRoute)
                        .build()
                    // Call this method with Context from within an Activity
                    NavigationLauncher.startNavigation(activity, options)

                }
                showRoute()
            }
        } else {

        }

    }
    private fun addDestinationIconSymbolLayer(@io.reactivex.annotations.NonNull loadedMapStyle: Style) {
        val drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.map_marker, null);
        val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
        loadedMapStyle.addImage(
            "destination-icon-id", mBitmap!!)
        val geoJsonSource = GeoJsonSource("destination-source-id")
        loadedMapStyle.addSource(geoJsonSource)
        val destinationSymbolLayer =
            SymbolLayer("destination-symbol-layer-id", "destination-source-id")
        destinationSymbolLayer.withProperties(
            PropertyFactory.iconImage("destination-icon-id"),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true)
        )
        loadedMapStyle.addLayer(destinationSymbolLayer)
    }

    override fun onMapClick(@io.reactivex.annotations.NonNull point: LatLng): Boolean {

        val destinationPoint = Point.fromLngLat(point.longitude, point.latitude)
        val originPoint = Point.fromLngLat(
            locationComponent!!.lastKnownLocation!!.longitude,
            locationComponent!!.lastKnownLocation!!.latitude
        )

        val source = mapboxMap!!.style!!.getSourceAs<GeoJsonSource>("destination-source-id")
        source?.setGeoJson(Feature.fromGeometry(destinationPoint))

        getRoute(originPoint, destinationPoint)
        startButton!!.isEnabled = true
        return true
    }

    fun showRoute() {

        val destinationPoint = Point.fromLngLat((dest?.geometry() as Point).longitude(),
            (dest?.geometry() as Point).latitude())
        if(src == null) {
            originPoint = Point.fromLngLat(
                gpsTracker?.longitude!!,
                gpsTracker?.latitude!!
            )
        } else {
            originPoint = Point.fromLngLat(
                (src?.geometry() as Point).longitude(),
                (src?.geometry() as Point).latitude()
            )
        }


        val source = mapboxMap!!.style!!.getSourceAs<GeoJsonSource>("destination-source-id")
        source?.setGeoJson(Feature.fromGeometry(destinationPoint))
        val distance = BaseHelper.distance(originPoint?.latitude()!!,originPoint?.longitude()!!,
            (dest?.geometry() as Point).latitude(),(dest?.geometry() as Point).longitude())
        dist.text = String.format("%.2f", distance)  +" Kms"
        getRoute(originPoint!!, destinationPoint)
        startButton!!.isEnabled = true
    }

    private fun getRoute(origin: Point, destination: Point) {

        val navigationRoute = NavigationRoute.builder(activity)
        navigationRoute.accessToken(Mapbox.getAccessToken()!!)
        navigationRoute.origin(origin)
        navigationRoute.annotations()
        navigationRoute.alternatives(true)
        navigationRoute.destination(destination)
        navigationRoute.addWaypoint(destination)
        navigationRoute.build()
            .getRoute(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    // You can get the generic HTTP info about the response
                    Log.d(TAG, "Response code: " + response.code())
                    if (response.body() == null) {
                        Log.e(
                            TAG,
                            "No routes found, make sure you set the right user and access token."
                        )
                        return
                    } else if (response.body()!!.routes().size < 1) {
                        Log.e(TAG, "No routes found")
                        return
                    }

                    currentRoute = response.body()!!.routes()[0]
                    System.out.println("Response code: "+response.body()?.waypoints()?.size)
                    time.text = currentRoute?.duration().toString()
                    // Draw the route on the map
                    if (navigationMapRoute != null) {
                        navigationMapRoute!!.removeRoute()
                    } else {
                        navigationMapRoute = NavigationMapRoute(
                            null,
                            mapView!!,
                            mapboxMap!!,
                            R.style.NavigationMapRoute
                        )
                    }
                    navigationMapRoute!!.addRoute(currentRoute)
                }

                override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                    Log.e(TAG, "Error: " + throwable.message)
                }
            })

    }

    fun addMarkers() {

        val symbolLayerIconFeatureList = ArrayList<Feature>()
        for (x in 0 until postnviteRideGiversViewModel.obj?.rider_list?.size!!) {
            symbolLayerIconFeatureList.add(
                Feature.fromGeometry(
                    Point.fromLngLat(postnviteRideGiversViewModel.obj?.rider_list?.get(x)?.from_address?.longitude!!.toDouble(),
                        postnviteRideGiversViewModel.obj?.rider_list?.get(x)?.from_address?.lattitude!!.toDouble())
                )
            )
            System.out.println("addMarkers "+postnviteRideGiversViewModel.obj?.rider_list?.get(x)?.from_address?.longitude!!)

        }
        val drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.map_marker, null);
        val mBitmap = com.mapbox.mapboxsdk.utils.BitmapUtils.getBitmapFromDrawable(drawable);
        mapboxMap?.setStyle(
            Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

                // Add the SymbolLayer icon image to the map style
                .withImage(
                    ICON_ID, mBitmap!!)


                // Adding a GeoJson source for the SymbolLayer icons.
                .withSource(
                    GeoJsonSource(
                        SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)
                    )
                )

                // Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
                // marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
                // the coordinate point. This is offset is not always needed and is dependent on the image
                // that you use for the SymbolLayer icon.
                .withLayer(
                    SymbolLayer(
                        LAYER_ID,
                        SOURCE_ID
                    )
                        .withProperties(
                            PropertyFactory.iconImage(ICON_ID),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true),
                            PropertyFactory.iconOffset(arrayOf(0f, -9f))
                        )
                )
        ) {
            enableLocationComponent(it)
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
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(activity)
        }
    }

    fun setInviteRideGiversAPIObserver() {
        postnviteRideGiversViewModel = ViewModelProviders.of(this).get(PostnviteRideGiversViewModel::class.java).apply {
            this@MapFragment.let { thisFragReference ->
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
                        PostnviteRideGiversViewModel.NEXT_STEP -> {
                            if(Keys.MAPTYPE == Keys.POOLING) {
                                addMarkers()
                            }
                        }
                    }
                })
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @io.reactivex.annotations.NonNull permissions: Array<String>, @io.reactivex.annotations.NonNull grantResults: IntArray) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(activity, R.string.app_name, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap!!.style)
        } else {
            Toast.makeText(activity, R.string.app_name, Toast.LENGTH_LONG).show()

        }
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onBackTriggered() {
        super.onBackTriggered()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    companion object {
        private val TAG = "DirectionsActivity"
        val SOURCE_ID = "SOURCE_ID"
        private val ICON_ID = "ICON_ID"
        val LAYER_ID = "LAYER_ID"
    }
}