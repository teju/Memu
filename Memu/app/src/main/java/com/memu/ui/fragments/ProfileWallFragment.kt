package com.memu.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.memu.etc.SpacesItemDecoration
import com.memu.etc.UserInfoManager
import com.memu.ui.adapters.FriendsAdapter
import com.memu.ui.adapters.PostsAdapter
import com.memu.webservices.GetUserWallViewModel
import com.memu.webservices.PostUploadDocViewModel
import kotlinx.android.synthetic.main.profile_header.*
import kotlinx.android.synthetic.main.profile_wall.*

class ProfileWallFragment : BaseFragment() ,View.OnClickListener {

    lateinit var getUserWallViewModel: GetUserWallViewModel
    lateinit var postUploadDocViewModel: PostUploadDocViewModel
    val PICK_PHOTO_PHOTO = 10010
    var user_id = ""
    var isPubLicWall = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.profile_wall, container, false)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        find_more.setOnClickListener(this)
        arrow_left.setOnClickListener(this)
        tvfollowers.setOnClickListener(this)
        tvFriends.setOnClickListener(this)
        upload_activity.setOnClickListener(this)
        setGetActivitiesObserver()
        setUploadActivityPhotoObserver()
        setUSerMAinDataAPIObserver()

        val sglm2 = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_grid1)
        friens_rl.setLayoutManager(sglm2)
        friens_rl.setNestedScrollingEnabled(false)
        friens_rl.addItemDecoration(SpacesItemDecoration(3, spacingInPixels, true))
        val adapter = FriendsAdapter(context!!)
        friens_rl.adapter = adapter
        /*(friens_rl.adapter as FriendsAdapter).productAdapterListener =
            object : FriendsAdapter.ProductAdapterListener {
                override fun onClick(position: Int) {

                }
            }*/
        if(BaseHelper.isEmpty(user_id)) {
            user_id = UserInfoManager.getInstance(activity!!).getAccountId()
        } else {
            privateWallSettings()
        }

        getUserWallViewModel.loadData(user_id)
        posUserMainDataViewModel.loadData(user_id)
    }

    fun privateWallSettings() {
        upload_activity.visibility = View.GONE
        friends_map_bg.visibility = View.GONE
        llFriends.visibility = View.GONE
        find_more.visibility = View.GONE
    }

    override fun setUserMainData() {
        super.setUserMainData()
        if(isPubLicWall) {
            followers_cnt_.visibility = View.GONE
            messages_cnt.visibility = View.GONE
            friends_cnt.visibility = View.GONE
            tvFriends.text = "Add Friends"
            tvfollowers.text = "Follow"
            messages.text = "Message"
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
            R.id.tvfollowers -> {
                home().setFragment(FollowersRequestFragment())
            }
            R.id.upload_activity -> {
                pickImage()
            }
            R.id.tvFriends -> {
                home().setFragment(FollowersRequestFragment().apply {
                    isFriendsRequest = true

                })
            }
        }
    }

    fun setGetActivitiesObserver() {
        getUserWallViewModel = ViewModelProviders.of(this).get(GetUserWallViewModel::class.java).apply {
            this@ProfileWallFragment.let { thisFragReference ->
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
                getTrigger().observe(thisFragReference, Observer {
                    posts_rl.layoutManager = LinearLayoutManager(activity)
                    posts_rl.isNestedScrollingEnabled = false
                    val postsAdapter =  PostsAdapter(activity!!)
                    postsAdapter.obj = getUserWallViewModel.obj?.activities!!
                    posts_rl.adapter = postsAdapter
                })
            }
        }
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_PHOTO);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val imageuri = data?.getData();// Get intent
            val real_Path = BaseHelper.getRealPathFromUri(activity, imageuri);
            postUploadDocViewModel.loadData(PostUploadDocViewModel.ACTIVITY_PHOTO, real_Path)
        } catch (e: Exception) {
        }
    }

    fun setUploadActivityPhotoObserver() {
        postUploadDocViewModel = ViewModelProviders.of(this).get(PostUploadDocViewModel::class.java).apply {
            this@ProfileWallFragment.let { thisFragReference ->
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
                        PostUploadDocViewModel.NEXT_STEP -> {
                            getUserWallViewModel.loadData(user_id)
                        }
                    }
                })

            }
        }
    }

}
