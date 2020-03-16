package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iapps.logs.com.pascalabs.util.log.helper.Helper
import com.memu.R
import com.memu.modules.completedRides.Completed
import kotlinx.android.synthetic.main.recurring_item.view.*
import android.widget.LinearLayout
import com.iapps.gon.etc.callback.RecursiveListener

class RecurringListAdapter(
    val context: Context) : RecyclerView.Adapter<RecurringListAdapter.ViewHolder>()  {

    var Rides: List<Completed> = listOf()
     var screenWidth: Int = 0
    lateinit var listener: RecursiveListener

    var type = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recurring_item, parent, false))
    }

    override fun getItemCount(): Int {
        return Rides.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.pos = position
        try {

            holder.root.setLayoutParams(
                LinearLayout.LayoutParams(
                    screenWidth - com.memu.etc.Helper.dpToPx(context, 85),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

        } catch (e: Exception) {
            Helper.logException(null, e)
        }

        holder.dateTime.text = Rides.get(position).time
        holder.srcLoc.text = Rides.get(position).from_address.address_line1
        holder.destLoc.text = Rides.get(position).to_address.address_line1
        holder.days.text = Rides.get(position).days
        holder.edit_icon.setOnClickListener {
            listener.let {
                listener.onButtonClicked(Rides.get(holder.pos))
            }
        }

    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var root = view.root
        var dateTime = view.dateTime
        var srcLoc = view.srcLoc
        var destLoc = view.destLoc
        var days = view.days
        var edit_icon = view.edit_icon


    }


}