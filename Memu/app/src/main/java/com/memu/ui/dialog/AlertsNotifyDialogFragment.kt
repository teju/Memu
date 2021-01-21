package com.memu.ui.dialog

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.R
import com.memu.etc.Helper
import com.memu.etc.SpacesItemDecoration
import com.memu.etc.UserInfoManager
import com.memu.modules.mapFeeds.MapFeed
import com.memu.ui.adapters.AlertsAdapter
import kotlinx.android.synthetic.main.alerts_notiy_dialog_fragment.*

class AlertsNotifyDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.alerts_notiy_dialog_fragment
    var coinsReceived = ""
    var username = ""
    var title = ""
    var user_image = ""
    var isLiked = false
    var description = ""
    companion object {
        val TAG = "NotifyDialogFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
    }
    var didSendAlert = false
    lateinit var listener: NotifyListener
    var  mapfeed : List<MapFeed> = listOf()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(username.isEmpty()) {
            user_info.visibility = View.GONE
        } else{
            user_info.visibility = View.VISIBLE
        }
        name.setText(username)
        header_title.setText(title)
        desc.setText(description)
        coins.setText(coinsReceived)
        try {
            Helper.loadImage(activity!!,
                user_image,profile_pic,R.drawable.user_default)

        } catch (e : java.lang.Exception){

        }
        if(!username.isEmpty()) {
            if (isLiked) {
                like_dislike_btn.setImageResource(R.drawable.like_btn)
            } else {
                like_dislike_btn.setImageResource(R.drawable.dislike_btn)
                desc.setTextColor(resources.getColor(R.color.Red))
            }
        }
        Handler().postDelayed(
            {
                //dismiss()
            },
            5000 // value in milliseconds
        )
    }

}