package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.etc.APIs
import com.memu.etc.Helper
import com.memu.etc.Keys
import com.memu.etc.SingleLiveEvent
import com.memu.modules.GenericResponse
import com.memu.modules.UserSignup.UserSignUp
import com.memu.modules.googleMaps.GoogleMAps
import org.json.JSONArray
import org.json.JSONObject

class PostGetRoutesViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: GoogleMAps? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(url: String) {
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
                        obj = gson.fromJson(response!!.content.toString(), GoogleMAps::class.java)
                        trigger.postValue(GetVehicleTypeViewModel.NEXT_STEP)

                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.GET
        genericHttpAsyncTask.setUrl(url)
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}