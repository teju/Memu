package com.memu.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Color.GREEN
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.etc.Helper
import com.memu.modules.friendList.User
import kotlinx.android.synthetic.main.followers_item.view.*


class PendingFriendsAdapter(val context: Context) : RecyclerView.Adapter<PendingFriendsAdapter.ViewHolder>()  {

    var productAdapterListener : ProductAdapterListener? = null
    var obj : ArrayList<User> =  ArrayList<User>()
    var isFriendsRequest =  false
    interface ProductAdapterListener {
        fun onClick(position:Int,status : String)
        fun onClickOfList(position:Int,status : String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.followers_item, parent, false))
    }

    override fun getItemCount(): Int {
        return obj.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position
        if(isFriendsRequest) {
            holder.rl.backgroundTintList = context.getResources().getColorStateList(R.color.mapboxPink)
            holder.accept_remove.text = "Accept"
        }
        holder.user_name.text = obj.get(position).name
        try {
            Helper.loadImage(
                context,
                obj.get(position).photo.original_path,
                holder.profile_icon,
                R.drawable.default_profile_icon
            )
        } catch (e : Exception){

        }
        holder.accept_remove.setOnClickListener {
            productAdapterListener?.onClick(holder.pos,"Accepted")
        }
        holder.clRoot.setOnClickListener {
            productAdapterListener?.onClickOfList(holder.pos,"Remove")
        }
    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var llfollowers_rect = view.llfollowers_rect
        var clRoot = view.clRoot
        var accept_remove = view.accept_remove
        var user_name = view.user_name
        var profile_icon = view.profile_icon
        var rl = view.rl


    }


}