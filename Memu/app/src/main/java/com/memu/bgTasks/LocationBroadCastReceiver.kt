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
import com.memu.ui.BaseFragment
import org.json.JSONObject
import java.io.IOException
import java.util.*


class LocationBroadCastReceiver : BroadcastReceiver() {
    var context : Context? = null
    var gpsTracker : GPSTracker?  = null

    override fun onReceive(context: Context, intent: Intent) {
        println("LocationBroadCastReceiver Alarm Manager just ran")
        this.context = context
        gpsTracker = GPSTracker(context!!)
        if(gpsTracker!!.canGetLocation()) {
            BaseFragment.postUpdateLocationViewModel.loadData(Location())
        }

    }


    fun getAddress(): List<Address>? {
        val geocoder: Geocoder
        var addresses: List<Address>? = null
        geocoder = Geocoder(context, Locale.getDefault())

        try {
            addresses = geocoder.getFromLocation(gpsTracker?.latitude!!, gpsTracker?.longitude!!, 1)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addresses

    }

    fun Location() : JSONObject {
        val gpsTracker = GPSTracker(context!!)
        val obj = JSONObject()

        obj.put("country", getAddress()?.get(0)?.countryName)

        obj.put("state", getAddress()?.get(0)?.getAdminArea())

        obj.put("city", getAddress()?.get(0)?.locality)
        obj.put("location",getAddress()?.get(0)?.subLocality)
        obj.put("pincode", getAddress()?.get(0)?.postalCode)
        obj.put("lattitude",gpsTracker.latitude.toString())
        obj.put("longitude", gpsTracker.longitude.toString())
        obj.put("formatted_address", getAddress()?.get(0)?.getAddressLine(0))

        return obj
    }

}

