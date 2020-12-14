package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.etc.Helper
import com.memu.modules.friendList.User
import kotlinx.android.synthetic.main.friends_item.view.*


class FriendsAdapter(val context: Context) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>()  {

    var productAdapterListener : ProductAdapterListener? = null
    var obj : ArrayList<User> =  ArrayList<User>()
    interface ProductAdapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.friends_item, parent, false))
    }

    override fun getItemCount(): Int {
        return obj.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position
        try {
            Helper.loadImage(context!!,obj.get(position).photo.thumb_path,holder.friend_img,R.drawable.default_profile_icon)
        } catch (e : java.lang.Exception){
        }
        holder.friend_img.setOnClickListener {
            productAdapterListener?.onClick(holder.pos)
        }
    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var clRoot = view.clRoot
        var friend_img = view.friend_img

    }


}