package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.modules.TripGivers.Pooler
import com.memu.modules.riderList.Rider
import kotlinx.android.synthetic.main.matching_item.view.*

class MatchingRidersAdapter(val poolers : List<Rider>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {
    lateinit var MeAdapterListener : MedapterListener

    interface MedapterListener {
        fun onClick(position:Int)
    }
    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return poolers.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.matching_item, parent, false)
        val v = ViewHolder(view)
        return ViewHolder(view).listen { pos, type ->
            MeAdapterListener.onClick(pos)
            //TODO do other stuff here
        }
        return v

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.name?.text = poolers.get(position).name
        holder?.matching_buddy_time?.text = poolers.get(position).time
        if(poolers.get(position).route_per != null) {
            holder.routell.visibility = View.VISIBLE
            holder.pay_ll.visibility = View.VISIBLE
            var route_per = poolers.get(position).route_per!!
            holder.route_percent.text = route_per+"%"
        }
    }
}
fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(getAdapterPosition(), getItemViewType())
    }
    return this
}
class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val profile_pic = view.profile_pic
    val name = view.name
    val occupation = view.occupation
    val route_percent = view.route_percent
    val matching_buddy_time = view.matching_buddy_time
    val amount_to_pay = view.amount_to_pay
    val request = view.request
    val routell = view.routell
    val pay_ll = view.pay_ll

}