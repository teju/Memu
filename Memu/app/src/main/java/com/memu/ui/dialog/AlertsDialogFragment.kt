package com.memu.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.R
import com.memu.etc.SpacesItemDecoration
import com.memu.modules.mapFeeds.MapFeed
import com.memu.ui.adapters.AlertsAdapter
import kotlinx.android.synthetic.main.alerts_dialog_fragment.*

class AlertsDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.alerts_dialog_fragment

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
        alertsRecyclerView()

    }
    fun alertsRecyclerView() {
       /* var obj : ArrayList<AlertsModule> =  ArrayList<AlertsModule>()

        var alertsmodule = AlertsModule()
        alertsmodule.tittle = "Signal Camera"
        alertsmodule.image = R.drawable.signal_camera
        obj.add(alertsmodule)

        alertsmodule = AlertsModule()
        alertsmodule.tittle = "Road Blocked"
        alertsmodule.image = R.drawable.road_blocked
        obj.add(alertsmodule)

        alertsmodule = AlertsModule()
        alertsmodule.tittle = "Report a Crash"
        alertsmodule.image = R.drawable.crash_report
        obj.add(alertsmodule)

        alertsmodule = AlertsModule()
        alertsmodule.tittle = "Road Work"
        alertsmodule.image = R.drawable.road_work
        obj.add(alertsmodule)

        alertsmodule = AlertsModule()
        alertsmodule.tittle = "Add a Shortcut"
        alertsmodule.image = R.drawable.short_cut

        obj.add(alertsmodule)
        alertsmodule = AlertsModule()
        alertsmodule.tittle = "Expecting Traffic jam"
        alertsmodule.image = R.drawable.traffic_jam

        obj.add(alertsmodule)
        alertsmodule = AlertsModule()
        alertsmodule.tittle = "Ambulance Alert"
        alertsmodule.image = R.drawable.ambulance_alert

        obj.add(alertsmodule)
        alertsmodule = AlertsModule()
        alertsmodule.tittle = "Add Signal"
        alertsmodule.image = R.drawable.add_signal

        obj.add(alertsmodule)
        alertsmodule = AlertsModule()
        alertsmodule.tittle = "Police"
        alertsmodule.image = R.drawable.police

        obj.add(alertsmodule)
*/
        val sglm2 = GridLayoutManager(context, 3)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_grid1)
        alertList.setLayoutManager(sglm2)
        alertList.setNestedScrollingEnabled(false)
        alertList.addItemDecoration(SpacesItemDecoration(3, spacingInPixels, true))
        val adapter = AlertsAdapter(context!!)
        adapter.obj = mapfeed as ArrayList<MapFeed>
        alertList.adapter = adapter
        (alertList.adapter as AlertsAdapter).productAdapterListener =
            object : AlertsAdapter.ProductAdapterListener {
                override fun onClick(position: Int) {
                    listener.let {
                        listener.onButtonClicked(position)
                    }
                    dismiss()
                }
            }
        arrow_left.setOnClickListener {
            dismiss()
        }
    }

}