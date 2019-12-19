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
import kotlinx.android.synthetic.main.map_fragment.*

import org.json.JSONObject
import java.io.IOException


class MapFragment : BaseFragment() , View.OnClickListener {

    private var gpsTracker: GPSTracker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(com.memu.R.layout.map_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        gpsTracker = GPSTracker(activity)
        if(gpsTracker?.canGetLocation()!!) {
            showMAp()
        }

    }

    fun showMAp() {
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS, object : Style.OnStyleLoaded {
                override fun onStyleLoaded(@NonNull style: Style) {

                    val locationComponent = mapboxMap.locationComponent

                    // Activate with a built LocationComponentActivationOptions object
                    locationComponent.activateLocationComponent(
                        LocationComponentActivationOptions.builder(
                            activity!!,
                            style
                        ).build()
                    )

                    // Enable to make component visible
                    locationComponent.isLocationComponentEnabled = true

                    // Set the component's camera mode
                    locationComponent.cameraMode = CameraMode.TRACKING

                    // Set the component's render mode
                    locationComponent.renderMode = RenderMode.COMPASS

                    val position = CameraPosition.Builder()
                        .target(LatLng(gpsTracker?.latitude!!, gpsTracker?.longitude!!)) // Sets the new camera position
                        .zoom(15.0) // Sets the zoom
                        .build(); // Creates a CameraPosition from the builder

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 7000);

                }
            })
        }
        mapView.onStart();

    }
    override fun onClick(v: View?) {

    }



}
