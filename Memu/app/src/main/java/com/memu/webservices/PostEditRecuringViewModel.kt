package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.GenericResponse
import com.memu.modules.completedRides.Completed
import org.json.JSONObject

class PostEditRecuringViewModel(application: Application) : BaseViewModel(application) {

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

    fun loadData(
        completed: Completed,
        fromAddress: JSONObject,
        toAddress: JSONObject
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
                        obj = gson.fromJson(response!!.content.toString(), GenericResponse::class.java)
                        if (obj!!.status.equals(Keys.STATUS_CODE)) {
                            trigger.postValue(GetVehicleTypeViewModel.NEXT_STEP)
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
        genericHttpAsyncTask.setUrl(APIs.postEditRecuring)
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.setPostParams(Keys.USER_ID, UserInfoManager.getInstance(apl).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.ID,completed.id)
        genericHttpAsyncTask.setPostParams(Keys.DATE,completed.date)
        genericHttpAsyncTask.setPostParams(Keys.TIME,completed.time)
        genericHttpAsyncTask.setPostParams(Keys.TYPE,completed.type)
        genericHttpAsyncTask.setPostParams(Keys.NO_OF_SEATS,completed.no_of_seats)
        genericHttpAsyncTask.setPostParams(Keys.VEHICLE_ID,completed.vehicle_id)
        genericHttpAsyncTask.setPostParams(Keys.STATUS,completed.status)
        genericHttpAsyncTask.setPostParams(Keys.IS_RECURRING_RIDE,completed.is_recurring_ride)
        genericHttpAsyncTask.setPostParams(Keys.DAYS,completed.days)
        genericHttpAsyncTask.setPostParams(Keys.FROM,fromAddress)
        genericHttpAsyncTask.setPostParams(Keys.To,toAddress)
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}