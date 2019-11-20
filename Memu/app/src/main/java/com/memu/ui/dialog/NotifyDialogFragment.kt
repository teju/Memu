package com.memu.ui.dialog

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.memu.R
import kotlinx.android.synthetic.main.generic_dialog.*

class NotifyDialogFragment : BaseDialogFragment() {

    val DATEPICKERFRAGMENT_LAYOUT = R.layout.generic_dialog

    companion object {
        val TAG = "NotifyDialogFragment"
        val BUTTON_POSITIVE = 1
        val BUTTON_NEGATIVE = 0
    }
    var notify_tittle : String? = ""
    var notify_messsage : String? = ""
    var button_positive : String? = ""
    var button_negative : String? = ""
    var useHtml = false
    lateinit var listener: NotifyListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(DATEPICKERFRAGMENT_LAYOUT, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(BaseHelper.isEmpty(notify_tittle)){
            vw_title.visibility = View.GONE
        }else{
            vw_title.visibility = View.VISIBLE
            vw_title.text = notify_tittle
        }

        if(!useHtml){
            vw_text.text = notify_messsage
        }else{
            vw_text.text = Html.fromHtml(notify_messsage)
        }

        btn_positive.text = button_positive
        btn_negative.text = button_negative
        if(btn_negative.text != "") {
            btn_negative.visibility = View.VISIBLE
        } else {
            btn_negative.visibility = View.GONE
        }
        btn_positive.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                listener.let {
                    listener.onButtonClicked(BUTTON_POSITIVE)
                }
                dismiss()
            }
        })
        btn_negative.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                listener.let {
                    listener.onButtonClicked(BUTTON_NEGATIVE)
                }
                dismiss()
            }
        })

    }

}