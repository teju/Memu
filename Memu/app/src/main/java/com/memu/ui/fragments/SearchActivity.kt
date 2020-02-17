package com.memu.ui.fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.memu.R
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ArrayAdapter
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.memu.etc.GPSTracker
import kotlinx.android.synthetic.main.activity_search.*
import com.google.android.libraries.places.api.model.Place
import java.util.Arrays.asList
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment
import java.util.*
import kotlin.collections.ArrayList


class SearchActivity : AppCompatActivity() {
    private val AUTOCOMPLETE_REQUEST_CODE: Int = 2001
    var arrayList: ArrayList<String> = ArrayList()
    private var gpsTracker: GPSTracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        gpsTracker = GPSTracker(this)
        if(gpsTracker?.canGetLocation()!!) {
            initUI()
        }
    }

    private fun initUI() {
        val apiKey = getString(R.string.api_key)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        //search()

    }

}
