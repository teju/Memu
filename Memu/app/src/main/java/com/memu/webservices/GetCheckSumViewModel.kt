package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.profileWall.ProfileWall

class GetCheckSumViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: ProfileWall? = null


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
                        obj = gson.fromJson(response!!.content.toString(), ProfileWall::class.java)
                        if (obj!!.status.equals(Keys.STATUS_CODE)) {
                            trigger.postValue(NEXT_STEP)
                        }else{
                            errorMessage.value = createErrorMessageObject(response)

                        }
                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl("https://www.blueappsoftware.com/payment/payment_paytm/generateChecksum.php")
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.setPostParams(Keys.MID,"1000000")
        genericHttpAsyncTask.setPostParams(Keys.ORDER_ID,order_id)
        genericHttpAsyncTask.setPostParams(Keys.CUST_ID,cust_id)
        genericHttpAsyncTask.setPostParams(Keys.CHANNEL_ID,"WAP")
        genericHttpAsyncTask.setPostParams(Keys.TXN_AMOUNT,"0")
        genericHttpAsyncTask.setPostParams(Keys.WEBSITE,"WEBSTAGING")
        genericHttpAsyncTask.setPostParams(Keys.CALLBACK_URL,callback_url)
        genericHttpAsyncTask.setPostParams(Keys.INDUSTRY_TYPE_ID,"Retail")
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}