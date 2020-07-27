package com.memu.etc

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import com.iapps.deera.etc.OTPExpiryListener
import com.memu.ui.BaseFragment
import java.util.*
import java.util.concurrent.TimeUnit

open class CountDownTimerHelper {
    private var mCountDownTimer: CountDownTimer? = null
    private val emptyTimer: CountDownTimer? = null
    private var min: Long = -1
    private var sec:Long = -1
    private var smslimitTimer:Long = 0
    private var tvTimer: TextView? = null
    private val customView: View? = null
    private var message: Int = 0
    private var message2: String = ""
    private var mend: Int = 0
    private var mstart: Int = 0
    private var mOTPExpirtyListener: OTPExpiryListener? = null
    private var OTPExpiryTimeStamp: Long = 0
    private var OTPduration:Long = 0
    var isTimerOn = false

    fun showCountDownTimer(
        listener: OTPExpiryListener,
        context: Context,
        timer: TextView,
        string: String,
        start: Int,
        end: Int
    ) {
        mOTPExpirtyListener = listener

        try {
            tvTimer = timer
            message2 = string
            mend = end
            mstart = start
            OTPExpiryTimeStamp = UserInfoManager.getInstance(context).getOTPExpiryTimeStamp()
            OTPduration = UserInfoManager.getInstance(context).getOtpDuration()

            if (OTPExpiryTimeStamp == 0L) {
                if (mCountDownTimer == null) {
                    buildTimer(context)
                    return
                }
            }

            val nowCal = Calendar.getInstance()
            val timeNow = nowCal.timeInMillis

            if (timeNow > OTPExpiryTimeStamp) {
                // beyond duration
                mOTPExpirtyListener!!.onExpiry()
                mCountDownTimer = emptyTimer
            } else {

                if (mCountDownTimer == null) {
                    buildTimer(context)

                } else {
                    buildTimer(context)
                }
            }


        } catch (e: Exception) {
        }

    }

    @SuppressLint("StaticFieldLeak")
    fun buildTimer(context: Context?) {
        try {
            executeParalelPlease(object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg p0: Void?): Void? {
                    smslimitTimer = UserInfoManager.getInstance(context!!).getOtpDuration()
                    return null
                }

                override fun onPostExecute(aVoid: Void?) {
                    super.onPostExecute(aVoid)
                    if (!isTimerOn) {
                        if (smslimitTimer != 0L) {
                            isTimerOn = true
                            mCountDownTimer = object : CountDownTimer(smslimitTimer, 1000) {
                                override fun onTick(millisUntilFinished: Long) {

                                    min = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                                    sec =
                                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                                        )
                                    if (tvTimer != null) {
                                        val str = String.format(message2,min,sec)
                                        try {
                                            tvTimer!!.text = str
                                        } catch (e : java.lang.Exception) {
                                        }
                                    }
                                }

                                override fun onFinish() {
                                    isTimerOn = false
                                    mOTPExpirtyListener!!.onExpiry()

                                }

                            }
                            cancelCountDownTimer()
                            mCountDownTimer!!.start()
                        }
                    }


                }
            })
        } catch (e : RuntimeException){

        }

    }

    fun executeParalelPlease(task: AsyncTask<Void, Void, Void>) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            } else {
                task.execute()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun cancelCountDownTimer(){
        if(mCountDownTimer != null) {
            mCountDownTimer!!.cancel();
        }
    }
}