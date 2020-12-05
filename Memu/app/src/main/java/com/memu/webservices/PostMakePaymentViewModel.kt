package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.helpers.BaseHelper
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.checksum.Payment
import com.memu.modules.checksum.WalletBalance
import com.memu.modules.profileWall.ProfileWall
import org.json.JSONObject

class PostMakePaymentViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: Payment? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(mode : String,credit_amount:String,wallet_balance : String,
                 driver_id:String,trip_id:String,payment_mode:String,invoice_id:String,amount:String,status:String) {
        genericHttpAsyncTask = Helper.GenericHttpAsyncTask(object : Helper.GenericHttpAsyncTask.TaskListener {

            override fun onPreExecute() {
                isLoading.postValue(true)
            }

            override fun onPostExecute(response: Response?) {
                isLoading.postValue(false)

                if (!Helper.isNetworkAvailable(apl)) {
                    isNetworkAvailable.postValue(false)
                    return
                }

                val json = checkResponse(response, apl)

                if (json != null) {
                    try {
                        val gson = GsonBuilder().create()
                        obj = gson.fromJson(response!!.content.toString(), Payment::class.java)
                        if (obj!!.status.equals(Keys.STATUS_CODE)) {
                        }
                        trigger.postValue(GetUserWallViewModel.NEXT_STEP)

                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.postPay)
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.setPostParams(Keys.USER_ID,UserInfoManager.getInstance(apl!!).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.MODE,mode)
        val wallet_details = JSONObject()
        wallet_details.put(Keys.CREDIT_AMOUNT,credit_amount)
        wallet_details.put(Keys.WALLET_BALANCE,wallet_balance)
        genericHttpAsyncTask.setPostParams(Keys.WALLET_DETAILS,wallet_details)
        val payment_details = JSONObject()
        if(!BaseHelper.isEmpty(driver_id)) {
            payment_details.put(Keys.CUSTOMER_ID,UserInfoManager.getInstance(apl!!).getAccountId())
            payment_details.put(Keys.DRIVER_ID,driver_id)
            payment_details.put(Keys.TRIP_ID,trip_id)
            payment_details.put(Keys.PAYMENT_MODE,payment_mode)
            payment_details.put(Keys.INVOICE_ID,invoice_id)
            payment_details.put(Keys.AMOUNT,amount)
            payment_details.put(Keys.STATUS,status)
        }
        genericHttpAsyncTask.setPostParams(Keys.PAYMENT_DETAILS,payment_details)

        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()
    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}