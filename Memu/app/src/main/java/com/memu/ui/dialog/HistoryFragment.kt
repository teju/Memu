package com.memu.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.iapps.gon.etc.callback.RequestListener
import com.memu.R
import com.memu.modules.riderList.Rider

class HistoryFragment : DialogFragment() {

    var v: View? = null
    val DATEPICKERFRAGMENT_LAYOUT = R.layout.history_fragment

    companion object {
        val TAG = "HistoryFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
    }

    lateinit var listener: RequestListener
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            it.window.setBackgroundDrawableResource(R.color.transparent)
            it.window.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 10))

        }

    }

}