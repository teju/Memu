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
import androidx.annotation.NonNull
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.location.modes.CameraMode
import android.R.style
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.PermissionListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.memu.bgTasks.LocationBroadCastReceiver
import com.memu.etc.GPSTracker
import com.memu.etc.Helper
import com.memu.etc.Keys
import com.memu.etc.UserInfoManager
import com.memu.webservices.*
import kotlinx.android.synthetic.main.home_fragment.ld
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat


class HomeFragment : BaseFragment() , View.OnClickListener {

    private var pendingIntent: PendingIntent? = null
    private var alarmManager: AlarmManager? = null
    private var gpsTracker: GPSTracker? = null

    lateinit var postUpdateNotiTokenViewModel: PostUpdateNotiTokenViewModel
    lateinit var postFindRideViewModel: PostFindRideViewModel


    private val REQUEST_CODE_AUTOCOMPLETE = 1
    private val REQUEST_CODE_AUTOCOMPLETEDEST = 2

    var selectedCarmenFeatureSrc:CarmenFeature? = null
    var selectedCarmenFeatureDest:CarmenFeature? = null

    var type = 1
    var FINDRIDER = 1
    var OFFERRIDE = 2
    var strdate =""
    var strtime =""
    var strseat =""
    var strType = "find_ride"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(com.memu.R.layout.home_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setUpdateNotiTokenAPIObserver()
        setFindTripAPIObserver()

        gpsTracker = GPSTracker(activity)
        if(gpsTracker?.canGetLocation()!!) {

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

        initSearchFab()
        initSearchFabDest()

        System.out.println("getAccountName "+UserInfoManager.getInstance(activity!!).getAccountName())
        name.setText(UserInfoManager.getInstance(activity!!).getAccountName())
        date.text = SimpleDateFormat("EEE,\nMMM dd").format(System.currentTimeMillis())
        time.text = SimpleDateFormat("hh:mm\na").format(System.currentTimeMillis())
        seatstv.text = "01\nSeats"
        find_ride.backgroundTintList = activity?.resources?.getColorStateList(R.color.LightBlue)
        offer_ride.backgroundTintList = activity?.resources?.getColorStateList(R.color.White)

        strdate =  SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        strtime =  SimpleDateFormat("hh:mm a").format(System.currentTimeMillis())
        strseat = "01"

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

    fun CallApi() {
        var latitude = gpsTracker?.latitude
        var longitude = gpsTracker?.longitude
        if(selectedCarmenFeatureSrc != null) {
            latitude = (selectedCarmenFeatureSrc!!.geometry() as com.mapbox.geojson.Point).latitude()
            longitude = (selectedCarmenFeatureSrc!!.geometry() as com.mapbox.geojson.Point).longitude()
        }
        val from = FromJSon(latitude!!,longitude!!)
        val latng1 = com.google.android.gms.maps.model.LatLng(latitude,longitude)
        val latng2 = com.google.android.gms.maps.model.LatLng( (selectedCarmenFeatureDest!!.geometry() as com.mapbox.geojson.Point).latitude(),
            (selectedCarmenFeatureDest!!.geometry() as com.mapbox.geojson.Point).longitude())

        val distance = BaseHelper.showDistance(latng1,latng2)
        val To = FromJSon((selectedCarmenFeatureDest!!.geometry() as com.mapbox.geojson.Point).latitude(),
            (selectedCarmenFeatureDest!!.geometry() as com.mapbox.geojson.Point).longitude())
        postFindRideViewModel.loadData(strdate,strtime, strseat,"no","",
            from,To,distance.toInt().toString(),strType)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.rlBestRoute -> {
                bestRouteUI()
                Keys.MAPTYPE = Keys.SHORTESTROUTE
            }

            R.id.rlpooling -> {
                poolingUI()
                Keys.MAPTYPE = Keys.POOLING
            }
            R.id.rlcab -> {

            }
            R.id.rlprofile -> {

            }
            R.id.seatstv -> {
                spinner()

            }
            R.id.btnNExt -> {
                if(!BaseHelper.isEmpty(edtdestLoc.text.toString())) {
                    edtdestLocerror.visibility = View.GONE
                    if(Keys.MAPTYPE == Keys.SHORTESTROUTE) {
                        home().setFragment(MapFragment().apply {
                            src = selectedCarmenFeatureSrc
                            dest = selectedCarmenFeatureDest
                        })
                    } else {

                        CallApi()
                    }

                } else {
                    edtdestLocerror.visibility = View.VISIBLE
                }

            }
            R.id.cancel,R.id.arrow_left -> {
                reset()

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
                    DatePickerDialog(activity, dateSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()
                }
            }
            R.id.time -> {
                TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }
            R.id.find_ride -> {
                strType = "find_ride"
                seatstv.text = "01\nSeats"
                type = FINDRIDER
                find_ride.backgroundTintList = activity?.resources?.getColorStateList(R.color.LightBlue)
                offer_ride.backgroundTintList = activity?.resources?.getColorStateList(R.color.White)
            }
            R.id.offer_ride -> {
                seatstv.text = "01\nSeats"
                strType = "offer_ride"
                type = OFFERRIDE
                find_ride.backgroundTintList = activity?.resources?.getColorStateList(R.color.White)
                offer_ride.backgroundTintList = activity?.resources?.getColorStateList(R.color.LightBlue)
            }
        }
    }

    val cal = Calendar.getInstance()

    val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)

