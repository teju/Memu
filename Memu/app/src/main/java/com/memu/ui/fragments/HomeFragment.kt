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
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.PermissionListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponent
import com.memu.bgTasks.LocationBroadCastReceiver
import com.memu.etc.GPSTracker
import com.memu.etc.UserInfoManager
import com.memu.webservices.GetVehicleTypeViewModel
import com.memu.webservices.PostUpdateLocationViewModel
import com.memu.webservices.PostUpdateNotiTokenViewModel
import kotlinx.android.synthetic.main.home_fragment.ld
import org.json.JSONObject
import java.io.IOException


class HomeFragment : BaseFragment() , View.OnClickListener {
    private var pendingIntent: PendingIntent? = null
    private var alarmManager: AlarmManager? = null
    private var gpsTracker: GPSTracker? = null
    lateinit var postUpdateNotiTokenViewModel: PostUpdateNotiTokenViewModel

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
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.rlBestRoute -> {
                bestRouteUI()
            }
            R.id.rlpooling -> {
                poolingUI()

            }
            R.id.rlcab -> {

            }
            R.id.rlprofile -> {

            }
            R.id.btnNExt -> {
                home().setFragment(MapFragment())

            }
            R.id.cancel -> {
                reset()

            }
        }
    }

    fun reset() {
        popUpView.visibility = View.GONE
        llPooling.visibility = View.GONE
        cancel.visibility = View.GONE
        rlTopBar.visibility = View.VISIBLE
        home_map_bg.alpha = 1f
        rlpooling.alpha = 1f
        rlcab.alpha = 1f
        rlprofile.alpha = 1f
        rlBestRoute.alpha = 1f
    }

    fun poolingUI() {
        popUpView.visibility = View.VISIBLE
        llPooling.visibility = View.VISIBLE
        cancel.visibility = View.VISIBLE
        rlTopBar.visibility = View.GONE
        home_map_bg.alpha = 0.5f
        rlpooling.alpha = 1f
        rlcab.alpha = 0.5f
        rlprofile.alpha = 0.5f
        rlBestRoute.alpha = 0.5f
    }

    fun bestRouteUI() {
        popUpView.visibility = View.VISIBLE
        cancel.visibility = View.VISIBLE
        rlTopBar.visibility = View.GONE
        llPooling.visibility = View.GONE
        home_map_bg.alpha = 0.5f
        rlpooling.alpha = 0.5f
        rlcab.alpha = 0.5f
        rlprofile.alpha = 0.5f
        rlBestRoute.alpha = 1f
    }

    override fun onBackTriggered() {
        home().exitApp()
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


}
