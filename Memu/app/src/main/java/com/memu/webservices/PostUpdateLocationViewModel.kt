package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.DocUpload.DocUpload
import com.memu.modules.VehicleType.VehicleType
import org.json.JSONArray
import org.json.JSONObject
import java.security.Key

class PostUpdateLocationViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: DocUpload? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(location_detail : JSONObject) {
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

                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.context = apl.applicationContext
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.setUrl(APIs.currentVehicleLocation)

        genericHttpAsyncTask.setPostParams(Keys.USER_ID,UserInfoManager.getInstance(apl).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.LOCATION_DETAILS,location_detail)

        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }


}