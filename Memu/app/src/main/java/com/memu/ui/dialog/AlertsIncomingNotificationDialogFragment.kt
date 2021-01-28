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
import com.memu.modules.mapFeeds.MapFeed
import com.memu.ui.adapters.AlertsAdapter
import kotlinx.android.synthetic.main.alerts_noti_dialog_fragment.*
import java.lang.Exception

class AlertsIncomingNotificationDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.alerts_noti_dialog_fragment
    var notify_tittle:String = ""
    var notify_userNAme:String = ""
    var notify_image:String = ""
    var notify_logo:String = ""
    companion object {
        val TAG = "NotifyDialogFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
        val POINT_ALERT = 2
    }

    lateinit var listener: NotifyListener
    var  mapfeed : List<MapFeed> = listOf()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tittle.text = notify_tittle
        username.text = notify_userNAme
        Helper.loadImage(activity!!,notify_image,profile_pic,R.drawable.default_profile_icon)
        Helper.loadImage(activity!!,notify_logo,logo)
        like_btn.setOnClickListener {
            listener.onButtonClicked(BUTTON_POSITIVE)
            dismiss()
        }
        dislike_btn.setOnClickListener {
            listener.onButtonClicked(BUTTON_NEGATIVE)
            dismiss()
        }
        point_alert.setOnClickListener {
            listener.onButtonClicked(POINT_ALERT)
            dismiss()
        }
        try {
            Handler().postDelayed(
                {
                    dismiss()
                },
                8000 // value in milliseconds
            )
        } catch (e : Exception){

        }
    }


}