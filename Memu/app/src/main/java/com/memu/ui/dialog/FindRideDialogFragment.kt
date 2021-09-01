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
import android.widget.AdapterView
import com.iapps.gon.etc.callback.FindRideDialogListener
import com.iapps.libs.helpers.BaseUIHelper.loadImage
import com.memu.modules.poolerVehicleList.Vehicle


class FindRideDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.find_ride_popup

    companion object {
        val TAG = "FindRideDialogFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
    }

    lateinit var listener: FindRideDialogListener
    var vehicle_list: List<Vehicle> = listOf()
    var selectedPos = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_positive.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                listener.let {
                    listener.onButtonClicked(selectedPos,select_rate.text.toString())
                }
                dismiss()
            }
        })
        showVehicleSpinner()
    }

    fun showVehicleSpinner() {
        val list = ArrayList<String>()
        for(x in vehicle_list) {
            list.add(x.vehicle_no)
        }

        val dataAdapter = ArrayAdapter<String>(
            activity!!,
            android.R.layout.simple_spinner_item, list
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        select_vehicle.setAdapter(dataAdapter)
        select_vehicle.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPos = position
                vehicle_name.setText(vehicle_list.get(selectedPos).vehicle_name)
                vehicle_brand.setText(vehicle_list.get(selectedPos).vehicle_brand)
            }

        }
        vehicle_name.setText(vehicle_list.get(selectedPos).vehicle_name)
        vehicle_brand.setText(vehicle_list.get(selectedPos).vehicle_brand)
    }

}