package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.helpers.BaseKeys.LATITUDE
import com.iapps.libs.helpers.BaseKeys.LONGITUDE
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.friendList.FriendList

class PostFriendListViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: FriendList? = null
    interface FriendsSearchResListener {
        fun onResult(result:FriendList?, searchByLoc: Int)
    }


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(type: String, search_word : String, searchByLoc : Int, friendsSearchAPIListner : FriendsSearchResListener) {
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
                        obj = gson.fromJson(response!!.content.toString(), FriendList::class.java)
                        if (obj!!.status.equals(Keys.STATUS_CODE)) {
                            trigger.postValue(NEXT_STEP)
                            friendsSearchAPIListner?.onResult(obj,searchByLoc)

                        }else{
                            errorMessage.value = createErrorMessageObject(response)

                        }
                    } catch (e: Exception) {
                        System.out.println("postPendingFriendListViewModel Exception "+e.toString())

                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.friendList)
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.setPostParams(Keys.LIMIT,"1000000")
        genericHttpAsyncTask.setPostParams(Keys.OFFSET,"0")
        genericHttpAsyncTask.setPostParams(Keys.TYPE,type)
        genericHttpAsyncTask.setPostParams(Keys.SEARCH_WORD,search_word)
        genericHttpAsyncTask.setPostParams(Keys.SEARCHBYLOC,searchByLoc.toString())
        genericHttpAsyncTask.setPostParams(Keys.USER_ID, UserInfoManager.getInstance(apl).getAccountId())
        if(searchByLoc == 1) {
            var gpsTracker: GPSTracker? = null
            gpsTracker = GPSTracker(apl)
            if(gpsTracker?.canGetLocation()!!) {
                genericHttpAsyncTask.setPostParams(LATITUDE, gpsTracker.latitude.toString())
                genericHttpAsyncTask.setPostParams(LONGITUDE, gpsTracker.longitude.toString())

            }
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