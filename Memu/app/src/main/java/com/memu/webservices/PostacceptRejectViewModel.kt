package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.helpers.BaseHelper
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.FindTrip.FindTRip
import com.memu.modules.GenericResponse
import com.memu.modules.VehicleType.VehicleType
import com.memu.modules.poolerVehicles.PoolerVehicles
import org.json.JSONObject

class PostacceptRejectViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: GenericResponse? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(trip_id : String,
                 status : String,
                 trip_rider_id : String,
                 type : String) {
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
                        obj = gson.fromJson(response!!.content.toString(), GenericResponse::class.java)
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
        genericHttpAsyncTask.setUrl(APIs.postAcceptRejectReq)
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.setPostParams(Keys.USER_ID,UserInfoManager.getInstance(apl).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.TYPE,type)
        if(!BaseHelper.isEmpty(trip_id)) {
            genericHttpAsyncTask.setPostParams(Keys.TRIP_ID, trip_id)
        }
        if(!BaseHelper.isEmpty(trip_rider_id)) {
            genericHttpAsyncTask.setPostParams(Keys.TRIP_RIDER_ID_, trip_rider_id)
        }
        genericHttpAsyncTask.setPostParams(Keys.STATUS,status)

        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}