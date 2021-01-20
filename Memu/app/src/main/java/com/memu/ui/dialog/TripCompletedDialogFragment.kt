package com.memu.ui.dialog

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.R
import com.memu.etc.SpacesItemDecoration
import com.memu.modules.mapFeeds.MapFeed
import com.memu.ui.adapters.AlertsAdapter

class TripCompletedDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.trip_completed

    companion object {
        val TAG = "NotifyDialogFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
    }

    lateinit var listener: NotifyListener
    var  mapfeed : List<MapFeed> = listOf()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler().postDelayed(
            {
                listener.onButtonClicked(BUTTON_NEGATIVE)
                dismiss()
            },
            2000 // value in milliseconds
        )
    }

}