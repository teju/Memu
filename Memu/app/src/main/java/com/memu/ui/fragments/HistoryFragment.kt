package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import com.memu.ui.adapters.HistoryAdapter
import com.memu.webservices.PostScheduledCompleteRidesViewModel
import kotlinx.android.synthetic.main.history_fragment.*
import android.text.style.UnderlineSpan
import android.text.SpannableString
import android.util.DisplayMetrics
import android.widget.TextView
import com.iapps.gon.etc.callback.RecursiveListener
import com.memu.etc.Helper
import com.memu.etc.Keys
import com.memu.etc.UserInfoManager
import com.memu.modules.completedRides.Completed
import com.memu.ui.adapters.RecurringListAdapter
import com.memu.webservices.PostRecurryingRidesViewModel
import androidx.core.os.HandlerCompat.postDelayed
import android.os.Handler


class HistoryFragment : BaseFragment() ,View.OnClickListener {


    lateinit var postScheduledCompleteRidesViewModel: PostScheduledCompleteRidesViewModel
    lateinit var postRecurryingRidesViewModel: PostRecurryingRidesViewModel
    var history:HistoryAdapter? = null
    var recurringListAdapter:RecurringListAdapter? = null
    val completedContent = SpannableString("Completed &\nCancelled")
    val scheduledContent = SpannableString("Scheduled &\nUpcoming")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.history_fragment, container, false)
        return v
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        initUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setCompletedAPIObserver()
        setRecuringAPIObserver()
        history = HistoryAdapter(activity!!)
        history!!.listener = object  : RecursiveListener {
            override fun onButtonClicked(which: Completed) {
                if(which.type.equals("offer_ride",ignoreCase = true)) {
                    Keys.MAPTYPE = Keys.POOLING_OFFER_RIDE
                } else {
                    Keys.MAPTYPE = Keys.POOLING_FIND_RIDE
                }
                home().setFragment(MapFragment().apply {
                    srcLat = which.from_address.lattitude.toDouble()
                    srcLng = which.from_address.longitude.toDouble()
                    destLng = which.to_address.longitude.toDouble()
                    destLat = which.to_address.lattitude.toDouble()
                    this.type = which.type
                    this.trip_rider_id = which.id
                })
            }

        }
        recurringListAdapter = RecurringListAdapter(activity!!)
        recurringListAdapter!!.listener = object  : RecursiveListener {
            override fun onButtonClicked(which: Completed) {
                home().setFragment(MapFragment().apply {
                    Keys.MAPTYPE = Keys.RECURSIVE_EDIT
                    srcLat = which.from_address.lattitude.toDouble()
                    srcLng = which.from_address.longitude.toDouble()
                    destLng = which.to_address.longitude.toDouble()
                    destLat = which.to_address.lattitude.toDouble()
                    this.type = which.type
                    this.recursivedays = which.days
                    this.completed = which
                })
            }

        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recurringrecyclerView.layoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.HORIZONTAL, false)
        try {
            Helper.loadImage(activity!!,UserInfoManager.getInstance(activity!!).getProfilePic(),profile_pic,R.drawable.default_profile_icon)
        } catch (e : java.lang.Exception){ }
        completed.setOnClickListener (this)
        upcoming.setOnClickListener (this)
        create_new.setOnClickListener (this)
        arrow_left.setOnClickListener (this)
        history!!.type = HistoryAdapter.TYPE_SCHEDULED
        scheduledContent.setSpan(UnderlineSpan(), 0, scheduledContent.length, 0)
        upcoming.setText(scheduledContent)
        removeSpam(completedContent,completed)
        postScheduledCompleteRidesViewModel.loadData(history!!.type)
        postRecurryingRidesViewModel.loadData()

    }

    fun removeSpam(SpannableString : SpannableString,tv : TextView) {
        val ss = SpannableString
        val uspans = ss.getSpans(0, ss.length, UnderlineSpan::class.java)
        for (us in uspans) {
            ss.removeSpan(us)
        }
        tv.text = ss
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.completed -> {
                tvSubTitle.text = "Completed Rides"
                completedContent.setSpan(UnderlineSpan(), 0, completedContent.length, 0)
                completed.setText(completedContent)
                removeSpam(scheduledContent,upcoming)
                history!!.type = HistoryAdapter.TYPE_COMPLETED
                postScheduledCompleteRidesViewModel.loadData(history!!.type)
            }
            R.id.upcoming -> {
                tvSubTitle.text = "Upcoming Rides"
                history!!.type = HistoryAdapter.TYPE_SCHEDULED
                scheduledContent.setSpan(UnderlineSpan(), 0, scheduledContent.length, 0)
                upcoming.setText(scheduledContent)
                removeSpam(completedContent,completed)
                postScheduledCompleteRidesViewModel.loadData(history!!.type)
            }
            R.id.arrow_left -> {
                onBackTriggered()
            }
            R.id.create_new -> {
                Keys.MAPTYPE = Keys.POOLING_BACK
                onBackTriggered()
            }
        }
    }

    fun setCompletedAPIObserver() {
        postScheduledCompleteRidesViewModel = ViewModelProviders.of(this).get(PostScheduledCompleteRidesViewModel::class.java).apply {
            this@HistoryFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    no_list_found.text = s.message!!
                    no_list_found.visibility = View.VISIBLE

                    recyclerView.visibility = View.GONE
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostScheduledCompleteRidesViewModel.NEXT_STEP -> {
                            recyclerView.visibility = View.VISIBLE
                            no_list_found.visibility = View.GONE

                            if(history!!.type == HistoryAdapter.TYPE_COMPLETED) {
                                history!!.Rides =
                                    postScheduledCompleteRidesViewModel.obj!!.completed_list
                            } else {
                                history!!.Rides =
                                    postScheduledCompleteRidesViewModel.obj!!.scheduled_list
                            }
                            recyclerView.adapter = history
                        }
                    }
                })
            }
        }
    }
    fun setRecuringAPIObserver() {
        postRecurryingRidesViewModel = ViewModelProviders.of(this).get(PostRecurryingRidesViewModel::class.java).apply {
            this@HistoryFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                   recurringrecyclerView.visibility = View.GONE
                    tvRecuring.visibility = View.GONE
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostRecurryingRidesViewModel.NEXT_STEP -> {
                            System.out.println("onBindViewHolder ")
                            val displayMetrics = DisplayMetrics()
                            activity!!.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
                            val screenWidth = displayMetrics.widthPixels
                            recurringrecyclerView.visibility = View.VISIBLE
                            tvRecuring.visibility = View.VISIBLE
                            recurringListAdapter?.Rides = postRecurryingRidesViewModel.obj!!.scheduled_list
                            recurringListAdapter?.screenWidth = screenWidth
                            recurringrecyclerView.adapter = recurringListAdapter

                        }
                    }
                })
            }
        }
    }

}
