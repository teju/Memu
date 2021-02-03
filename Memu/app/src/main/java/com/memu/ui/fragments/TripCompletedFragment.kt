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
import androidx.lifecycle.ViewModelProviders
import java.util.*
import org.json.JSONException
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.ui.activity.MockNavigationFragment
import com.memu.webservices.PostCustomerEndNavigationViewModel
import com.memu.webservices.PostEndNavigationViewModel
import com.memu.webservices.PostTripSummaryViewModel
import kotlinx.android.synthetic.main.trip_complete_dialog_fragment.*
import androidx.lifecycle.Observer
import com.memu.etc.Helper
import com.memu.etc.Keys
import com.memu.etc.UserInfoManager



class TripCompletedFragment : BaseFragment()  {
    var ID :String = ""
    lateinit var postTripSummaryViewModel: PostTripSummaryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.trip_complete_dialog_fragment, container, false)
        return v
    }

    override fun onBackTriggered() {
        home().backToMainScreen()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCustomerEndTripAPIObserver()
        var type = ""
        if (Keys.MAPTYPE == Keys.POOLING_OFFER_RIDE) {
            type = "offer_ride"
        } else {
            type = "find_ride"
        }
        try {
            Helper.loadImage(activity!!,
                UserInfoManager.getInstance(activity!!).getProfilePic(),profile_pic,R.drawable.user_default)

        } catch (e : java.lang.Exception){

        }
        arrow_left.setOnClickListener {
            home().backToMainScreen()
        }
        postTripSummaryViewModel.loadData(type,ID)
    }

    private fun initUI() {
        val tripSummary = postTripSummaryViewModel.obj
        distance.setText(tripSummary?.distance_travelled!!)
        time.setText(tripSummary?.time_taken.toString()!!)
        amount.setText(tripSummary.money_earned_spent)
        coins.setText(tripSummary.reputation_coin)
    }

    fun setCustomerEndTripAPIObserver() {
        postTripSummaryViewModel = ViewModelProviders.of(this).get(
            PostTripSummaryViewModel::class.java).apply {
            this@TripCompletedFragment.let { thisFragReference ->
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
                        PostTripSummaryViewModel.NEXT_STEP -> {
                            initUI()
                        }
                    }
                })
            }
        }
    }

}
