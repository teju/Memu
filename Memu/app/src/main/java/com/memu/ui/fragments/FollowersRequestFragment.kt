package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.memu.etc.UserInfoManager
import com.memu.ui.adapters.FollowersAdapter
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
        posUserMainDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getAccountId())
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
