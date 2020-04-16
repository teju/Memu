package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.etc.Helper
import com.memu.modules.mapFeeds.MapFeed
import kotlinx.android.synthetic.main.followers_item.view.*


class FollowersAdapter(val context: Context) : RecyclerView.Adapter<FollowersAdapter.ViewHolder>()  {

    var productAdapterListener : ProductAdapterListener? = null
    var obj : ArrayList<MapFeed> =  ArrayList<MapFeed>()
    var isFriendsRequest =  false
    interface ProductAdapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.followers_item, parent, false))
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position
        if(isFriendsRequest) {
            holder.llfollowers_rect.background = context.getDrawable(R.drawable.followers_rect_green)
            holder.accept_remove.setTextColor(context.resources.getColor(R.color.Black))
            holder.accept_remove.text = "Accept"
        } else {
            holder.llfollowers_rect.background = context.getDrawable(R.drawable.followers_rect)
            holder.accept_remove.setTextColor(context.resources.getColor(R.color.Red))
            holder.accept_remove.text = "Remove"
        }

    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var clRoot = view.clRoot
        var accept_remove = view.accept_remove
        var llfollowers_rect = view.llfollowers_rect

    }


}