package com.memu.ui.fragments

import android.content.DialogInterface
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*
import org.json.JSONException
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.memu.etc.SpacesItemDecoration
import com.memu.modules.userMainData.UserMainData
import com.memu.ui.adapters.FollowersAdapter
import com.memu.ui.adapters.FriendsAdapter
import com.memu.ui.adapters.PostsAdapter
import kotlinx.android.synthetic.main.followers_request_fragment.*
import kotlinx.android.synthetic.main.profile_header.*

class FollowersRequestFragment : BaseFragment() ,View.OnClickListener {

    var isFriendsRequest = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.followers_request_fragment, container, false)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        arrow_left.setOnClickListener(this)
        ll_options.visibility = View.GONE

        followers.layoutManager = LinearLayoutManager(activity)
        val followersAdapter = FollowersAdapter(activity!!)
        followersAdapter.isFriendsRequest = isFriendsRequest
        followers.adapter = followersAdapter

        me_followers.layoutManager = LinearLayoutManager(activity)
        val mefollowersAdapter = FollowersAdapter(activity!!)
        mefollowersAdapter.isFriendsRequest = false
        me_followers.adapter = mefollowersAdapter

        if(isFriendsRequest) {
            imgRight.setImageResource(R.drawable.myfriends)
            tvTxt1.text = "My friends List"
            txtfollow.text = ""

        } else {
            imgRight.setImageResource(R.drawable.notificationsmain)
            tvTxt1.text = "My Followers list"
            txtfollow.text = "I Follow"

        }
        setUSerMAinDataAPIObserver()
        posUserMainDataViewModel.loadData()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.find_more -> {
                home().setFragment(FriendsFragment())
            }
            R.id.arrow_left -> {
                home().onBackPressed()
            }
            R.id.arrow_left -> {
                home().onBackPressed()
            }
        }
    }

}
