package com.memu.ui.dialog

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.memu.R
import kotlinx.android.synthetic.main.find_ride_popup.*
import android.widget.ArrayAdapter
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.iapps.libs.helpers.BaseUIHelper.loadImage


class FindRideDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.find_ride_popup

    companion object {
        val TAG = "FindRideDialogFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
    }

    lateinit var listener: NotifyListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_positive.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                listener.let {
                    listener.onButtonClicked(BUTTON_POSITIVE)
                }
                dismiss()
            }
        })
        showVehicleSpinner()
        showRateSpinner()
    }

    fun showVehicleSpinner() {
        val list = ArrayList<String>()
        list.add("KA 05 A 1234")

        val dataAdapter = ArrayAdapter<String>(
            activity,
            android.R.layout.simple_spinner_item, list
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        select_vehicle.setAdapter(dataAdapter)
    }

    fun showRateSpinner() {
        val list = ArrayList<String>()
        list.add("01")
        list.add("02")
        list.add("03")

        val dataAdapter = ArrayAdapter<String>(
            activity,
            android.R.layout.simple_spinner_item, list
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        select_rate.setAdapter(dataAdapter)
    }
}