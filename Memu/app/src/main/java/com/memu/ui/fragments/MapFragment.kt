package com.memu.ui.fragments

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.PolyUtil

import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.RequestListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory

import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils


import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute

import com.memu.etc.GPSTracker
import com.memu.etc.Helper
import com.memu.etc.Keys
import com.memu.etc.UserInfoManager
import com.memu.modules.PlaceHolder
import com.memu.modules.completedRides.Completed
import com.memu.modules.completedRides.FromAddress
import com.memu.modules.completedRides.ToAddress
import com.memu.ui.activity.MockNavigationFragment
import com.memu.ui.activity.SearchActivity
import com.memu.ui.adapters.WeekAdapter
import com.memu.webservices.*
import kotlinx.android.synthetic.main.map_fragment.*
import kotlinx.android.synthetic.main.map_fragment.ld
import kotlinx.android.synthetic.main.map_view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


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
    var weekdays : java.util.ArrayList<String> = java.util.ArrayList<String>()

    var trip_rider_id: String? = ""
    var type: String? = ""
    var recursivedays: String? = ""
    private var gpsTracker: GPSTracker? = null
    var completed : Completed?= null
    private var permissionsManager: PermissionsManager? = null
    private var locationComponent: LocationComponent? = null

    private var navigationMapRoute: NavigationMapRoute? = null

    lateinit var postnviteRideGiversViewModel: PostnviteRideGiversViewModel
    lateinit var postRidersFragment: PostRequestRideViewModel
    lateinit var postEditRecuringViewModel: PostEditRecuringViewModel
    var destLatitide:Double = 0.0
    var destLongitude:Double = 0.0
    var viaLongitude:Double = 0.0
    var srcLongitude:Double = 0.0
    var srcLatitude:Double = 0.0

    private var mMap: GoogleMap? = null
    internal var markerPoints = ArrayList<LatLng>()
    var mapcurrentRoute: DirectionsRoute? = null
    private var mapboxMap: MapboxMap? = null

    private val REQUEST_CODE_AUTOCOMPLETE = 1
    private val REQUEST_CODE_AUTOCOMPLETEDEST = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.map_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        googlemapView?.onCreate(savedInstanceState);
        googlemapView?.onResume();

        try {
            val  inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
            myView = inflater.inflate(R.layout.map_view, null) as LinearLayout
            frame_layout.addView(myView);
            mapView.onCreate(savedInstanceState)
            mapView!!.getMapAsync(this)
        } catch (e : Exception){

        }

        initUI();
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden) {
            try {
                frame_layout.addView(myView);
            } catch (e : Exception){
            }
        } else {
            frame_layout.removeAllViews()
        }
        if(Keys.MAPTYPE == Keys.RECURSIVE_EDIT) {
            shortes_route_result.visibility = View.GONE
            startButton.visibility = View.GONE
            fledit_recuring.visibility = View.VISIBLE
            weeAdapter()
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

    private fun initUI() {
        setInviteRideGiversAPIObserver()
        setRequestRideAPIObserver()
        setEditRecurringAPIObserver()
        arrow_left.setOnClickListener(this)
        rloption_c.setOnClickListener(this)
        rloption_a.setOnClickListener(this)
        rloption_b.setOnClickListener(this)
        startButton.setOnClickListener(this)
        srcRl.setOnClickListener(this)
        destRl.setOnClickListener(this)
        gpsTracker = GPSTracker(activity)
        try {
            Helper.loadImage(activity!!,
                UserInfoManager.getInstance(activity!!).getProfilePic(),profile_pic,R.drawable.user_default)

        } catch (e : java.lang.Exception){

        }
        startButton.isEnabled = false
        when (Keys.MAPTYPE) {
            Keys.SHORTESTROUTE -> {
                shortes_route_result.visibility = View.VISIBLE
                sos.visibility = View.GONE
                fledit_recuring.visibility = View.GONE

            }
            Keys.POOLING_OFFER_RIDE,Keys.POOLING_FIND_RIDE -> {
                postnviteRideGiversViewModel.loadData(trip_rider_id!!, type!!)
                shortes_route_result.visibility = View.GONE
                sos.visibility = View.VISIBLE
                fledit_recuring.visibility = View.GONE
                if(Keys.MAPTYPE == Keys.POOLING_FIND_RIDE) {
                    startButton.visibility = View.GONE
                } else {
                    startButton.visibility = View.VISIBLE
                }
            }
            Keys.RECURSIVE_EDIT -> {
                shortes_route_result.visibility = View.GONE
                startButton.visibility = View.GONE
                fledit_recuring.visibility = View.VISIBLE
                weeAdapter()
            }

        }
    }

    fun weeAdapter() {
        var obj : java.util.ArrayList<PlaceHolder> = java.util.ArrayList<PlaceHolder>()
        var placeholder = PlaceHolder()
        placeholder.name = "Mo"
        obj.add(placeholder)

        placeholder = PlaceHolder()
        placeholder.name = "Tu"
        obj.add(placeholder)

        placeholder = PlaceHolder()
        placeholder.name = "We"
        obj.add(placeholder)

        placeholder = PlaceHolder()
        placeholder.name = "Th"
        obj.add(placeholder)

        placeholder = PlaceHolder()
        placeholder.name = "Fr"
        obj.add(placeholder)

        placeholder = PlaceHolder()
        placeholder.name = "Sa"
        obj.add(placeholder)

        placeholder = PlaceHolder()
        placeholder.name = "Su"
        obj.add(placeholder)

        val sglm2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        weeksList.setLayoutManager(sglm2)
        weeksList.setNestedScrollingEnabled(false)

        val adapter = WeekAdapter(context!!)
        val recursive = recursivedays?.split(",")!!
        for(week in recursive) {
            weekdays.add(week)
        }
        adapter.obj = obj
        adapter.weekdays = weekdays
        weeksList.adapter = adapter
        adapter.isHome = false
        (weeksList.adapter as WeekAdapter).productAdapterListener =
            object : WeekAdapter.ProductAdapterListener {
                override fun onClick(position: String, checked: Boolean) {

                    if(checked && !BaseHelper.containsIgnoreCase(weekdays,position)) {
                        weekdays.add(position)
                    } else {

                        weekdays.remove(position)
                        weekdays.remove(position.toLowerCase())
                        System.out.println("recursivedays WeekAdapter "+weekdays)

                    }

                    adapter.weekdays = weekdays
                    weeksList.adapter?.notifyDataSetChanged()

                }
            }
        srcLatitude = completed!!.from_address.lattitude.toDouble()
        srcLongitude = completed!!.from_address.longitude.toDouble()
        destLatitide = completed!!.to_address.lattitude.toDouble()
        destLongitude = completed!!.to_address.longitude.toDouble()

        srcLoc.setText(completed?.from_address?.address_line1)
        destLoc.setText(completed?.from_address?.address_line1)
        pause_button.setOnClickListener {
            makeCompletedPAram(getStatus())
        }
        delete.setOnClickListener {
            makeCompletedPAram("deleted")
        }
        pause_button.setText(getStatusText())
    }

    fun makeCompletedPAram(status: String) {
        try {
            val fromAddress = FromJSon(srcLatitude, srcLongitude)
            val toAddress = FromJSon(destLatitide, destLongitude)
            val days = weekdays.joinToString(separator = ",")
            var is_recuring = "no"
            var vehicle_id = ""
            if (!BaseHelper.isEmpty(days)) {
                is_recuring = "yes"
            }
            if (!BaseHelper.isEmpty(completed!!.vehicle_id)) {
                vehicle_id = completed!!.vehicle_id
            }
            val completed = Completed(
                "", "", completed?.date!!,
                FromAddress(), completed?.id!!, listOf(),
                status,
                completed!!.time,
                days,
                completed!!.type,
                vehicle_id,
                is_recuring,
                completed!!.no_of_seats,
                ToAddress()
            )
            postEditRecuringViewModel.loadData(completed,fromAddress,toAddress)

            System.out.println("makeCompletedPAram " + completed)
        } catch (e : Exception){
            System.out.println("makeCompletedPAram Exception " + e.toString())

        }
    }
    fun getStatus() : String{
        when(completed?.type) {
            "offer_ride" -> {
                if(completed?.status!!.equals("scheduled",ignoreCase = true)) {
                    return "pause"
                } else {
                    return "scheduled"
                }
            }
            "find_ride" -> {
                if(completed?.status!!.equals("requested",ignoreCase = true)) {
                    return "pause"
                } else {
                    return "requested"
                }
            }
        }
        return "pause"
    }

    fun getStatusText() : String{
        when(completed?.type) {
            "offer_ride" -> {
                if(completed?.status!!.equals("scheduled",ignoreCase = true)) {
                    return "Pause"
                } else {
                    return "Scheduled"
                }
            }
            "find_ride" -> {
                if(completed?.status!!.equals("requested",ignoreCase = true)) {
                    return "Pause"
                } else {
                    return "Requested"
                }
            }
        }
        return "Pause"
    }

    fun FromJSon(lattitude : Double,longitude : Double) : JSONObject {
        val getAddress = getAddress(lattitude,longitude)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("address_line1", getAddress?.get(0)?.getAddressLine(0))
            jsonObject.put("lattitude", lattitude.toString())
            jsonObject.put("longitude", longitude.toString())
            jsonObject.put("state", getAddress?.get(0)?.getAdminArea())
            jsonObject.put("formatted_address", getAddress?.get(0)?.getAddressLine(0))
        } catch (e :java.lang.Exception) {
            System.out.println("makeCompletedPAram " + e.toString())

        }
        return jsonObject
    }

    fun getAddress(latitude : Double,longitude : Double): List<Address>? {
        val geocoder: Geocoder
        var addresses: List<Address>? = null
        geocoder = Geocoder(context, Locale.getDefault())

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addresses
    }


    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
        System.out.println("GetRoutes onFailure "+t.stackTrace.toString())

    }

    override fun onResponse(
        call: Call<DirectionsResponse>,
        response: Response<DirectionsResponse>)
    {
        System.out.println("GetRoutes onResponse "+response)
        try {
            if (response.isSuccessful
                && response.body() != null
                && !response.body()!!.routes().isEmpty()) {
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
                    .zoom(12.0)
                    .tilt(20.0)
                    .build();
                mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);

            }

        } catch (e : Exception){

        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.arrow_left -> {
                home().proceedDoOnBackPressed()
            }
            R.id.srcRl -> {
                startActivityForResult(Intent(activity, SearchActivity::class.java),REQUEST_CODE_AUTOCOMPLETE);

            }
            R.id.destRl -> {
                startActivityForResult(Intent(activity, SearchActivity::class.java),REQUEST_CODE_AUTOCOMPLETEDEST);

            }
            R.id.startButton -> {
                home().setFragment(MockNavigationFragment(this!!.destinationPoint!!, this@MapFragment.maporiginPoint!!).apply {
                    this.trip_id = trip_rider_id!!
                    this.currentRoute = mapcurrentRoute
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
                val lat = data?.getDoubleExtra("Lat",0.0)
                val lng = data?.getDoubleExtra("Lng",0.0)
                srcLoc.setText(data?.getStringExtra("Address"))
                srcLatitude = lat!!
                srcLongitude = lng!!
                            }
            if (requestCode == REQUEST_CODE_AUTOCOMPLETEDEST) {
                val lat = data?.getDoubleExtra("Lat",0.0)
                val lng = data?.getDoubleExtra("Lng",0.0)
                destLoc.setText(data?.getStringExtra("Address"))
                destLatitide = lat!!
                destLongitude = lng!!

            }
        } catch (e : Exception){

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

        GetRoutes()
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
                            if(Keys.MAPTYPE == Keys.POOLING_OFFER_RIDE || Keys.MAPTYPE == Keys.POOLING_FIND_RIDE) {
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
    fun setEditRecurringAPIObserver() {
        postEditRecuringViewModel = ViewModelProviders.of(this).get(PostEditRecuringViewModel::class.java).apply {
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
                        PostEditRecuringViewModel.NEXT_STEP -> {
                            onBackTriggered()
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

}
