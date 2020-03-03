package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.modules.PlaceHolder
import kotlinx.android.synthetic.main.week_item_adapter.view.*


class WeekAdapter(val context: Context) : RecyclerView.Adapter<WeekAdapter.ViewHolder>()  {

    var productAdapterListener : ProductAdapterListener? = null
    var obj : ArrayList<PlaceHolder> =  ArrayList<PlaceHolder>()
    var weekdays : java.util.ArrayList<String> = java.util.ArrayList<String>()

    interface ProductAdapterListener {
        fun onClick(position: String, checked: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.week_item_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return obj.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position

        holder.checkBox.setText(obj.get(holder.pos).name)
        if(weekdays.contains(obj.get(holder.pos).name)) {
            holder.checkBox.setBackgroundColor(context.resources.getColor(R.color.White))
        } else {
            holder.checkBox.setBackgroundColor(context.resources.getColor(R.color.transparent))
        }
        holder.checkBox.setOnClickListener {
            productAdapterListener?.let {
                if(weekdays.contains(obj.get(holder.pos).name)) {
                    holder.checkBox.setBackgroundColor(context.resources.getColor(R.color.transparent))
                    it.onClick(obj.get(holder.pos).name, false)
                } else {
                    holder.checkBox.setBackgroundColor(context.resources.getColor(R.color.White))
                    it.onClick(obj.get(holder.pos).name, true)
                }

            }
        }
    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var clRoot = view.clRoot

        val checkBox =  view.checkBox
        init {
        }
    }


}