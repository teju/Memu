package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iapps.gon.etc.callback.RecursiveListener
import com.iapps.logs.com.pascalabs.util.log.helper.Helper
import com.memu.R
import com.memu.modules.completedRides.Completed
import com.memu.modules.completedRides.MatchedBudy
import com.memu.modules.mapFeeds.MapFeed
import kotlinx.android.synthetic.main.alerts_dialog_fragment.*
import kotlinx.android.synthetic.main.history_fragment.*
import kotlinx.android.synthetic.main.history_item.view.*
import java.text.SimpleDateFormat


class HistoryAdapter(
    val context: Context) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>()  {
    lateinit var listener: RecursiveListener

    var Rides: List<Completed> = listOf()
    companion object {
        var TYPE_SCHEDULED = 1001
        var TYPE_COMPLETED = 1002
    }
    var type = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.history_item, parent, false))
    }

    override fun getItemCount(): Int {
        return Rides.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position
        when(type) {
            TYPE_COMPLETED -> {
                holder.bottom_view.visibility = View.VISIBLE
                holder.coins_earned.text = "Coins Earned : "+Rides.get(position).coins_earned
                holder.coins_spent.text = "Coins Spent : "+Rides.get(position).coins_spent
            }
            TYPE_SCHEDULED -> {
                holder.bottom_view.visibility = View.GONE
            }

        }
        val dob = com.memu.etc.Helper.parseDate( Rides.get(position).date.trim(), "yyyy-MM-dd")

        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy")
        val strDate = dateFormat.format(dob)
        holder.dateTime.text = strDate +" "+Rides.get(position).time
        holder.srcLoc.text = Rides.get(position).from_address.address_line1
        holder.destLoc.text = Rides.get(position).to_address.address_line1

        holder.root.setOnClickListener {
            listener.let {
                listener.onButtonClicked(Rides.get(holder.pos))
            }
        }
        holder.matching_buddies.layoutManager = LinearLayoutManager(context)
        val sglm2 = GridLayoutManager(context, 2)
        holder.matching_buddies.setLayoutManager(sglm2)
        holder.matching_buddies.setNestedScrollingEnabled(false)
        val adapter = MatchingBuddiesAdapter(context!!)
        adapter.obj = Rides.get(holder.pos).matched_budies as ArrayList<MatchedBudy>
        holder.matching_buddies.adapter = adapter
    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var root = view.root
        var bottom_view = view.bottom_view
        var dateTime = view.dateTime
        var srcLoc = view.srcLoc
        var viaLoc = view.viaLoc
        var destLoc = view.destLoc
        var coins_earned = view.coins_earned
        var coins_spent = view.coins_spent
        var matching_buddies = view.matching_buddies


    }


}