package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.checksum.CheckSum
import com.memu.modules.profileWall.ProfileWall
import org.json.JSONObject

class GetCheckSumViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: CheckSum? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(callback_url : String,cust_id:String,order_id:String) {
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
                        obj = gson.fromJson(response!!.content.toString(), CheckSum::class.java)
                        trigger.postValue(NEXT_STEP)
                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.getPaytmCheckSum)
        Helper.applyHeader(apl,genericHttpAsyncTask)
        var paytm_params = JSONObject()
        paytm_params.put(Keys.MID,"IDacRJsO55733339470443")
        paytm_params.put(Keys.ORDER_ID,order_id)
        paytm_params.put(Keys.CUST_ID,cust_id)
        paytm_params.put(Keys.CHANNEL_ID,"WAP")
        paytm_params.put(Keys.TXN_AMOUNT,"1.00")
        paytm_params.put(Keys.WEBSITE,"WEBSTAGING")
        paytm_params.put(Keys.CALLBACK_URL,callback_url)
        paytm_params.put(Keys.INDUSTRY_TYPE_ID,"Retail")
        genericHttpAsyncTask.setPostParams(Keys.USER_ID,UserInfoManager.getInstance(apl!!).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.PAYTM_PARAMS, paytm_params)

        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}