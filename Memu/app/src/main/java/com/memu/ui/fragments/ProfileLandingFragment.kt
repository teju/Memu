package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.memu.R
import com.memu.etc.SpacesItemDecoration
import com.memu.etc.UserInfoManager
import com.memu.modules.friendList.FriendList
import com.memu.modules.friendList.User
import com.memu.ui.BaseFragment
import com.memu.ui.adapters.FriendsAdapter
import com.memu.webservices.PostFriendListViewModel
import kotlinx.android.synthetic.main.profile_header.*
import kotlinx.android.synthetic.main.profile_landing_fragment.*
import java.util.*


class ProfileLandingFragment : BaseFragment(), PostFriendListViewModel.FriendsSearchResListener,View.OnClickListener  {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.profile_landing_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setUSerMAinDataAPIObserver()
        setFriendListAPIObserver()
        ll_options.visibility = View.GONE
        initFriendsAdapter()

        profile_wall.setOnClickListener(this)
        walletView.setOnClickListener(this)
        rep_coins.setOnClickListener(this)
        settings.setOnClickListener(this)
        find_more.setOnClickListener(this)
        rlInvite.setOnClickListener(this)

        posUserMainDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getAccountId())
        postFriendListViewModel.loadData("FR", "", 0, this, UserInfoManager.getInstance(activity!!).getAccountId())
        postFriendListViewModel.loadData("FR", "", 1, this, UserInfoManager.getInstance(activity!!).getAccountId())
    }

    fun initFriendsAdapter() {
        val sglm2 = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_grid1)
        friens_rl.setLayoutManager(sglm2)
        friens_rl.setNestedScrollingEnabled(false)
        friens_rl.addItemDecoration(SpacesItemDecoration(3, spacingInPixels, true))

    }

    override fun onHiddenChanged(hidden: Boolean) {

        posUserMainDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getAccountId())
        postFriendListViewModel.loadData("FR", "", 0, this, UserInfoManager.getInstance(activity!!).getAccountId())
        postFriendListViewModel.loadData("FR", "", 1, this, UserInfoManager.getInstance(activity!!).getAccountId())
    }
    override fun onResult(result: FriendList?, searchByLoc: Int) {
        if(result?.user_list?.size == 0){
            friens_rl.visibility = View.GONE
        } else{
            friens_rl.visibility = View.VISIBLE

        }
        if(searchByLoc == 0) {
            val adapter = FriendsAdapter(context!!)
            if(result?.user_list?.size != 0) {
                adapter.obj = result?.user_list as ArrayList<User>
            } else {
                adapter.obj = ArrayList()
            }
            friens_rl.adapter = adapter
            (friens_rl.adapter as FriendsAdapter).productAdapterListener =
                object : FriendsAdapter.ProductAdapterListener {
                    override fun onClick(position: Int) {
                        home().setFragment(ProfileWallFragment().apply {
                            friend_id = result?.user_list.get(position)?.freind_id!!
                            isPubLicWall = true
                        })
                    }
                }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.profile_wall -> {
                home().setFragment(ProfileWallFragment())
            }
            R.id.walletView -> {
                home().setFragment(WalletFragment())
            }
            R.id.rep_coins -> {

            }
            R.id.settings -> {

            }
            R.id.find_more -> {
                home().setFragment(FriendsFragment())
            }
            R.id.rlInvite -> {
                referFriend()
            }
        }
    }

}
