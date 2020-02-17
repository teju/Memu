package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.modules.AlertsModule
import kotlinx.android.synthetic.main.alerts_item.view.*


class AlertsAdapter(val context: Context) : RecyclerView.Adapter<AlertsAdapter.ViewHolder>()  {

    var productAdapterListener : ProductAdapterListener? = null
    var obj : ArrayList<AlertsModule> =  ArrayList<AlertsModule>()
    interface ProductAdapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.alerts_item, parent, false))
    }

    override fun getItemCount(): Int {
        return obj.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position
        holder.clRoot!!.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                productAdapterListener?.let {
                    it.onClick(holder.pos)
                }
            }
        })
        holder.img.setImageResource(obj.get(holder.pos).image)
        holder.tittle.setText(obj.get(holder.pos).tittle)

    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var clRoot = view.clRoot
        val img =  view.img
        val tittle =  view.tittle
        init {
        }
    }


}