package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.memu.R
import com.memu.ui.BaseFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.etc.UserInfoManager
import com.memu.modules.friendList.FriendList
import com.memu.modules.friendList.User
import com.memu.ui.adapters.AcceptedFriendsAdapter
import com.memu.ui.adapters.PendingFriendsAdapter
import com.memu.webservices.PostAcceptFriendRequestViewModel
import com.memu.webservices.PostFollowersListViewModel
import com.memu.webservices.PostFriendListViewModel
import com.memu.webservices.PostPendingFriendListViewModel
import kotlinx.android.synthetic.main.followers_request_fragment.*
import kotlinx.android.synthetic.main.profile_header.*

class FollowersRequestFragment : BaseFragment() ,View.OnClickListener ,
    PostFriendListViewModel.FriendsSearchResListener {

    lateinit var postPendingFriendListViewModel: PostPendingFriendListViewModel
    lateinit var postFollowersListViewModel: PostFollowersListViewModel
    lateinit var postAcceptFriendRequestViewModel: PostAcceptFriendRequestViewModel

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
        setFriendRequestObserver()
        setFriendListAPIObserver()
        setAcceptFriendRequestObserver()
        setFollowersAPIObserver()

        arrow_left.setOnClickListener(this)
        ll_options.visibility = View.GONE
        followers.layoutManager = LinearLayoutManager(activity)
        me_followers.layoutManager = LinearLayoutManager(activity)

        if(isFriendsRequest) {
            imgRight.setImageResource(R.drawable.myfriends)
            tvTxt1.text = "My friends List"
            txtfollow.text = ""
            postPendingFriendListViewModel.loadData("FR","to_me")
            postFriendListViewModel.loadData("FR","",0,this)

        } else {
            imgRight.setImageResource(R.drawable.notificationsmain)
            tvTxt1.text = "My Followers list"
            txtfollow.text = "I Follow"
            postPendingFriendListViewModel.loadData("FL","by_me")
            postFollowersListViewModel.loadData("FL","to_me")
        }
        setUSerMAinDataAPIObserver()
        posUserMainDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getAccountId())

    }

    override fun onResult(result: FriendList?, searchByLoc: Int) {
        val followersAdapter = AcceptedFriendsAdapter(activity!!)
        followersAdapter.isFriendsRequest = isFriendsRequest
        if(result?.user_list?.size != 0) {
            followersAdapter.obj = result?.user_list as ArrayList<User>
        } else {
            followersAdapter.obj = ArrayList<User>()
        }
        followers.adapter = followersAdapter
        (followers.adapter as AcceptedFriendsAdapter).productAdapterListener =
            object : AcceptedFriendsAdapter.ProductAdapterListener {
                override fun onClick(position: Int,status : String) {
                    if(isFriendsRequest) {
                        postAcceptFriendRequestViewModel.loadData(
                            "FR",
                            result?.user_list.get(position).freind_id, status,"to_me")
                    } else {
                        postAcceptFriendRequestViewModel.loadData(
                            "FL",
                            result?.user_list.get(position).freind_id, "Remove","to_me")
                    }
                }

                override fun onClickOfList(position: Int, status: String) {
                    home().setFragment(ProfileWallFragment().apply {
                        friend_id =  result?.user_list?.get(position).freind_id
                        isPubLicWall = true
                    })
                }
            }
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

    fun setFriendRequestObserver() {
        postPendingFriendListViewModel = ViewModelProviders.of(this).get(PostPendingFriendListViewModel::class.java).apply {
            this@FollowersRequestFragment.let { thisFragReference ->
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
                    when (state) {
                        PostPendingFriendListViewModel.NEXT_STEP -> {
                            val mefollowersAdapter = PendingFriendsAdapter(activity!!)
                            mefollowersAdapter.isFriendsRequest = isFriendsRequest
                            if(postPendingFriendListViewModel.obj?.user_list?.size != 0) {
                                mefollowersAdapter.obj =
                                    postPendingFriendListViewModel.obj?.user_list as ArrayList<User>
                            } else {
                                mefollowersAdapter.obj =  ArrayList<User>()
                            }
                            me_followers.adapter = mefollowersAdapter
                            (me_followers.adapter as PendingFriendsAdapter).productAdapterListener =
                                object : PendingFriendsAdapter.ProductAdapterListener {
                                   override fun onClick(position: Int,status : String) {
                                       if(isFriendsRequest) {
                                           postAcceptFriendRequestViewModel.loadData(
                                               "FR",
                                               postPendingFriendListViewModel.obj!!.user_list.get(
                                                   position).freind_id, status,"to_me")
                                       } else {
                                           postAcceptFriendRequestViewModel.loadData(
                                               "FL",
                                               postPendingFriendListViewModel.obj!!?.user_list.get(position).freind_id,
                                               "Remove","by_me")
                                       }
                                   }

                                    override fun onClickOfList(position: Int, status: String) {
                                        home().setFragment(ProfileWallFragment().apply {
                                            friend_id =  postPendingFriendListViewModel.obj!!?.user_list?.get(position).freind_id
                                            isPubLicWall = true
                                        })
                                    }
                                }
                        }
                    }
                })

            }
        }
    }
    fun setFollowersAPIObserver() {
        postFollowersListViewModel = ViewModelProviders.of(this).get(PostFollowersListViewModel::class.java).apply {
            this@FollowersRequestFragment.let { thisFragReference ->
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
                    when (state) {
                        PostFollowersListViewModel.NEXT_STEP -> {
                            val mefollowersAdapter = PendingFriendsAdapter(activity!!)
                            mefollowersAdapter.isFriendsRequest = isFriendsRequest
                            if(postFollowersListViewModel.obj?.user_list?.size != 0) {
                                mefollowersAdapter.obj =
                                    postFollowersListViewModel.obj?.user_list as ArrayList<User>
                            } else {
                                mefollowersAdapter.obj = ArrayList()
                            }
                            followers.adapter = mefollowersAdapter
                            (followers.adapter as PendingFriendsAdapter).productAdapterListener =
                                object : PendingFriendsAdapter.ProductAdapterListener {
                                    override fun onClick(position: Int,status : String) {
                                        postAcceptFriendRequestViewModel.loadData("FL",
                                            postFollowersListViewModel.obj!!.user_list.get(position).freind_id,
                                            "Remove","to_me")
                                    }

                                    override fun onClickOfList(position: Int, status: String) {
                                        home().setFragment(ProfileWallFragment().apply {
                                            friend_id =  postFollowersListViewModel.obj!!?.user_list?.get(position).freind_id
                                            isPubLicWall = true
                                        })
                                    }
                                }
                        }
                    }
                })

            }
        }
    }

    fun setAcceptFriendRequestObserver() {
        postAcceptFriendRequestViewModel = ViewModelProviders.of(this).get(PostAcceptFriendRequestViewModel::class.java).apply {
            this@FollowersRequestFragment.let { thisFragReference ->
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
                    when (state) {
                        PostAcceptFriendRequestViewModel.NEXT_STEP -> {
                            System.out.println("PostPendingFriendListViewModel isFriendsRequest "+isFriendsRequest)
                            if (isFriendsRequest) {
                                postPendingFriendListViewModel.loadData("FR", "to_me")
                                postFriendListViewModel.loadData(
                                    "FR", "",
                                    0, this@FollowersRequestFragment)
                            } else {
                                postPendingFriendListViewModel.loadData("FL","by_me")
                                postFollowersListViewModel.loadData("FL","to_me")
                            }
                        }
                    }
                })
            }
        }
    }
}