        time.text = SimpleDateFormat("hh:mm\na").format(cal.time)
        strtime = SimpleDateFormat("hh:mm a").format(cal.time)
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

    }

    fun poolingUI() {
        popUpView.visibility = View.VISIBLE
        arrow_left.visibility = View.VISIBLE
        llPooling.visibility = View.VISIBLE
        cancel.visibility = View.GONE
        offer_take_ride.visibility = View.VISIBLE
        rlTopBar.visibility = View.GONE
        home_map_bg.alpha = 0.5f
        rlpooling.alpha = 1f
        rlcab.alpha = 0.5f
        rlprofile.alpha = 0.5f
        rlBestRoute.alpha = 0.5f
        scrollView.scrollTo(0,Helper.dpToPx(activity!!,200))

    }

    fun bestRouteUI() {
        popUpView.visibility = View.VISIBLE
        cancel.visibility = View.GONE
        rlTopBar.visibility = View.GONE
        offer_take_ride.visibility = View.GONE
        llPooling.visibility = View.GONE
        arrow_left.visibility = View.VISIBLE
        home_map_bg.alpha = 0.5f
        rlpooling.alpha = 0.5f
        rlcab.alpha = 0.5f
        rlprofile.alpha = 0.5f
        rlBestRoute.alpha = 1f
        scrollView.fullScroll(View.FOCUS_UP);

    }

    private fun initSearchFab() {
        val destinationPoint = com.mapbox.geojson.Point.fromLngLat(gpsTracker!!.longitude, gpsTracker!!.latitude)
        edtScrLoc.setOnClickListener {
            val intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(
                    if (Mapbox.getAccessToken() != null) Mapbox.getAccessToken()!! else getString(
                        R.string.map_box_access_token))
                .placeOptions(
                    PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .country("IN")
                        .proximity(destinationPoint)
                        .build(PlaceOptions.MODE_CARDS)
                )
                .build(activity)
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }
    }

    private fun initSearchFabDest() {
        val destinationPoint = com.mapbox.geojson.Point.fromLngLat(gpsTracker!!.longitude, gpsTracker!!.latitude)

        edtdestLoc.setOnClickListener {
            val intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(
                    if (Mapbox.getAccessToken() != null) Mapbox.getAccessToken()!! else getString(
                        R.string.map_box_access_token))
                .placeOptions(
                    PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .country("IN")
                        .limit(100000)
                        .proximity(destinationPoint)
                        .build(PlaceOptions.MODE_CARDS)
                )
                .build(activity)
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETEDEST)
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
                            home().setFragment(MapFragment().apply {
                                src = selectedCarmenFeatureSrc
                                dest = selectedCarmenFeatureDest
                                if(postFindRideViewModel.obj?.trip_id != null) {
                                    trip_rider_id = postFindRideViewModel.obj?.trip_id!!
                                } else {
                                    trip_rider_id = postFindRideViewModel.obj?.trip_rider_id!!
                                }
                                this.type = strType
                            })
                        }
                    }
                })
            }
        }
    }

    fun FromJSon(lattitude : Double,longitude : Double) : JSONObject {
        val getAddress = getAddress(lattitude,longitude)
        val jsonObject = JSONObject()
        jsonObject.put("address_line1",getAddress?.get(0)?.getAddressLine(0))
        jsonObject.put("lattitude",lattitude.toString())
        jsonObject.put("longitude",longitude.toString())
        jsonObject.put("state",getAddress?.get(0)?.getAdminArea())
        jsonObject.put("formatted_address",getAddress?.get(0)?.getAddressLine(0))
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
                selectedCarmenFeatureSrc = PlaceAutocomplete.getPlace(data);
                edtScrLoc.setText(selectedCarmenFeatureSrc!!.placeName())
            }
            if (requestCode == REQUEST_CODE_AUTOCOMPLETEDEST) {
                selectedCarmenFeatureDest = PlaceAutocomplete.getPlace(data);
                edtdestLoc.setText(selectedCarmenFeatureDest!!.placeName())
            }
        } catch (e : Exception){

        }


    }

}
