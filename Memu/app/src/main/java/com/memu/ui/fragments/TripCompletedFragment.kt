package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import com.facebook.AccessToken
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.facebook.CallbackManager
import com.facebook.Profile.getCurrentProfile
import com.facebook.internal.ImageRequest.getProfilePictureUri
import com.squareup.picasso.Picasso
import android.util.Log
import java.util.*
import org.json.JSONException
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import kotlinx.android.synthetic.main.trip_complete_dialog_fragment.*


class TripCompletedFragment : BaseFragment()  {

    var distanceCompleted = ""
    var timeTaken = ""
    var coinsEarned = ""
    var amountPaid = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.trip_complete_dialog_fragment, container, false)
        return v
    }

    override fun onBackTriggered() {
        home().backToMainScreen()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        distance.setText(distanceCompleted+" Km")
        time.setText(timeTaken)
        amount.setText(amountPaid+"\nRupees")
        coins.setText(coinsEarned+"\nCoins")
    }

}
