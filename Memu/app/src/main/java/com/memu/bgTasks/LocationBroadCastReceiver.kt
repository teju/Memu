package com.memu.bgTasks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProviders
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.R

import com.memu.webservices.GetVehicleTypeViewModel
import com.memu.webservices.PostUpdateLocationViewModel
import kotlinx.android.synthetic.main.home_fragment.*
import android.location.Address
import android.location.Geocoder
import com.memu.etc.GPSTracker
import com.memu.etc.Helper
import com.memu.ui.BaseFragment
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.*


class LocationBroadCastReceiver : BroadcastReceiver() {
    var context : Context? = null
    var gpsTracker : GPSTracker?  = null

    override fun onReceive(context: Context, intent: Intent) {
        println("LocationBroadCastReceiver Alarm Manager just ran")
        this.context = context
        gpsTracker = GPSTracker(context!!)
        if(gpsTracker!!.canGetLocation()) {
            BaseFragment.postUpdateLocationViewModel?.loadData(Helper().CurrentLocation(context))
        }

    }

}

