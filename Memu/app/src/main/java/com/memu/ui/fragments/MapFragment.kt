package com.memu.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment

import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil

import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.RequestListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory

import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils


import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute

import com.memu.etc.GPSTracker
import com.memu.etc.Keys
import com.memu.modules.googleMaps.Route
import com.memu.ui.activity.MockNavigationFragment
import com.memu.webservices.*
import kotlinx.android.synthetic.main.map_fragment.*
import kotlinx.android.synthetic.main.map_fragment.ld
import kotlinx.android.synthetic.main.map_view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


import java.lang.Exception
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class MapFragment : BaseFragment() , View.OnClickListener, PermissionsListener ,
    OnMapReadyCallback ,Callback<DirectionsResponse>,com.mapbox.mapboxsdk.maps.OnMapReadyCallback{


    private var destinationPoint: Point? = null
    private var maporiginPoint: Point? = null
    private var routes: List<DirectionsRoute> = listOf<DirectionsRoute>()
    private var myView: LinearLayout? = null
    var srcLat = 0.0
    var srcLng = 0.0
    var destLat = 0.0
    var destLng = 0.0

    var trip_rider_id: String? = ""
    var type: String? = ""
    private var gpsTracker: GPSTracker? = null
    //private var mapboxMap: MapboxMap? = null
    // variables for adding location layer
    private var permissionsManager: PermissionsManager? = null
    private var locationComponent: LocationComponent? = null
    // variables for calculating and drawing a route
    private var navigationMapRoute: NavigationMapRoute? = null
    // variables needed to initialize navigation
    lateinit var postnviteRideGiversViewModel: PostnviteRideGiversViewModel
    lateinit var postRidersFragment: PostRequestRideViewModel
    lateinit var postGetRoutesViewModel: PostGetRoutesViewModel
    private var mMap: GoogleMap? = null
    internal var markerPoints = ArrayList<com.google.android.gms.maps.model.LatLng>()
    var mapcurrentRoute: DirectionsRoute? = null
    private var mapboxMap: MapboxMap? = null
    private val styleCycle = StyleCycle()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(com.memu.R.layout.map_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        googlemapView?.onCreate(savedInstanceState);
        googlemapView?.onResume();
       // mapView?.getMapAsync(this)
        try {
            val  inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
            myView = inflater.inflate(R.layout.map_view, null) as LinearLayout
            frame_layout.addView(myView);
            mapView.onCreate(savedInstanceState)
            mapView!!.getMapAsync(this)
        } catch (e : Exception){
            System.out.println("initUIException "+e.toString())
        }
        initUI();
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            try {
                frame_layout.addView(myView);
               // mapView!!.getMapAsync(this)

            } catch (e : Exception){
                System.out.println("initUIException "+e.toString())
            }
        } else {
            frame_layout.removeAllViews()
        }
    }
    override fun onMapReady(mapboxMap: MapboxMap) {
        try {
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(getString(R.string.navigation_guidance_day)) { style ->
                enableLocationComponent(style)
                addDestinationIconSymbolLayer(style)
                navigationMapRoute = NavigationMapRoute(null, mapView, mapboxMap)
                mapboxMap.addMarker(
                    com.mapbox.mapboxsdk.annotations.MarkerOptions().position(
                        com.mapbox.mapboxsdk.geometry.LatLng(
                            srcLat,
                            srcLng
                        )
                    )
                )
                mapboxMap.addMarker(
                    com.mapbox.mapboxsdk.annotations.MarkerOptions().position(
                        com.mapbox.mapboxsdk.geometry.LatLng(
                            destLat,
                            destLng
                        )
                    )
                )

                navigationMapRoute?.setOnRouteSelectionChangeListener {
                    startButton.isEnabled = true
                    mapcurrentRoute = it
                }
                GetRoutes()
            }
        } catch (e : Exception){

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

    private fun initializeLocationComponent(mapboxMap: MapboxMap) {
        val locationComponent = mapboxMap.locationComponent
        locationComponent.activateLocationComponent(activity!!, mapboxMap.style!!)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.renderMode = RenderMode.COMPASS
        locationComponent.cameraMode = CameraMode.TRACKING
        locationComponent.zoomWhileTracking(15.0)
    }
    class StyleCycle {

        private var index: Int = 0

        val nextStyle: String
            get() {
                index++
                if (index == STYLES.size) {
                    index = 0
                }
                return style
            }

        val style: String
            get() = STYLES[index]

        companion object {
            val STYLES = arrayOf(
                Style.MAPBOX_STREETS,
                Style.OUTDOORS,
                Style.LIGHT,
                Style.DARK,
                Style.SATELLITE_STREETS
            )
        }
    }

    private fun initUI() {
        setInviteRideGiversAPIObserver()
        setRequestRideAPIObserver()
        setGoogleMapRouteAPIObserver()
        arrow_left.setOnClickListener(this)
        rloption_c.setOnClickListener(this)
        rloption_a.setOnClickListener(this)
        rloption_b.setOnClickListener(this)
        startButton.setOnClickListener(this)
        gpsTracker = GPSTracker(activity)

        startButton.isEnabled = false
        when (Keys.MAPTYPE) {
            Keys.SHORTESTROUTE -> {
                shortes_route_result.visibility = View.VISIBLE
                sos.visibility = View.GONE
            }
            Keys.POOLING -> {
                postnviteRideGiversViewModel.loadData(trip_rider_id!!, type!!)
                shortes_route_result.visibility = View.GONE
                startButton.visibility = View.VISIBLE
                sos.visibility = View.VISIBLE

            }
        }

    }
    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
    }

    override fun onResponse(
        call: Call<DirectionsResponse>,
        response: Response<DirectionsResponse>
    ) {
        if (response.isSuccessful
            && response.body() != null
            && !response.body()!!.routes().isEmpty()
        ) {
            routes = response.body()!!.routes()
            Collections.sort(routes,object  : Comparator<DirectionsRoute>{
                override fun compare(o1: DirectionsRoute?, o2: DirectionsRoute?): Int {
                    return o1?.duration()?.compareTo(o2?.duration()!!)!!; // To compare string values

                }
            })


            navigationMapRoute?.addRoutes(routes)
            mapcurrentRoute = routes.get(0)
            startButton.isEnabled = true
            addButtons(response.body()!!.routes())
            val position = CameraPosition.Builder()
                .target(com.mapbox.mapboxsdk.geometry.LatLng(srcLat, srcLng))
                .zoom(15.0)
                .tilt(20.0)
                .build();
            mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
            System.out.println("onResponse DirectionsResponse "+response.body()!!.routes().size)

        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.arrow_left -> {
                // frame_layout.removeView(myView)
                home().proceedDoOnBackPressed()
            }
            R.id.rloption_a -> {
                startButton.isEnabled = true
                rloption_b.setBackgroundTintList(null)
                rloption_c.setBackgroundTintList(null)
                rloption_a.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.Blue)));
                mapcurrentRoute = routes.get(0)
            }
            R.id.rloption_b -> {
                startButton.isEnabled = true
                rloption_a.setBackgroundTintList(null)
                rloption_c.setBackgroundTintList(null)
                rloption_b.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.Yellow)));
                mapcurrentRoute = routes.get(1)
            }
            R.id.rloption_c -> {
                startButton.isEnabled = true
                rloption_a.setBackgroundTintList(null)
                rloption_b.setBackgroundTintList(null)
                rloption_c.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.polylinePink)));
                mapcurrentRoute = routes.get(2)

            }
            R.id.startButton -> {
                home().setFragment(MockNavigationFragment(this!!.destinationPoint!!, this@MapFragment.maporiginPoint!!).apply {
                    System.out.println("onMapReady mapcurrentRoute "+mapcurrentRoute?.legs())
                    this.currentRoute = mapcurrentRoute

                })
            }
        }
    }

    fun addButtons(routes: List<DirectionsRoute>) {
        for(i in  0..(routes.size - 1)) {
            if (i == 0) {
                rloption_a.visibility = View.VISIBLE
                var duration = splitToComponentTimes(BigDecimal(routes.get(i).duration()!!))
                option_a_time.text = duration
                option_a_date.text = FormatDistance(routes.get(i).distance()!!,"km")
            } else if (i == 1) {
                rloption_b.visibility = View.VISIBLE
                var duration = splitToComponentTimes(BigDecimal(routes.get(i).duration()!!))

                option_b_time.text = duration
                option_b_dist.text = FormatDistance(routes.get(i).distance()!!,"km")
            } else {
                var duration = splitToComponentTimes(BigDecimal(routes.get(i).duration()!!))
                option_c_time.text = duration
                option_c_dist.text = FormatDistance(routes.get(i).distance()!!,"km")
                rloption_c.visibility = View.VISIBLE

            }
        }
    }
    fun FormatDistance(meters:Double, unitString:String):String {
        val df = DecimalFormat("#.##");
        return ""+df.format(meters/1000 )+unitString
    }

    fun splitToComponentTimes(biggy: BigDecimal):String {
        val longVal = biggy.toLong()
        val hours = longVal.toInt() / 3600
        var remainder = longVal.toInt() - hours * 3600
        val mins = remainder / 60
        remainder = remainder - mins * 60
        val secs = remainder
        val ints = intArrayOf(hours, mins, secs)
        var duration = ""
        if(ints.get(0) > 0) {
            duration = ""+ints.get(0)+"hr"
        }
        if(ints.get(1) > 0) {
            duration = duration+" "+ints.get(1)+"min"
        }

        return duration
    }

    override fun onMapReady(googleMap: GoogleMap) {
        System.out.println("onMapReady called")
        mMap = googleMap
        val origin = LatLng(srcLat, srcLng)
        val dest = LatLng(destLat, destLng)

        if (markerPoints.size > 1) {
            markerPoints.clear()
            mMap!!.clear()
        }
        markerPoints.add(origin)
        markerPoints.add(dest)

        googleMap.addMarker(MarkerOptions().position(origin));
        googleMap.addMarker(MarkerOptions().position(dest));
        mMap!!.moveCamera(
            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                origin,
                17f
            )
        )
        if (markerPoints.size >= 2) {
            // Getting URL to the Google Directions API
            val url = getDirectionsUrl(origin, dest)
            postGetRoutesViewModel.loadData(url)

        }
        GetRoutes()
    }
    fun  getPoints(points: String): List<LatLng>? {
        return PolyUtil.decode(points);
    }

    fun GetRoutes() {
        maporiginPoint = Point.fromLngLat(
            srcLng, srcLat
        )
        destinationPoint = Point.fromLngLat(
            destLng,
            destLat
        )

        findRoute(maporiginPoint!!, destinationPoint!!)
    }

    fun findRoute(origin: Point, destination: Point) {
        NavigationRoute.builder(activity!!)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(origin)
            .destination(destination)
            .alternatives(true)
            .build()
            .getRoute(this)
    }

    private fun displayMap(travelRoutes: List<Route>) {
        for(i in  0..(travelRoutes.size - 1)){
            val route = travelRoutes.get(i)
            if (route != null && route.overview_polyline != null) {
                val  routePoints = getPoints(route.overview_polyline.points)
                val polylineOptions = PolylineOptions()
                if(i == 0) {
                    rloption_a.visibility = View.VISIBLE
                    option_a_time.text = route.legs.get(0).duration.text
                    option_a_date.text = route.legs.get(0).distance.text
                    polylineOptions.color(resources.getColor(R.color.Blue))
                }else if(i == 1) {
                    rloption_b.visibility = View.VISIBLE
                    option_b_time.text = route.legs.get(0).duration.text
                    option_b_dist.text = route.legs.get(0).distance.text
                    polylineOptions.color(resources.getColor(R.color.Yellow))
                } else {
                    if(!BaseHelper.isEmpty(route.legs.get(0).duration.text)) {
                        option_c_time.text = route.legs.get(0).duration.text
                        option_c_dist.text = route.legs.get(0).distance.text
                        rloption_c.visibility = View.VISIBLE
                        polylineOptions.color(resources.getColor(R.color.BlueViolet))
                    }
                }
                polylineOptions.addAll(routePoints)
                mMap!!.addPolyline(polylineOptions).isClickable = true
                mMap!!.setOnPolylineClickListener { polyline ->
                    //polyline.color = Color.GREEN
                }
            }
        }
    }
    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "&alternatives=true&sensor=false"
        val mode = "mode=driving"
        val key = "key=" + getString(R.string.google_maps_key)
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode&$key"

        // Output format
        val output = "json"

        // Building the url to the web service


        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }

    fun addMarkers() {

        val symbolLayerIconFeatureList = ArrayList<Feature>()
        try {
            for (x in 0 until postnviteRideGiversViewModel.obj?.pooler_list?.size!!) {
                symbolLayerIconFeatureList.add(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            postnviteRideGiversViewModel.obj?.pooler_list?.get(x)?.from_address?.longitude!!.toDouble(),
                            postnviteRideGiversViewModel.obj?.pooler_list?.get(x)?.from_address?.lattitude!!.toDouble()
                        )
                    )
                )
                System.out.println(
                    "addMarkers " + postnviteRideGiversViewModel.obj?.pooler_list?.get(
                        x
                    )?.from_address?.longitude!!
                )

            }
        } catch (e : Exception){

        }
        try {
            for (x in 0 until postnviteRideGiversViewModel.obj?.rider_list?.size!!) {
                symbolLayerIconFeatureList.add(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            postnviteRideGiversViewModel.obj?.rider_list?.get(x)?.from_address?.longitude!!.toDouble(),
                            postnviteRideGiversViewModel.obj?.rider_list?.get(x)?.from_address?.lattitude!!.toDouble()
                        )
                    )
                )
                System.out.println(
                    "addMarkers " + postnviteRideGiversViewModel.obj?.rider_list?.get(
                        x
                    )?.from_address?.longitude!!
                )

            }
        } catch (e : Exception){
            ld.hide()
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
            initializeLocationComponent(mapboxMap!!)
        }
    }

    /* override fun onMapReady(@io.reactivex.annotations.NonNull mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day)) { style ->
            enableLocationComponent(style)
            addDestinationIconSymbolLayer(style)
            //mapboxMap.addOnMapClickListener(this@MapFragment)
            startButton!!.setOnClickListener {
                *//*val simulateRoute = false
                val options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .shouldSimulateRoute(simulateRoute)

                    .build()
                // Call this method with Context from within an Activity
                NavigationLauncher.startNavigation(activity, options)*//*
                home().setFragment(NavigationFragment().apply {
                    this.currentRoute = this@MapFragment.currentRoute
                })
            }
            showRoute()
        }


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


    fun addMarkers() {

        val symbolLayerIconFeatureList = ArrayList<Feature>()
        try {
            for (x in 0 until postnviteRideGiversViewModel.obj?.pooler_list?.size!!) {
                symbolLayerIconFeatureList.add(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            postnviteRideGiversViewModel.obj?.pooler_list?.get(x)?.from_address?.longitude!!.toDouble(),
                            postnviteRideGiversViewModel.obj?.pooler_list?.get(x)?.from_address?.lattitude!!.toDouble()
                        )
                    )
                )
                System.out.println(
                    "addMarkers " + postnviteRideGiversViewModel.obj?.pooler_list?.get(
                        x
                    )?.from_address?.longitude!!
                )

            }
        } catch (e : Exception){

        }
        try {
            for (x in 0 until postnviteRideGiversViewModel.obj?.rider_list?.size!!) {
                symbolLayerIconFeatureList.add(
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            postnviteRideGiversViewModel.obj?.rider_list?.get(x)?.from_address?.longitude!!.toDouble(),
                            postnviteRideGiversViewModel.obj?.rider_list?.get(x)?.from_address?.lattitude!!.toDouble()
                        )
                    )
                )
                System.out.println(
                    "addMarkers " + postnviteRideGiversViewModel.obj?.rider_list?.get(
                        x
                    )?.from_address?.longitude!!
                )

            }
        } catch (e : Exception){
            ld.hide()
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
    }*/

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
                               // addMarkers()
                                try {
                                    if (postnviteRideGiversViewModel.obj?.pooler_list != null) {
                                        showMatchingRiders(postnviteRideGiversViewModel.obj?.pooler_list!!,
                                            object : RequestListener {
                                                override fun onButtonClicked(user_id: String, id: String) {
                                                    postRidersFragment.loadData(user_id, id, type!!)

                                                }
                                            })
                                    } else {
                                        showMatchingRiders(postnviteRideGiversViewModel.obj?.rider_list!!,
                                            object : RequestListener {
                                                override fun onButtonClicked(user_id: String, id: String) {
                                                    postRidersFragment.loadData(user_id, id, type!!)
                                                }
                                            })
                                    }
                                } catch (e : Exception){
                                    showNotifyDialog(
                                        "No Matching List found", "",
                                        getString(R.string.ok),"",object : NotifyListener {
                                            override fun onButtonClicked(which: Int) { }
                                        }
                                    )
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    fun setRequestRideAPIObserver() {
        postRidersFragment = ViewModelProviders.of(this).get(PostRequestRideViewModel::class.java).apply {
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
                        PostRequestRideViewModel.NEXT_STEP -> {

                        }
                    }
                })
            }
        }
    }

    fun setGoogleMapRouteAPIObserver() {
        postGetRoutesViewModel = ViewModelProviders.of(this).get(PostGetRoutesViewModel::class.java).apply {
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
                        PostGetRoutesViewModel.NEXT_STEP -> {
                           // displayMap(postGetRoutesViewModel.obj?.routes!!)
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
            //enableLocationComponent(mapboxMap!!.style)
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
