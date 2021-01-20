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
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*
import org.json.JSONException
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.etc.UserInfoManager
import com.memu.ui.adapters.CityFriendsAdapter
import com.memu.ui.adapters.MatchingRidersAdapter
import com.memu.webservices.GetTopEarnersViewModel
import com.memu.webservices.PostFindRideViewModel
import kotlinx.android.synthetic.main.profile_header.*
import kotlinx.android.synthetic.main.referral_details_fragment.*
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import com.iapps.gon.etc.callback.WalletBalanceListener
import com.memu.modules.checksum.WalletBalance


class ReferralDetailsFragment : BaseFragment() ,View.OnClickListener , WalletBalanceListener {


    lateinit var getTopEarnersViewModel: GetTopEarnersViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.referral_details_fragment, container, false)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFindTripAPIObserver()
        setWalletBalanceObserver(this)
        initUI();
    }

    private fun initUI() {
        ll_options.visibility = View.GONE
        setUSerMAinDataAPIObserver()
        posUserMainDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getAccountId())
        arrow_left.setOnClickListener(this)
        getTopEarnersViewModel.loadData()
        getWalletBalanceViewModel.loadData()
    }

    fun initAdapter() {
        city_rv.layoutManager = LinearLayoutManager(activity)
        friends_rv.layoutManager = LinearLayoutManager(activity)
        city_rv.adapter = CityFriendsAdapter(getTopEarnersViewModel.obj?.city_earners!!,activity!!)
        friends_rv.adapter = CityFriendsAdapter(getTopEarnersViewModel.obj?.friend_earners!!,activity!!)
    }

    fun setFindTripAPIObserver() {
        getTopEarnersViewModel = ViewModelProviders.of(this).get(GetTopEarnersViewModel::class.java).apply {
            this@ReferralDetailsFragment.let { thisFragReference ->
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
                    this@ReferralDetailsFragment.initAdapter()
                })
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.arrow_left -> {
                home().proceedDoOnBackPressed()
            }
        }
    }

    override fun walletBalanceResponse(balance: WalletBalance) {
        reputation_coins.setText(balance.referral_balance)
       if(balance.referral_balance.toInt() >= 10000) {
           levels.setText("01")
       } else if(balance.referral_balance.toInt() >= 20000) {
           levels.setText("02")

       } else if(balance.referral_balance.toInt() >= 30000) {
           levels.setText("03")
       }
       else if(balance.referral_balance.toInt() >= 40000) {
           levels.setText("04")

       }
       else if(balance.referral_balance.toInt() >= 50000) {
           levels.setText("05")

       }
       else if(balance.referral_balance.toInt() >= 60000) {
           levels.setText("06")

       }
       else if(balance.referral_balance.toInt() >= 70000) {
           levels.setText("07")

       }
       else if(balance.referral_balance.toInt() >= 80000) {
           levels.setText("08")

       }
       else if(balance.referral_balance.toInt() >= 90000) {
           levels.setText("09")

       }
       else if(balance.referral_balance.toInt() >= 100000) {
           levels.setText("10")

       }
    }
}
