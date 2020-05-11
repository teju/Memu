package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.GenericResponse
import com.memu.modules.UserSignup.UserSignUp
import com.memu.modules.googleMaps.GoogleMAps
import com.memu.modules.userMainData.UserMainData
import com.memu.modules.userSearch.UserSearch
import org.json.JSONArray
import org.json.JSONObject

class PosUSerSearchViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: UserSearch? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(search_word : String) {
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
                        obj = gson.fromJson(response!!.content.toString(), UserSearch::class.java)
                        trigger.postValue(GetVehicleTypeViewModel.NEXT_STEP)

                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.GET
        genericHttpAsyncTask.setUrl(APIs.searchUser)
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.context = apl.applicationContext
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.setPostParams(Keys.USER_ID, UserInfoManager.getInstance(apl).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.SEARCH_WORD, search_word)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}