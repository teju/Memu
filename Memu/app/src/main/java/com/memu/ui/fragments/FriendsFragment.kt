package com.memu.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.memu.etc.SpacesItemDecoration
import com.memu.ui.adapters.FriendsAdapter
import com.memu.webservices.PosUSerSearchViewModel
import kotlinx.android.synthetic.main.friends_fragment.*
import kotlinx.android.synthetic.main.profile_header.*
import androidx.lifecycle.Observer
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.etc.UserInfoManager
import com.memu.modules.userSearch.User
import kotlin.collections.ArrayList

class FriendsFragment : BaseFragment() ,View.OnClickListener {
    private var adapter: FriendsAdapter? = null
    lateinit var posUSerSearchViewModel: PosUSerSearchViewModel

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
        adapter = FriendsAdapter(context!!)
        friens_rl.adapter = adapter
        (friens_rl.adapter as FriendsAdapter).productAdapterListener =
            object : FriendsAdapter.ProductAdapterListener {
                override fun onClick(position: Int) {
                        home().setFragment(ProfileWallFragment().apply {
                            user_id = posUSerSearchViewModel.obj?.user_list?.get(position)?.id!!
                            isPubLicWall = true
                        })
                }
            }
        setUSerMAinDataAPIObserver()
        setSearchUserAPIObserver()
        posUserMainDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getAccountId())
        serach.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                posUSerSearchViewModel.loadData(p0.toString())

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
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

    fun setSearchUserAPIObserver() {
        posUSerSearchViewModel = ViewModelProviders.of(this).get(PosUSerSearchViewModel::class.java).apply {
            this@FriendsFragment.let { thisFragReference ->
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
                    if( posUSerSearchViewModel.obj?.user_list!!.size != 0) {
                        adapter?.obj = posUSerSearchViewModel.obj?.user_list!! as ArrayList<User>
                    } else {
                        adapter?.obj = ArrayList<User>()
                    }
                    adapter?.notifyDataSetChanged()

                })
            }
        }
    }

}
