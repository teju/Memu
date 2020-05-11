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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*
import org.json.JSONException
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.memu.etc.SpacesItemDecoration
import com.memu.modules.userMainData.UserMainData
import com.memu.ui.adapters.FriendsAdapter
import com.memu.ui.adapters.PostsAdapter
import kotlinx.android.synthetic.main.friends_fragment.*
import kotlinx.android.synthetic.main.profile_header.*

class FriendsFragment : BaseFragment() ,View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.friends_fragment, container, false)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        tvfollowers.setOnClickListener(this)
        tvFriends.setOnClickListener(this)
        arrow_left.setOnClickListener(this)
        val sglm2 = GridLayoutManager(context, 4)
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
        setUSerMAinDataAPIObserver()
        posUserMainDataViewModel.loadData()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.arrow_left -> {
                home().onBackPressed()
            }
            R.id.tvfollowers -> {
                home().setFragment(FollowersRequestFragment())
            }
            R.id.tvFriends -> {
                home().setFragment(FollowersRequestFragment())
            }
        }
    }

}
