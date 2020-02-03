package com.memu.webservices

import android.app.Application
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import com.memu.etc.*
import com.memu.modules.DocUpload.DocUpload
import com.memu.modules.VehicleType.VehicleType
import org.json.JSONArray

class PostUploadDocViewModel(application: Application) : BaseViewModel(application) {

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

    fun loadData(doc_type : Int,path : String) {

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
                        obj = gson.fromJson(response!!.content.toString(), DocUpload::class.java)
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
        System.out.println("onActivityResult12 loadData "+doc_type)

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.context = apl.applicationContext
        if(doc_type == VEHICLE_PHOTO) {
            genericHttpAsyncTask.setUrl(APIs.postUploadVehiclePhotp)
            genericHttpAsyncTask.setFileParams(Keys.VEHICLE,path,"multipart/form-data; boundar")
        } else if(doc_type == VEHICLE_REG_CERT_PHOTO) {
            genericHttpAsyncTask.setUrl(APIs.postUploadVehicleCertPhotp)
            genericHttpAsyncTask.setFileParams(Keys.REGISTRATION_CERTIFICATE,path,"multipart/form-data; boundar")
        } else if(doc_type == VEHICLE_DL_PHOTO) {
            genericHttpAsyncTask.setUrl(APIs.postUploadDlPhoto)
            genericHttpAsyncTask.setFileParams(Keys.DRIVING_LICENCE,path,"multipart/form-data; boundar")
        }else if(doc_type == PROFILE_PHOTO) {

            genericHttpAsyncTask.setUrl(APIs.postUploadProfilePhoto)
            Helper.applyHeader(apl,genericHttpAsyncTask)
            genericHttpAsyncTask.setFileParams(Keys.PROFILE,path,"multipart/form-data; boundar")
            genericHttpAsyncTask.settxtFileParams(Keys.USER_ID,UserInfoManager.getInstance(apl).getAccountId())

        }

        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
        var VEHICLE_PHOTO = 2
        var VEHICLE_REG_CERT_PHOTO = 3
        var VEHICLE_DL_PHOTO =  4
        var PROFILE_PHOTO =  5
    }

}