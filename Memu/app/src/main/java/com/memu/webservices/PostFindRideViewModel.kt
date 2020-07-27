package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.helpers.BaseHelper
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.FindTrip.FindTRip
import org.json.JSONObject

class PostFindRideViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: FindTRip? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(
        date: String,
        time: String,
        no_of_seats: String,
        is_recuring_ride: String,
        days: String,
        from: JSONObject,
        to: JSONObject,
        no_of_kms: String,
        type: String,
        vehicle_id: String,
        rs_per_kms: String,
        via: JSONObject
    ) {
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
                        obj = gson.fromJson(response!!.content.toString(), FindTRip::class.java)
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
        genericHttpAsyncTask.setUrl(APIs.postOffersRides)
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.setPostParams(Keys.USER_ID,UserInfoManager.getInstance(apl).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.DATE,date)
        genericHttpAsyncTask.setPostParams(Keys.TIME,time)
        genericHttpAsyncTask.setPostParams(Keys.NO_OF_SEATS,no_of_seats)
        genericHttpAsyncTask.setPostParams(Keys.IS_RECURING_RIDE,is_recuring_ride)
        genericHttpAsyncTask.setPostParams(Keys.DAYS,days)
        genericHttpAsyncTask.setPostParams(Keys.TYPE,type)
        genericHttpAsyncTask.setPostParams(Keys.NO_OF_KMS,no_of_kms)
        genericHttpAsyncTask.setPostParams(Keys.To,to)
        genericHttpAsyncTask.setPostParams(Keys.FROM,from)
        System.out.println("VEHICLE_ID1234 "+vehicle_id)
        if(!BaseHelper.isEmpty(vehicle_id)) {
            genericHttpAsyncTask.setPostParams(Keys.VEHICLE_ID,vehicle_id)
        }
        if(!BaseHelper.isEmpty(rs_per_kms)) {
            genericHttpAsyncTask.setPostParams(Keys.RS_PER_KMS,rs_per_kms)

        }
        if(via.length() != 0) {
            genericHttpAsyncTask.setPostParams(Keys.TRIP_VIA,via)
        }
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}