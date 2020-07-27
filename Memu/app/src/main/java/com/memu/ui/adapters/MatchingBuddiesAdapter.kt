package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.etc.Helper
import com.memu.modules.completedRides.MatchedBudy
import com.memu.modules.mapFeeds.MapFeed
import kotlinx.android.synthetic.main.matching_buddies_item.view.*


class MatchingBuddiesAdapter(val context: Context) : RecyclerView.Adapter<MatchingBuddiesAdapter.ViewHolder>()  {

    var productAdapterListener : ProductAdapterListener? = null
    var obj : ArrayList<MatchedBudy> =  ArrayList<MatchedBudy>()
    interface ProductAdapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.matching_buddies_item, parent, false))
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
        holder.matching_buddies_name.text = obj.get(holder.pos).name
        Helper.loadImage(context!!,obj.get(holder.pos).photo.original_path,holder.matching_buddies_img,R.drawable.default_profile_icon)

    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var clRoot = view.clRoot
        var matching_buddies_img = view.matching_buddies_img
        var matching_buddies_name = view.matching_buddies_name

    }


}