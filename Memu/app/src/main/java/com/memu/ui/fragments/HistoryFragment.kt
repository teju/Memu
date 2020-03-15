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

import com.iapps.gon.etc.callback.NotifyListener
import com.memu.ui.adapters.HistoryAdapter
import com.memu.webservices.PostScheduledCompleteRidesViewModel
import kotlinx.android.synthetic.main.history_fragment.*
import android.text.style.UnderlineSpan
import android.text.SpannableString
import android.widget.TextView
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager


class HistoryFragment : BaseFragment() ,View.OnClickListener {


    lateinit var postScheduledCompleteRidesViewModel: PostScheduledCompleteRidesViewModel
    var history:HistoryAdapter? = null
    val completedContent = SpannableString("Completed &\nCancelled")
    val scheduledContent = SpannableString("Scheduled &\nUpcoming")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.history_fragment, container, false)
        return v
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setCompletedAPIObserver()
        history = HistoryAdapter(activity!!)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        try {
            Helper.loadImage(activity!!,UserInfoManager.getInstance(activity!!).getProfilePic(),profile_pic,R.drawable.user_default)

        } catch (e : java.lang.Exception){

        }
        completed.setOnClickListener (this)
        upcoming.setOnClickListener (this)
        arrow_left.setOnClickListener (this)
        history!!.type = HistoryAdapter.TYPE_SCHEDULED
        postScheduledCompleteRidesViewModel.loadData(history!!.type)

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
                completedContent.setSpan(UnderlineSpan(), 0, completedContent.length, 0)
                completed.setText(completedContent)
                removeSpam(scheduledContent,upcoming)
                history!!.type = HistoryAdapter.TYPE_COMPLETED
                postScheduledCompleteRidesViewModel.loadData(history!!.type)
            }
            R.id.upcoming -> {
                history!!.type = HistoryAdapter.TYPE_SCHEDULED
                scheduledContent.setSpan(UnderlineSpan(), 0, scheduledContent.length, 0)
                upcoming.setText(scheduledContent)
                removeSpam(completedContent,completed)
                postScheduledCompleteRidesViewModel.loadData(history!!.type)
            }
            R.id.arrow_left -> {
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

}
