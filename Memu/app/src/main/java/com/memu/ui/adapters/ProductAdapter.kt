package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.memu.R
import kotlinx.android.synthetic.main.week_item_adapter.view.*


class ProductAdapter(val context: Context) : RecyclerView.Adapter<ProductAdapter.ViewHolder>()  {

    var productAdapterListener : ProductAdapterListener? = null
    var obj : ArrayList<String> =  ArrayList<String>()
    interface ProductAdapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        obj.add("Monday")
        obj.add("Tuesday")
        obj.add("Wednesday")
        obj.add("Thursday")
        obj.add("Friday")
        obj.add("Saturday")
        obj.add("Sunday")
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.week_item_adapter, parent, false))
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
        holder.checkBox.setText(obj.get(holder.pos))


    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var clRoot = view.clRoot

        val checkBox =  view.checkBox
        init {
        }
    }


}