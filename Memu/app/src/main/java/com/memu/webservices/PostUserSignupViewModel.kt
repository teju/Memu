package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.etc.APIs
import com.memu.etc.Helper
import com.memu.etc.Keys
import com.memu.etc.SingleLiveEvent
import com.memu.modules.UserSignup.UserSignUp
import org.json.JSONArray
import org.json.JSONObject

class PostUserSignupViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: UserSignUp? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(
        apisignupform: JSONObject,
        vehicle: JSONObject,
        address: JSONArray,
        documents: JSONArray,
        otpform: JSONObject?
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
                        obj = gson.fromJson(response!!.content.toString(), UserSignUp::class.java)
                        if (obj!!.status.equals(Keys.STATUS_CODE)) {
                            trigger.postValue(GetVehicleTypeViewModel.NEXT_STEP)
                        }else{
                            errorMessage.value = createErrorMessageObject(response)
                        }
                    } catch (e: Exception) {
                        errorMessage.value = createErrorMessageObject(true,"Exception",e.toString())
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.postUserSignup)
        if(otpform != null) {
           // genericHttpAsyncTask.setPostParams(Keys.OtpForm,otpform)
        }
        if(vehicle != null) {
            genericHttpAsyncTask.setPostParams(Keys.Vehicle,vehicle!!)
        }
        if(documents != null && documents.length() != 0) {
            genericHttpAsyncTask.setPostParams(Keys.Documents,documents!!)
        }
        genericHttpAsyncTask.setPostParams(Keys.ApiSignupForm,apisignupform!!)
        genericHttpAsyncTask.setPostParams(Keys.Address,address!!)
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}