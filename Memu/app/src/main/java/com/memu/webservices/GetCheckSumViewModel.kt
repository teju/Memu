package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.R
import com.memu.etc.*
import com.memu.modules.checksum.CheckSum
import com.memu.modules.checksum.WalletBalance
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

    fun loadData(cust_id:String,order_id:String,amt :Double) {
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
                        if (obj!!.status.equals(Keys.STATUS_CODE)) {
                            trigger.postValue(GetUserWallViewModel.NEXT_STEP)
                        }else{
                            errorMessage.value = createErrorMessageObject(response)

                        }
                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.getPaytmCheckSum)
        Helper.applyHeader(apl,genericHttpAsyncTask)
        var paytm_params = JSONObject()
        paytm_params.put(Keys.MID,apl.getString(R.string.mid))
        paytm_params.put(Keys.CUST_ID,UserInfoManager.getInstance(apl!!).getAccountId())
        paytm_params.put(Keys.ORDER_ID,order_id)
       // paytm_params.put(Keys.MOBILE_NO,"9964062237")
        paytm_params.put(Keys.CHANNEL_ID,"WAP")
        paytm_params.put(Keys.TXN_AMOUNT,amt.toString())
        paytm_params.put(Keys.WEBSITE,"DEFAULT")
        paytm_params.put(Keys.CALLBACK_URL,"https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=" + order_id)
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