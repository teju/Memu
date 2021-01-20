package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.etc.Helper
import com.memu.modules.mapFeeds.MapFeed
import com.memu.modules.top_earners.CityEarner
import kotlinx.android.synthetic.main.item_city_friends.view.*


class CityFriendsAdapter(var obj : List<CityEarner>,val context: Context) : RecyclerView.Adapter<CityFriendsAdapter.ViewHolder>()  {

    interface ProductAdapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_city_friends, parent, false))
    }

    override fun getItemCount(): Int {
        return obj.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.pos = position
        holder.name.setText(obj.get(position).name)
        holder.points.setText(obj.get(position).total_points)
        Helper.loadImage(context,obj.get(position).photo.original_path,holder.profile_pic,R.drawable.default_profile_icon)
    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var name = view.name
        var points = view.points
        var profile_pic = view.profile_pic

    }


}