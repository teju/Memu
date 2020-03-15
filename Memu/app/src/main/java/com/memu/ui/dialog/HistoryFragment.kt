package com.memu.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.iapps.gon.etc.callback.RequestListener
import com.memu.R
import com.memu.modules.completedRides.Completed
import com.memu.modules.riderList.Rider
import com.memu.ui.adapters.HistoryAdapter
import kotlinx.android.synthetic.main.history_fragment.*

class HistoryFragment : DialogFragment() {

    var v: View? = null
    val DATEPICKERFRAGMENT_LAYOUT = R.layout.history_fragment

    companion object {
        val TAG = "HistoryFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
    }

    lateinit var listener: RequestListener
    var compltedRides: List<Completed> = listOf()
    var rider_list : List<Rider>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            it.window.setBackgroundDrawableResource(R.color.transparent)
            it.window.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 10))

        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val history = HistoryAdapter(activity!!,compltedRides)
        history.type = HistoryAdapter.TYPE_SCHEDULED
        recyclerView.adapter = history
        completed.setOnClickListener {
            history.type = HistoryAdapter.TYPE_COMPLETED
            history.notifyDataSetChanged()
        }
        upcoming.setOnClickListener {
            history.type = HistoryAdapter.TYPE_SCHEDULED
            history.notifyDataSetChanged()
        }
    }

}