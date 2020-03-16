package com.memu.ui.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.iapps.libs.helpers.BaseHelper
import com.memu.R
import com.memu.modules.PlaceHolder
import kotlinx.android.synthetic.main.week_item_adapter.view.*
import java.util.regex.Pattern


class WeekAdapter(val context: Context) : RecyclerView.Adapter<WeekAdapter.ViewHolder>()  {

    var productAdapterListener : ProductAdapterListener? = null
    var obj : ArrayList<PlaceHolder> =  ArrayList<PlaceHolder>()
    var weekdays : java.util.ArrayList<String> = java.util.ArrayList<String>()
    var isHome = true
    interface ProductAdapterListener {
        fun onClick(position: String, checked: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.week_item_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return obj.size
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position

        holder.checkBox.setText(obj.get(holder.pos).name)
        System.out.println("recursivedays onBindViewHolder "+weekdays)

        if(BaseHelper.containsIgnoreCase(weekdays,obj.get(holder.pos).name)) {
            if(isHome) {
                holder.checkBox.setBackgroundColor(context.resources.getColor(R.color.White))
            } else {
                holder.checkBox.setBackgroundResource(R.drawable.themegreen_rounded_rectangle)
            }
        } else {
            if(isHome) {
                holder.checkBox.setBackgroundColor(context.resources.getColor(R.color.transparent))
            } else {
                holder.checkBox.setBackgroundResource(R.drawable.themegrayrecursive_rounded_rectangle)
            }
        }
        holder.checkBox.setOnClickListener {
            productAdapterListener?.let {
                System.out.println("recursivedays "+BaseHelper.containsIgnoreCase(weekdays,obj.get(holder.pos).name))

                if(BaseHelper.containsIgnoreCase(weekdays,obj.get(holder.pos).name)) {
                    if(isHome) {
                        holder.checkBox.setBackgroundColor(context.resources.getColor(R.color.transparent))
                    } else {
                        holder.checkBox.setBackgroundResource(R.drawable.themegrayrecursive_rounded_rectangle)
                    }
                    it.onClick(obj.get(holder.pos).name, false)
                } else {
                    if(isHome) {
                        holder.checkBox.setBackgroundColor(context.resources.getColor(R.color.White))
                    } else {
                        holder.checkBox.setBackgroundResource(R.drawable.themegreen_rounded_rectangle)
                    }
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