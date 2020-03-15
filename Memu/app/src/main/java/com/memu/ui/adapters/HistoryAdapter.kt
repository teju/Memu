package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.modules.completedRides.Completed
import kotlinx.android.synthetic.main.history_item.view.*


class HistoryAdapter(
    val context: Context,
    val compltedRides: List<Completed>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>()  {

    companion object {
        var TYPE_SCHEDULED = 1001
        var TYPE_COMPLETED = 1002
    }
    var type = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.history_item, parent, false))
    }

    override fun getItemCount(): Int {
        return compltedRides.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position
        when(type) {
            TYPE_COMPLETED -> {
                holder.bottom_view.visibility = View.VISIBLE
                holder.dateTime.text = compltedRides.get(position).date +" "+compltedRides.get(position).time
                holder.srcLoc.text = compltedRides.get(position).from_address.address_line1
                holder.destLoc.text = compltedRides.get(position).to_address.address_line1
            }
            TYPE_SCHEDULED -> {
                holder.bottom_view.visibility = View.GONE

            }
        }
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

    }


}