package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.memu.R
import com.memu.modules.PlaceHolder
import kotlinx.android.synthetic.main.history_item.view.*


class HistoryAdapter(val context: Context) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>()  {

    companion object {
        var TYPE_SCHEDULED = 1001
        var TYPE_COMPLETED = 1002
    }
    var type = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.history_item, parent, false))
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position
        when(type) {
            TYPE_COMPLETED -> {
                holder.bottom_view.visibility = View.VISIBLE
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

    }


}