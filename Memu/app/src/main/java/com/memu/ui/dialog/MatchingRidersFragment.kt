package com.memu.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.RequestListener
import com.iapps.libs.helpers.BaseHelper
import com.memu.R
import com.memu.etc.Keys
import com.memu.modules.TripGivers.Pooler
import com.memu.modules.riderList.Rider
import com.memu.ui.adapters.MatchingRidersAdapter
import com.memu.webservices.PostRequestRideViewModel
import com.memu.webservices.PostnviteRideGiversViewModel
import kotlinx.android.synthetic.main.map_fragment.*
import kotlinx.android.synthetic.main.matching_riders.*

class MatchingRidersFragment : DialogFragment() {

    var v: View? = null
    val DATEPICKERFRAGMENT_LAYOUT = R.layout.matching_riders

    companion object {
        val TAG = "NotifyDialogFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
    }

    lateinit var listener: RequestListener
    var rider_list : List<Rider>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            it.window?.setBackgroundDrawableResource(R.color.transparent)
            //it.window.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20))

        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = MatchingRidersAdapter( rider_list!!,activity!!)
        (recyclerView.adapter as MatchingRidersAdapter).MeAdapterListener = object  : MatchingRidersAdapter.MedapterListener{
            override fun onClick(position: Int) {
                listener.let {
                    listener.onButtonClicked(rider_list!!.get(position).user_id!!, rider_list!!.get(position).id!!)
                }
                dismiss()
            }
        }
    }

}