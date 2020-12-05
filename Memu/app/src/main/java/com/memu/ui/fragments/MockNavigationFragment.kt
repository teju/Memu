package com.memu.ui.activity


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.WalletBalanceListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.DirectionsCriteria.IMPERIAL
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.VoiceInstructions
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style

import com.mapbox.navigator.VoiceInstruction
import com.mapbox.services.android.navigation.ui.v5.NavigationViewModel
import com.mapbox.services.android.navigation.ui.v5.SoundButton
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.ui.v5.voice.NavigationSpeechPlayer
import com.mapbox.services.android.navigation.ui.v5.voice.SpeechPlayerProvider
import com.mapbox.services.android.navigation.ui.v5.voice.VoiceInstructionLoader
import com.mapbox.services.android.navigation.v5.instruction.Instruction
import com.mapbox.services.android.navigation.v5.location.replay.ReplayRouteLocationEngine
import com.mapbox.services.android.navigation.v5.milestone.*
import com.mapbox.services.android.navigation.v5.navigation.*
import com.mapbox.services.android.navigation.v5.navigation.camera.RouteInformation
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import com.memu.R
import com.memu.etc.GPSTracker
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager
import com.memu.modules.checksum.WalletBalance
import com.memu.modules.completedRides.MatchedBudy
import com.memu.ui.BaseFragment
import com.memu.ui.adapters.MatchingBuddiesAdapter
import com.memu.ui.fragments.HomeFragment
import com.memu.ui.fragments.WalletFragment
import com.memu.webservices.*
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPGService
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_mock_navigation.*
import kotlinx.android.synthetic.main.activity_mock_navigation.ld
import kotlinx.android.synthetic.main.custom_notification_layout.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import okhttp3.Cache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class MockNavigationFragment(
    var desrpoint: Point,
    var originpoint: Point) : BaseFragment(), OnMapReadyCallback, ProgressChangeListener, NavigationEventListener,
    MilestoneEventListener, OffRouteListener, RefreshCallback,WalletBalanceListener,
    PaytmPaymentTransactionCallback {
    private var CHECKSUMHASH: String = ""
    private var speechPlayer: NavigationSpeechPlayer?= null
    private var navigationViewModel: NavigationViewModel? = null
    private var srcmarker: Marker? = null
    private var cameraPosition: CameraPosition?= null
    var trip_id: String = ""
    var trip_type: String = ""
    var currentRoute: DirectionsRoute? = null
    var isTripStarted = false
    private var mapboxMap: MapboxMap? = null
    lateinit var postGetAlertListViewModel: PostGetAlertListViewModel
    lateinit var postMApFeedDataViewModel: PostMApFeedDataViewModel
    lateinit var postMApFeedAddViewModel: PostMApFeedAddViewModel
    lateinit var postCustomerEndNavigationViewModel: PostCustomerEndNavigationViewModel
    lateinit var postStartNavigationViewModel: PostStartNavigationViewModel
    lateinit var postCustomerEndNavigationIDViewModel: PostCustomerEndNavigationIDViewModel
    lateinit var postEndNavigationViewModel: PostEndNavigationViewModel
    lateinit var postMakePaymentViewModel: PostMakePaymentViewModel
    lateinit var getchecksumviewmodel: GetCheckSumViewModel

    // Navigation related variables
    private var locationEngine: LocationEngine? = null
    private var navigation: MapboxNavigation? = null
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var waypoint: Point? = null
    private var routeRefresh: RouteRefresh? = null
    private var isRefreshing = false
    private var mapView: MapView? = null
    var showDialog : Boolean = false
    var distanceTravelled = 0.0
    private var mockLocationEngine: ReplayRouteLocationEngine? = null
    private var tracking = false
    var walletBalance = ""
    var invoive_id = ""
    var amount_to_pay = 0.0
    var orderId="1000"

    private val callback = RerouteActivityLocationCallback(this)
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
        savedInstanceState: Bundle?): View? {
        activity?.setTheme(R.style.NavigationViewLight);
        v = inflater.inflate(R.layout.activity_mock_navigation, container, false)
        return v
    }

    override fun onDetach() {
        super.onDetach()
        navigation!!.stopNavigation()
    }
    fun showAlertsDialog() {
        try {
            System.out.println("showAlertsDialog map_feeds " + postGetAlertListViewModel.obj?.map_feeds!!)
            showAlertsDialog(postGetAlertListViewModel.obj?.map_feeds!!, object : NotifyListener {
                override fun onButtonClicked(which: Int) {
                    postMApFeedAddViewModel.loadData(
                        postGetAlertListViewModel.obj?.map_feeds!!.get(
                            which
                        ).id
                    )
                }
            })
        } catch (e : Exception){
            showDialog = true
            postGetAlertListViewModel.loadData()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGetAlertsAPIObserver()
        setMapFeedsDataAPIObserver()
        setAddAlertAPIObserver()
        setStartTripAPIObserver()
        setEndTripAPIObserver()
        setWalletBalanceObserver(this)
        setPaymentAPIObserver()
        setGetCheckSUMRequestObserver()
        setCustomerEndTripAPIObserver()
        setCustomerEndTripIDAPIObserver()
        routeRefresh = RouteRefresh(Mapbox.getAccessToken(), this)
        mapView = v?.findViewById(R.id.mapView)
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


        navigation = MapboxNavigation(context, Mapbox.getAccessToken()!!,options)
        //navigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)

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
       // customNotification.register(MyBroadcastReceiver(navigation!!), context)
        alert.setOnClickListener {
            showAlertsDialog()
        }
        if(BaseHelper.isEmpty(trip_id)) {
            endButton.visibility = View.GONE
        }
        if(!isTripStarted && !UserInfoManager.getInstance(activity!!).getRoleType().equals("rider",ignoreCase = true)) {
            endButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.Green)));
            txtendbtn.text = "Start Ride"
        } else {
            endButton.setBackgroundTintList(null);
            txtendbtn.text = "End Ride"
        }
        endButton.setOnClickListener {
            if(!isTripStarted && !UserInfoManager.getInstance(activity!!).getRoleType().equals("rider",ignoreCase = true)) {
                if(!BaseHelper.isEmpty(trip_id)) {
                    postStartNavigationViewModel.loadData(trip_id)
                }
            } else {
                navigation!!.stopNavigation()
                if(UserInfoManager.getInstance(activity!!).getRoleType().equals("rider",ignoreCase = true)) {
                    postCustomerEndNavigationIDViewModel.loadData(trip_id)
                } else {
                     postEndNavigationViewModel.loadData(trip_id, distanceTravelled)
                }
            }
        }
        arrow_left.setOnClickListener {
            home().proceedDoOnBackPressed()
        }

        recenture.setOnClickListener {
            val gpsTracker = GPSTracker(activity!!)
            if(gpsTracker.canGetLocation()) {
                val currentLoc = LatLng(gpsTracker.latitude,gpsTracker.longitude)
                mapboxMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 28.0))
            }
        }
        initViewModels()
        instructionView.retrieveSoundButton().hide()
        val soundButton =  instructionView.findViewById<SoundButton>(R.id.soundLayout)
        instructionView.retrieveFeedbackButton()
        soundButton.setOnClickListener {
            println("navigationViewModel "+soundButton.toggleMute())
        }
        initializeSpeechPlayer()
        instructionView.retrieveFeedbackButton().hide()
        getWalletBalanceViewModel.loadData()

    }
    /*val voiceInstructionsObserver = object : VoiceInstructionsObserver {
        override fun onNewVoiceInstructions(voiceInstructions: VoiceInstructions) {

        }
    }*/
    private fun initializeSpeechPlayer() {
        val cache =
            Cache(File(activity?.cacheDir, VOICE_INSTRUCTION_CACHE), 10 * 1024 * 1024)
        val voiceInstructionLoader =
            VoiceInstructionLoader(activity, Mapbox.getAccessToken(), cache)
        val speechPlayerProvider =
            SpeechPlayerProvider(activity!!, Locale.US.language, true, voiceInstructionLoader)
        speechPlayer = NavigationSpeechPlayer(speechPlayerProvider)
    }
    private fun initViewModels() {
        try {
            navigationViewModel = ViewModelProviders.of(activity!!).get(NavigationViewModel::class.java)
        } catch (exception: ClassCastException) {
            throw ClassCastException("Please ensure that the provided Context is a valid FragmentActivity")
        }
    }
    fun onStartRouteClick() {


        val isValidNavigation = navigation != null
        val isValidRoute = route != null && route!!.distance()!! > TWENTY_FIVE_METERS
        if (isValidNavigation && isValidRoute) {

            navigation!!.addNavigationEventListener(this)
            navigation!!.addProgressChangeListener(this)
            navigation!!.addMilestoneEventListener(this)
            navigation!!.addOffRouteListener(this)
            //(locationEngine as ReplayRouteLocationEngine).assign(route)
            //navigation!!.locationEngine = locationEngine!!
            mapboxMap!!.locationComponent.isLocationComponentEnabled = true
            navigation!!.startNavigation(route!!)
        }
    }

    private fun newOrigin(point : LatLng) {
        val gpsTracker = GPSTracker(activity!!)
        if (mapboxMap != null) {
            val latLng = LatLng(point.latitude,point.longitude)
            val destlatLng = LatLng(desrpoint!!.latitude(),desrpoint.longitude())
            (locationEngine as ReplayRouteLocationEngine).assignLastLocation(
                Point.fromLngLat(latLng.longitude, latLng.latitude)
            )
            mapboxMap!!.addMarker(MarkerOptions().position(destlatLng))
            if(gpsTracker.canGetLocation()) {
                val currentLoc = LatLng(gpsTracker.latitude,gpsTracker.longitude)
                mapboxMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 28.0))
            }
        }
    }

    @SuppressLint("MissingPermission", "WrongConstant")
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            mockLocationEngine = ReplayRouteLocationEngine()

            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(activity!!, style)
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.renderMode = RenderMode.GPS
            locationComponent.isLocationComponentEnabled = false
            navigationMapRoute = NavigationMapRoute(navigation, mapView!!, mapboxMap)
            locationComponent.zoomWhileTracking(32.0)
            locationEngine = ReplayRouteLocationEngine()
            newOrigin(LatLng(originpoint.latitude(),originpoint.longitude()))
            if(currentRoute != null) {
                ld.showLoadingV2()
                showRoute()
            } else {
                reroute(originpoint.longitude(),originpoint.latitude())
            }
            val currentCameraPosition =
                mapboxMap!!.cameraPosition
            val currentZoom = currentCameraPosition.zoom
            System.out.println("currentCameraPosition "+currentZoom)
        }
    }

    fun showRoute() {
        System.out.println("destinationPoint navigationRoute "+currentRoute)

        this@MockNavigationFragment.route = currentRoute
        navigationMapRoute!!.addRoute(this@MockNavigationFragment.route)
        if(ld != null) {
            ld.hide()
        }
        onStartRouteClick()
        tracking = true
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

    fun navigationRoute(origin: Point) {

        val navigationRouteBuilder = NavigationRoute.builder(activity!!)
            .accessToken(Mapbox.getAccessToken()!!)
        navigationRouteBuilder.origin(origin)
        navigationRouteBuilder.destination(desrpoint!!)
        if (waypoint != null) {
            navigationRouteBuilder.addWaypoint(waypoint!!)
        }
        navigationRouteBuilder.voiceUnits(IMPERIAL)

        navigationRouteBuilder.enableRefresh(true)
        navigationRouteBuilder.build().getRoute(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                Timber.d("Url: %s", call.request().url().toString())
                if (response.body() != null) {
                    if (!response.body()!!.routes().isEmpty()) {
                        var routes = response.body()!!.routes().get(0)
                        route = routes
                        navigationMapRoute!!.addRoute(routes)
                        if(ld != null) {
                            ld.hide()
                        }
                        onStartRouteClick()
                        tracking = true
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                Timber.e(throwable, "onFailure: navigation.getRoute()")
            }
        })
    }

    @SuppressLint("LongLogTag")
    override fun onMilestoneEvent(
        routeProgress: RouteProgress,
        instruction: String,
        milestone: Milestone
    ) {
        instructionView.updateBannerInstructionsWith(milestone)

        Log.d("Milestone Event Occurred with id: %d", milestone.identifier.toString())
        Log.d("Voice instruction: %s", instruction)

    }

    override fun onRunning(running: Boolean) {
        if (running) {
            navigation?.addOffRouteListener(this);
            navigation?.addProgressChangeListener(this);
            Timber.d("onRunning: Started")
        } else {
            Timber.d("onRunning: Stopped")
        }
    }

    override fun userOffRoute(location: Location) {
        reroute(location.longitude,location.latitude)
    }

    fun reroute(longitude : Double,latitude : Double ) {
        ld.showLoadingV2()
        val origin = Point.fromLngLat(longitude, latitude)
        Snackbar.make(v!!, "User Off Route", Snackbar.LENGTH_SHORT).show();
        originpoint = origin
        navigationMapRoute?.removeRoute()
        navigationRoute(origin)
    }
    override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
        mapboxMap!!.locationComponent.forceLocationUpdate(location)

        if (!isRefreshing) {
            isRefreshing = true
            routeRefresh!!.refresh(routeProgress)
        }
        Timber.d(
            "onProgressChange: fraction of route traveled: %f",
            routeProgress.distanceTraveled()
        )
        distanceTravelled = routeProgress.distanceTraveled()/1000
        if(routeProgress.fractionTraveled() == 1f){
            if(!BaseHelper.isEmpty(trip_id)) {
                if(!BaseHelper.isEmpty(walletBalance) && walletBalance.toDouble() != 0.0) {
                    postEndNavigationViewModel.loadData(trip_id,distanceTravelled)
                } else {
                    home().setFragment(WalletFragment())
                }
            }
        }

        if (tracking) {
            mapboxMap!!.locationComponent.forceLocationUpdate(location)
            cameraPosition =
                CameraPosition.Builder()
                    .zoom(17.0)
                    .target(
                        LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )
                    .bearing(location.bearing.toDouble())
                    .build()
            mapboxMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition!!), 2000)

            duration_left.text =
                Helper.splitToComponentTimes(BigDecimal(routeProgress.durationRemaining()))
            dist_left.text = ","+Helper.FormatDistance(routeProgress.distanceRemaining(), "km")
            val formatter = SimpleDateFormat("hh:mm a")
            val dateString: String = formatter.format(Date())
            current_time.text = dateString
        }
        RouteInformation.create(route, location, routeProgress);
        instructionView.updateDistanceWith(routeProgress);
    }


    override fun onResume() {
        super.onResume()
        mapView!!.onResume()

    }

    override fun onPause() {
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
    fun updateLocation(location: Location?) {
        if (!tracking) {
            mapboxMap!!.locationComponent.forceLocationUpdate(location)
        }
    }

    private class BeginRouteInstruction : Instruction() {

        override fun buildInstruction(routeProgress: RouteProgress): String {
            return "Have a safe trip!"
        }
    }

    fun amountToPAy()  {
        if(postCustomerEndNavigationViewModel.obj?.trip_details != null) {
            amount_to_pay =
                postCustomerEndNavigationViewModel.obj?.trip_details?.no_of_kms!!.toDouble() *
                        postCustomerEndNavigationViewModel.obj?.trip_details?.price_per_km!!.toDouble()
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
                            if(showDialog) {
                                showAlertsDialog(postGetAlertListViewModel.obj?.map_feeds!!, object : NotifyListener {
                                    override fun onButtonClicked(which: Int) {
                                        postMApFeedAddViewModel.loadData(
                                            postGetAlertListViewModel.obj?.map_feeds!!.get(
                                                which
                                            ).id
                                        )
                                    }
                                })
                                showDialog = false
                            }
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

    fun setStartTripAPIObserver() {
        postStartNavigationViewModel = ViewModelProviders.of(this).get(PostStartNavigationViewModel::class.java).apply {
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
                            override fun onButtonClicked(which: Int) {
                                endButton.setBackgroundTintList(null);
                                txtendbtn.text = "End Ride"
                                isTripStarted = true
                            }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostStartNavigationViewModel.NEXT_STEP -> {
                            endButton.setBackgroundTintList(null);
                            txtendbtn.text = "End Ride"
                            isTripStarted = true
                        }
                    }
                })
            }
        }
    }
    fun setCustomerEndTripIDAPIObserver() {
        postCustomerEndNavigationIDViewModel = ViewModelProviders.of(this).get(PostCustomerEndNavigationIDViewModel::class.java).apply {
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
                            override fun onButtonClicked(which: Int) {
                                endButton.setBackgroundTintList(null);
                                txtendbtn.text = "End Ride"
                                isTripStarted = true
                            }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostCustomerEndNavigationIDViewModel.NEXT_STEP -> {
                            postCustomerEndNavigationViewModel.loadData(postCustomerEndNavigationIDViewModel.obj?.trip_details?.trip_id!!,
                                distanceTravelled,postCustomerEndNavigationIDViewModel.obj?.trip_details?.trip_rider_id!!)

                        }
                    }
                })
            }
        }
    }

    fun setEndTripAPIObserver() {
        postEndNavigationViewModel = ViewModelProviders.of(this).get(PostEndNavigationViewModel::class.java).apply {
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
                            override fun onButtonClicked(which: Int) {
                                isTripStarted = false
                            }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostEndNavigationViewModel.NEXT_STEP -> {
                            showNotifyDialog(
                                "",postEndNavigationViewModel.obj?.message,
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) {
                                        isTripStarted = false
                                        home().setFragment(HomeFragment())
                                    }
                                }
                            )
                        }
                    }
                })
            }
        }
    }

    fun setCustomerEndTripAPIObserver() {
        postCustomerEndNavigationViewModel = ViewModelProviders.of(this).get(PostCustomerEndNavigationViewModel::class.java).apply {
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
                            override fun onButtonClicked(which: Int) {
                            }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostEndNavigationViewModel.NEXT_STEP -> {
                            showNotifyDialog(
                                "","Pay Now",
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) {
                                        isTripStarted = false
                                        amountToPAy()
                                        var amt = amount_to_pay
                                        if( amount_to_pay > walletBalance.toDouble()) {
                                            amt = amount_to_pay - walletBalance.toDouble()
                                        }
                                        paymentStates = BEFORE_PAYMENT
                                        postMakePaymentViewModel.loadData("before",amt.toString(),
                                            walletBalance,
                                            postCustomerEndNavigationViewModel.obj?.trip_details?.driver_id!!,
                                            postCustomerEndNavigationViewModel.obj?.trip_details?.trip_id!!,
                                            "online", invoive_id,amount_to_pay.toString(),"pending")

                                        //home().setFragment(HomeFragment())
                                    }
                                }
                            )
                        }
                    }
                })
            }
        }
    }

    fun setPaymentAPIObserver() {
        postMakePaymentViewModel = ViewModelProviders.of(this).get(
            PostMakePaymentViewModel::class.java).apply {
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
                        PostMakePaymentViewModel.NEXT_STEP -> {
                            if(obj!!.transaction_id != null) {
                                invoive_id = obj!!.transaction_id
                            }
                            when (paymentStates) {
                                BEFORE_PAYMENT -> {
                                    amountToPAy()
                                    if( amount_to_pay > walletBalance.toDouble()) {
                                        orderId = "ID"+Random().nextInt()
                                        paymentStates = RECHARGE
                                        getchecksumviewmodel.loadData(UserInfoManager.getInstance(activity!!).getAccountId(),orderId,amount_to_pay)
                                    } else {
                                        paymentStates = AFTER_PAYMENT
                                        var amt = amount_to_pay
                                        if( amount_to_pay > walletBalance.toDouble()) {
                                            amt = amount_to_pay - walletBalance.toDouble()
                                        }
                                        postMakePaymentViewModel.loadData(
                                            "after", amt.toString(),
                                            walletBalance, postCustomerEndNavigationViewModel.obj?.trip_details?.driver_id!!,  trip_id, "online",
                                            invoive_id, amount_to_pay.toString(), "success")
                                    }
                                }
                                AFTER_PAYMENT -> {
                                    home().setFragment(HomeFragment())
                                }
                                RECHARGE -> {
                                    paymentStates = AFTER_PAYMENT
                                    amountToPAy()
                                    var amt = amount_to_pay
                                    if( amount_to_pay > walletBalance.toDouble()) {
                                        amt = amount_to_pay - walletBalance.toDouble()
                                    }
                                    postMakePaymentViewModel.loadData(
                                        "after", amt.toString(),
                                        walletBalance,
                                        postCustomerEndNavigationViewModel.obj?.trip_details?.driver_id!!,
                                        trip_id, "online",
                                        invoive_id, amount_to_pay.toString(), "success")
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    fun setGetCheckSUMRequestObserver() {
        getchecksumviewmodel = ViewModelProviders.of(this).get(
            GetCheckSumViewModel::class.java).apply {
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
                        GetCheckSumViewModel.NEXT_STEP -> {
                            if(getchecksumviewmodel.obj?.generate_signature != null) {
                                CHECKSUMHASH = getchecksumviewmodel.obj?.generate_signature!!
                            }
                            val Service = PaytmPGService.getProductionService()
                            val paramMap =
                                HashMap<String, String>()
                            //these are mandatory parameters
                            paramMap["MID"] = getString(R.string.mid) //MID provided by paytm
                            paramMap["CUST_ID"] = UserInfoManager.getInstance(apl!!).getAccountId()
                            paramMap["ORDER_ID"] = orderId
                            paramMap["CHANNEL_ID"] = "WAP"
                            paramMap["TXN_AMOUNT"] = amount_to_pay.toString()
                            paramMap["WEBSITE"] = "DEFAULT"
                            paramMap["CALLBACK_URL"] = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId
                            // paramMap.put("EMAIL", "daya_salagare@yahoo.com");   // no need
                            // paramMap.put("MOBILE_NO", "9986104911");  // no need
                            paramMap["CHECKSUMHASH"] = CHECKSUMHASH
                            // paramMap.put("PAYMENT_TYPE_ID", "CC");    // no need
                            paramMap["INDUSTRY_TYPE_ID"] = "Retail"
                            val Order = PaytmOrder(paramMap)
                            com.paytm.pgsdk.Log.d(TAG,"checksum "+"param $paramMap")
                            System.out.println("check-sum "+"param $paramMap")

                            com.paytm.pgsdk.Log.e("checksum ", "param $paramMap")
                            Service.initialize(Order, null)
                            // start payment service call here
                            Service.startPaymentTransaction(
                                context, true, true,
                                this@MockNavigationFragment)

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
        const val VOICE_INSTRUCTION_CACHE = "voice-instruction-cache"
        var paymentStates = 1

        private val BEGIN_ROUTE_MILESTONE = 1001
        private val BEFORE_PAYMENT = 1
        private val AFTER_PAYMENT = 2
        private val RECHARGE = 3
        private val TWENTY_FIVE_METERS = 25.0
        private val TAG = "check-sum"
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
    private class RerouteActivityLocationCallback internal constructor(activity: MockNavigationFragment) :
        LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<MockNavigationFragment>
        override fun onSuccess(result: LocationEngineResult) {
            val activity = activityWeakReference.get()
            if (activity != null) {
                val location = result.lastLocation ?: return
                activity.updateLocation(location)
            }
        }

        override fun onFailure(exception: Exception) {
            Timber.e(exception)
        }

        init {
            activityWeakReference = WeakReference(activity)
        }
    }

    override fun walletBalanceResponse(balance: WalletBalance) {
        walletBalance = balance.balance!!
    }

    override fun onTransactionResponse(inResponse: Bundle?) {
        com.paytm.pgsdk.Log.d(TAG,"inResponse "+inResponse.toString())
        if(inResponse!!.getString("RESPCODE") == "01") {
            val amt_to_pay = amount_to_pay - walletBalance.toDouble()
            paymentStates = RECHARGE
            postMakePaymentViewModel.loadData("wallet",amt_to_pay.toString(),walletBalance,"","","","","","")
        } else {
            BaseHelper.showAlert(activity, inResponse.getString("RESPMSG"))
        }
    }

    override fun clientAuthenticationFailed(inErrorMessage: String?) {
        com.paytm.pgsdk.Log.d(TAG,"inErrorMessage "+inErrorMessage.toString())
    }

    override fun someUIErrorOccurred(inErrorMessage: String?) {
        com.paytm.pgsdk.Log.d(TAG,"someUIErrorOccurred "+inErrorMessage.toString())
    }

    override fun onTransactionCancel(inErrorMessage: String?, inResponse: Bundle?) {
        com.paytm.pgsdk.Log.d(TAG,"onTransactionCancel "+inErrorMessage.toString())
    }

    override fun networkNotAvailable() {
        com.paytm.pgsdk.Log.d(TAG,"networkNotAvailable ")
    }

    override fun onErrorLoadingWebPage(
        iniErrorCode: Int,
        inErrorMessage: String?,
        inFailingUrl: String?
    ) {
        com.paytm.pgsdk.Log.d(TAG,"onErrorLoadingWebPage "+inErrorMessage)
    }

    override fun onBackPressedCancelTransaction() {
        com.paytm.pgsdk.Log.d(TAG,"onBackPressedCancelTransaction ")
    }
}
