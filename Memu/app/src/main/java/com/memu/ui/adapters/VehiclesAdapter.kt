package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.modules.userDetails.Vehicle
import kotlinx.android.synthetic.main.item_address.view.*


class VehiclesAdapter(val context: Context) : RecyclerView.Adapter<VehiclesAdapter.ViewHolder>()  {

    var obj : ArrayList<Vehicle> = ArrayList()
    interface ProductAdapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_address, parent, false))
    }

    override fun getItemCount(): Int {
        return obj.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.pos = position
        holder.brandTxt.setText(obj.get(position).vehicle_brand)
        holder.registrationTxt.setText(obj.get(position).vehicle_no)
        holder.name.setText(obj.get(position).vehicle_name)
        holder.modelTxt.setText(obj.get(position).vehicle_model_type)
        if(obj.get(position).vehicle_type == 2) {
            holder.whiteboard.isChecked = true
            holder.yellowboard.isChecked = false
        } else{
            holder.whiteboard.isChecked = false
            holder.yellowboard.isChecked = true
        }
        if(obj.get(position).showCancel) {
            holder.rlCancel.visibility = View.VISIBLE
        } else {
            holder.rlCancel.visibility = View.GONE
        }
        holder.rlCancel.setOnClickListener {
            obj.get(position).showCancel = false
            obj.removeAt(position)
            notifyDataSetChanged()
        }
        holder.yellowboard.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                holder.whiteboard.isChecked = false
            }
        }
        holder.whiteboard.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                holder.yellowboard.isChecked = false
            }
        }
    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var registrationTxt  = view.registration
        var name  = view.name
        var brandTxt  = view.brand
        var modelTxt  = view.model
        var yellowboard  = view.yellowboard
        var whiteboard  = view.whiteboard
        var rlCancel  = view.rlCancel

    }


}