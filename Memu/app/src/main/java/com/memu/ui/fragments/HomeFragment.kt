package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment

import kotlinx.android.synthetic.main.home_fragment.*
import java.util.*
import android.widget.*
import androidx.lifecycle.Observer
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.view.MenuItem
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.iapps.gon.etc.callback.FindRideDialogListener
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.memu.bgTasks.LocationBroadCastReceiver
import com.memu.etc.*
import com.memu.modules.PlaceHolder
import com.memu.ui.activity.SearchActivity
import com.memu.ui.adapters.WeekAdapter
import com.memu.webservices.*
import kotlinx.android.synthetic.main.map_view.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat


class HomeFragment : BaseFragment() , View.OnClickListener,
    OnMapReadyCallback, PermissionsListener{

    private var myView: LinearLayout? = null
    private var pendingIntent: PendingIntent? = null
    private var alarmManager: AlarmManager? = null
    private var gpsTracker: GPSTracker? = null

    lateinit var postUpdateNotiTokenViewModel: PostUpdateNotiTokenViewModel
    lateinit var postFindRideViewModel: PostFindRideViewModel
    lateinit var postUserVehicleListViewModel: PostUserVehicleListViewModel
    var viaLatitide:Double = 0.0
    var destLatitide:Double = 0.0
    var destLongitude:Double = 0.0
    var viaLongitude:Double = 0.0
    var srcLongitude:Double = 0.0
    var srcLatitude:Double = 0.0
    private var mapboxMap: MapboxMap? = null

    private val REQUEST_CODE_AUTOCOMPLETE = 1
    private val REQUEST_CODE_AUTOCOMPLETEDEST = 2
    private val REQUEST_CODE_AUTOCOMPLETEVIA = 3
    private var permissionsManager: PermissionsManager? = null
    private var locationComponent: LocationComponent? = null
    var weekdays : ArrayList<String> =  ArrayList<String>()

    var type = 1
    var strdate =""
    var strtime =""
    var strseat =""
    var strType = "find_ride"
    var days = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.home_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden) {
            try {
                home_mapView.addView(myView);
                mapView!!.getMapAsync(this)

            } catch (e : Exception){

            }
        }
        if(Keys.MAPTYPE == Keys.POOLING_BACK) {
            poolingUI()
        }
    }
    override fun onBackTriggered() {
        super.onBackTriggered()
        home().exitApp()
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


    private fun initUI() {
        setUpdateNotiTokenAPIObserver()
        setFindTripAPIObserver()
        setPoolerVehicleListAPIObserver()
        cv.setCardBackgroundColor(activity!!.resources.getColor(R.color.Purple));
        gpsTracker = GPSTracker(activity)
        if(gpsTracker?.canGetLocation()!!) {
            srcLatitude = gpsTracker?.latitude!!
            srcLongitude = gpsTracker?.longitude!!
        }

        try {
            Helper.loadImage(activity!!,UserInfoManager.getInstance(activity!!).getProfilePic(),profile_pic,R.drawable.user_default)

        } catch (e : java.lang.Exception){

        }
        try {
            val  inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
            myView = inflater.inflate(R.layout.map_view, null) as LinearLayout
            home_mapView.addView(myView);
            mapView!!.getMapAsync(this)
        } catch (e : Exception){

        }
        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as  AlarmManager;

        val alarmIntent =  Intent(activity, LocationBroadCastReceiver::class.java);
        pendingIntent = PendingIntent.getBroadcast(activity, 0, alarmIntent, 0);
        startAlarm()
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(object : OnSuccessListener<InstanceIdResult> {
            override fun onSuccess(p0: InstanceIdResult?) {
                val token = p0?.getToken();
                UserInfoManager.getInstance(activity!!).saveNotiToken(token)
                postUpdateNotiTokenViewModel.loadData()
            }

        });
        postUserVehicleListViewModel.loadData()

        rlBestRoute.setOnClickListener(this)
        rlpooling.setOnClickListener(this)
        rlcab.setOnClickListener(this)
        rlprofile.setOnClickListener(this)
        btnNExt.setOnClickListener(this)
        cancel.setOnClickListener(this)
        date.setOnClickListener(this)
        offer_ride.setOnClickListener(this)
        time.setOnClickListener(this)
        seatstv.setOnClickListener(this)
        find_ride.setOnClickListener(this)
        arrow_left.setOnClickListener(this)
        bike_offer_ride.setOnClickListener(this)
        bike_find_ride.setOnClickListener(this)
        history_icon.setOnClickListener(this)
        edtVia.setOnClickListener(this)

        initSearchFab()
        initSearchFabDest()

        System.out.println("getAccountName "+UserInfoManager.getInstance(activity!!).getAccountName())
        name.setText(UserInfoManager.getInstance(activity!!).getAccountName())
        date.text = SimpleDateFormat("EEE,\nMMM dd").format(System.currentTimeMillis())
        time.text = SimpleDateFormat("hh:mm\na").format(System.currentTimeMillis())
        seatstv.text = "01\nSeats"

        strdate =  SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        strtime =  SimpleDateFormat("hh:mm a").format(System.currentTimeMillis())
        strseat = "01"
        weekRecyclerView()
        try {
            val getAddress = getAddress(gpsTracker?.latitude!!, gpsTracker?.longitude!!)
            edtScrLoc.setText(getAddress?.get(0)?.getAddressLine(0))

        } catch (e : Exception){

        }
    }

    fun spinner() {
       val popup = PopupMenu(activity, seatstv);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                    .inflate(R.menu.menu, popup.getMenu());

                memuData(popup)
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        seatstv.text = item?.getTitle().toString()+"\nSeats"
                        strseat = item?.getTitle().toString()
                        return true;
                    }

                });

                popup.show(); //sho
    }

    fun memuData(popup:PopupMenu) {
        popup.getMenu().clear()
        popup.getMenu().add("01");
        popup.getMenu().add("02");
        popup.getMenu().add("03");
        popup.getMenu().add("04");
        popup.getMenu().add("05");
    }

    fun startAlarm() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),1000, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),1000, pendingIntent);
        } else {
            alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),1000, pendingIntent);
        }
    }

    fun CallApi(vehicle_id : String,rs_per_kms : String) {

        val from = FromJSon(srcLatitude!!,srcLongitude!!)
        val latng1 = com.google.android.gms.maps.model.LatLng(srcLatitude,srcLongitude)
        val latng2 = com.google.android.gms.maps.model.LatLng( destLatitide,destLongitude)

        val distance = BaseHelper.showDistance(latng1,latng2)
        val To = FromJSon(destLatitide, destLongitude)
        val via = FromJSon(viaLatitide, viaLongitude)
        var is_recuring = "no"
        if(!BaseHelper.isEmpty(days)) {
            is_recuring = "yes"
        }
        if(srcLatitude != 0.0 && srcLongitude != 0.0) {
            postFindRideViewModel.loadData(
                strdate, strtime, strseat, is_recuring, days,
                from, To, distance.toInt().toString(), strType,
                vehicle_id,rs_per_kms,via)
        } else {
            showNotifyDialog(
                "", "Select your source location",
                getString(R.string.ok),"",object : NotifyListener {
                    override fun onButtonClicked(which: Int) { }
                }
            )
        }

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.rlBestRoute -> {
                bestRouteUI()
                Keys.MAPTYPE = Keys.SHORTESTROUTE
            }

            R.id.rlpooling -> {
                poolingUI()
            }
            R.id.btnNExt -> {
                if(!BaseHelper.isEmpty(edtdestLoc.text.toString())) {
                    edtdestLocerror.visibility = View.GONE
                    if(Keys.MAPTYPE == Keys.SHORTESTROUTE ) {
                        if((srcLatitude != 0.0 && srcLongitude != 0.0)) {
                            reset()
                            System.out.println("GetRoutes maporiginPoint " +
                                    "srcLatitude "+srcLatitude+" srcLongitude "+srcLongitude)

                            home_mapView.removeAllViews()
                            val mapFragment = MapFragment()
                            mapFragment.srcLat = srcLatitude
                            mapFragment.srcLng = srcLongitude
                            mapFragment.destLng = destLongitude
                            mapFragment.destLat = destLatitide
                            home().setFragment(mapFragment)

                        } else {
                            showNotifyDialog(
                                "", "Select your source location",
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) { }
                                }
                            )
                        }

                    }

                } else {
                    edtdestLocerror.visibility = View.VISIBLE
                }

            }
            R.id.rlcab -> {

            }
            R.id.rlprofile -> {

            }
            R.id.seatstv -> {
                spinner()

            }
            R.id.edtVia -> {
                startActivityForResult(Intent(activity, SearchActivity::class.java),REQUEST_CODE_AUTOCOMPLETEVIA);
            }
            R.id.cancel,R.id.arrow_left -> {
                reset()

            }
            R.id.history_icon -> {
                Keys.MAPTYPE = Keys.HISTORY
                reset()
                home_mapView.removeAllViews()
                home().setFragment(HistoryFragment().apply {})
            }
            R.id.date -> {

                var cal = Calendar.getInstance()

                val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val myFormat = "EEE,\nMMM dd" // mention the format you need
                    val myFormat2 = "yyyy-MM-dd" // mention the format you need
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    val sdf2 = SimpleDateFormat(myFormat2, Locale.US)
                    date.text = sdf.format(cal.time)
                    strdate = sdf2.format(cal.time)
                }

                date.setOnClickListener {
                    val datePickerDialog = DatePickerDialog(activity, dateSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH))
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() );
                    datePickerDialog.show()
                }
            }
            R.id.time -> {
                TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }
            R.id.find_ride -> {
                Keys.MAPTYPE = Keys.POOLING_FIND_RIDE
                days = weekdays.joinToString(separator = ",")

                strType ="find_ride"
                findRide("","")
            }
            R.id.bike_find_ride -> {
                Keys.MAPTYPE = Keys.POOLING_FIND_RIDE
                days = weekdays.joinToString(separator = ",")
                strType ="find_ride"
                findRide("","")
            }
            R.id.bike_offer_ride -> {
                Keys.MAPTYPE = Keys.POOLING_OFFER_RIDE
                strType ="offer_ride"
                days = weekdays.joinToString(separator = ",")
                showFindRideDialog()
            }
            R.id.offer_ride -> {
                Keys.MAPTYPE = Keys.POOLING_OFFER_RIDE
                strType ="offer_ride"
                days = weekdays.joinToString(separator = ",")
                showFindRideDialog()
            }
        }
    }


    fun  findRide(vehicle_id : String,rs_per_kms : String) {
        if(validate()) {
            if (Keys.MAPTYPE == Keys.SHORTESTROUTE) {
                if ((srcLatitude != 0.0 && srcLongitude != 0.0)) {
                    reset()
                    home_mapView.removeAllViews()
                    val mapFragment = MapFragment()
                    mapFragment.srcLat = srcLatitude
                    mapFragment.srcLng = srcLongitude
                    mapFragment.destLng = destLongitude
                    mapFragment.destLat = destLatitide
                    home().setFragment(mapFragment)

                }

            } else {
                CallApi(vehicle_id,rs_per_kms)
            }
        }
    }
    fun validate():Boolean {
        if (BaseHelper.isEmpty(edtdestLoc.text.toString())) {
            edtdestLocerror.visibility = View.GONE
            edtdestLoc.requestFocus()
            showNotifyDialog(
                "", "Select your source location",
                getString(R.string.ok), "", object : NotifyListener {
                    override fun onButtonClicked(which: Int) {}
                }
            )
            return false
        }
        return true
    }
    val cal = Calendar.getInstance()

    val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)

        time.text = SimpleDateFormat("hh:mm\na").format(cal.time)
        strtime = SimpleDateFormat("hh:mm a").format(cal.time)
    }

    fun showFindRideDialog() {
        if(postUserVehicleListViewModel.obj?.vehicle_list?.size != 0) {
            showFindRideDialog(postUserVehicleListViewModel.obj?.vehicle_list!!, object :
                FindRideDialogListener {
                override fun onButtonClicked(which: Int, rs_per_kms: String) {
                    findRide(
                        postUserVehicleListViewModel.obj!!.vehicle_list.get(which).vehicle_id,
                        rs_per_kms
                    )
                }
            })
        } else {
            showNotifyDialog(
                "",postUserVehicleListViewModel.obj?.message,
                getString(R.string.ok),"",object : NotifyListener {
                    override fun onButtonClicked(which: Int) { }
                }
            )
        }
    }

    fun reset() {
        popUpView.visibility = View.GONE
        arrow_left.visibility = View.GONE
        llPooling.visibility = View.GONE
        cancel.visibility = View.GONE
        rlTopBar.visibility = View.VISIBLE
        home_map_bg.alpha = 1f
        rlpooling.alpha = 1f
        rlcab.alpha = 1f
        rlprofile.alpha = 1f
        rlBestRoute.alpha = 1f
        scrollView.fullScroll(View.FOCUS_UP);
        rlBestRoute.isEnabled = true
    }

    fun poolingUI() {
        popUpView.visibility = View.VISIBLE
        edtVia.visibility = View.GONE
        arrow_left.visibility = View.VISIBLE
        llPooling.visibility = View.VISIBLE
        cancel.visibility = View.GONE
        rlTopBar.visibility = View.GONE
        vehicle_detail.visibility = View.VISIBLE
        cv.visibility = View.VISIBLE
        home_map_bg.alpha = 0.5f
        rlpooling.alpha = 1f
        rlcab.alpha = 0.5f
        rlprofile.alpha = 0.5f
        rlBestRoute.alpha = 0.5f
        scrollView.scrollTo(0,Helper.dpToPx(activity!!,200))
        rlBestRoute.isEnabled = false

    }

    fun weekRecyclerView() {
        var obj : ArrayList<PlaceHolder> =  ArrayList<PlaceHolder>()
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

        val sglm2 = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
        weeksList.setLayoutManager(sglm2)
        weeksList.setNestedScrollingEnabled(false)

        val adapter = WeekAdapter(context!!)

        adapter.obj = obj
        adapter.weekdays = weekdays
        weeksList.adapter = adapter

        (weeksList.adapter as WeekAdapter).productAdapterListener =
            object : WeekAdapter.ProductAdapterListener {
                override fun onClick(position: String, checked: Boolean) {
                    if(checked &&  !weekdays.contains(position)) {
                        weekdays.add(position)
                    } else {
                        weekdays.remove(position)
                    }
                    adapter.weekdays = weekdays
                    weeksList.adapter?.notifyDataSetChanged()

                }
            }
    }
    fun bestRouteUI() {
        popUpView.visibility = View.VISIBLE
        edtVia.visibility = View.GONE
        cancel.visibility = View.GONE
        rlTopBar.visibility = View.GONE
        llPooling.visibility = View.GONE
        vehicle_detail.visibility = View.GONE
        cv.visibility = View.GONE
        arrow_left.visibility = View.VISIBLE
        home_map_bg.alpha = 0.5f
        rlpooling.alpha = 0.5f
        rlcab.alpha = 0.5f
        rlprofile.alpha = 0.5f
        rlBestRoute.alpha = 1f
        scrollView.fullScroll(View.FOCUS_UP);
        rlBestRoute.isEnabled = false

    }
    override fun onMapReady(@io.reactivex.annotations.NonNull mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day)) { style ->
            enableLocationComponent(style)

            //   navigationMapRoute = NavigationMapRoute(null, mapView, mapboxMap)

            //mapboxMap.addOnMapClickListener(this@MapFragment)

            addMarkers()
        }


    }

    fun addMarkers() {
        val symbolLayerIconFeatureList = ArrayList<Feature>()
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(
                    srcLongitude,
                    srcLatitude))
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(
                    destLongitude,
                    destLatitide))
        )
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
        val position = CameraPosition.Builder()
            .target(LatLng(srcLatitude, srcLongitude))
            .zoom(15.0)
            .tilt(20.0)
            .build();
        mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
    }


    private fun enableLocationComponent(@io.reactivex.annotations.NonNull loadedMapStyle: Style?) {
        if (PermissionsManager.areLocationPermissionsGranted(activity)) {
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


    private fun initSearchFab() {
        edtScrLoc.setOnClickListener {
            startActivityForResult(Intent(activity, SearchActivity::class.java),REQUEST_CODE_AUTOCOMPLETE);
        }
    }

    private fun initSearchFabDest() {
        edtdestLoc.setOnClickListener {
            startActivityForResult(Intent(activity, SearchActivity::class.java),REQUEST_CODE_AUTOCOMPLETEDEST);
        }

    }


    fun setUpdateNotiTokenAPIObserver() {
        postUpdateNotiTokenViewModel = ViewModelProviders.of(this).get(PostUpdateNotiTokenViewModel::class.java).apply {
            this@HomeFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->

                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostUpdateNotiTokenViewModel.NEXT_STEP -> {

                        }
                    }
                })

            }
        }
    }


    fun setFindTripAPIObserver() {
        postFindRideViewModel = ViewModelProviders.of(this).get(PostFindRideViewModel::class.java).apply {
            this@HomeFragment.let { thisFragReference ->
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
                        PostFindRideViewModel.NEXT_STEP -> {
                            home_mapView.removeAllViews()
                            val mapFragment = MapFragment()
                            mapFragment.srcLat = srcLatitude
                            mapFragment.srcLng = srcLongitude
                            mapFragment.destLng = destLongitude
                            mapFragment.destLat = destLatitide
                            if(postFindRideViewModel.obj?.trip_id != null) {
                                mapFragment.trip_rider_id = postFindRideViewModel.obj?.trip_id!!
                            } else {
                                mapFragment.trip_rider_id = postFindRideViewModel.obj?.trip_rider_id!!
                            }
                            mapFragment.type = strType
                            home().setFragment(mapFragment)

                        }
                    }
                })
            }
        }
    }
    fun setPoolerVehicleListAPIObserver() {
        postUserVehicleListViewModel = ViewModelProviders.of(this).get(PostUserVehicleListViewModel::class.java).apply {
            this@HomeFragment.let { thisFragReference ->
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
                        PostUserVehicleListViewModel.NEXT_STEP -> {

                        }
                    }
                })
            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
                val lat = data?.getDoubleExtra("Lat",0.0)
                val lng = data?.getDoubleExtra("Lng",0.0)
                edtScrLoc.setText(data?.getStringExtra("Address"))
                srcLatitude = lat!!
                srcLongitude = lng!!
                try {
                    home_mapView.removeView(myView)
                    home_mapView.addView(myView);
                    mapView!!.getMapAsync(this)

                } catch (e : Exception){

                }


            }
            if (requestCode == REQUEST_CODE_AUTOCOMPLETEDEST) {
                val lat = data?.getDoubleExtra("Lat",0.0)
                val lng = data?.getDoubleExtra("Lng",0.0)
                edtdestLoc.setText(data?.getStringExtra("Address"))
                destLatitide = lat!!
                destLongitude = lng!!
                try {
                    home_mapView.removeView(myView)
                    home_mapView.addView(myView);
                    mapView!!.getMapAsync(this)

                } catch (e : Exception){

                }

            }
            if (requestCode == REQUEST_CODE_AUTOCOMPLETEVIA) {
                val lat = data?.getDoubleExtra("Lat",0.0)
                val lng = data?.getDoubleExtra("Lng",0.0)
                edtVia.setText(data?.getStringExtra("Address"))
                viaLatitide = lat!!
                viaLongitude = lng!!


            }
        } catch (e : Exception){

        }


    }
    companion object {
        private val TAG = "DirectionsActivity"
        val SOURCE_ID = "SOURCE_ID"
        private val ICON_ID = "ICON_ID"
        val LAYER_ID = "LAYER_ID"
    }
}
