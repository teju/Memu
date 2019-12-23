package com.memu.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.security.keystore.KeyProperties
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.PermissionListener
import com.iapps.gon.etc.callback.RequestListener
import com.iapps.libs.generics.GenericFragment
import com.iapps.libs.helpers.BaseUIHelper
import com.iapps.libs.views.LoadingCompound
import com.memu.ActivityMain
import com.memu.BuildConfig
import com.memu.R
import com.memu.etc.Constants
import com.memu.etc.GPSTracker
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager
import com.memu.modules.TripGivers.Pooler
import com.memu.modules.riderList.Rider
import com.memu.ui.dialog.MatchingRidersFragment
import com.memu.ui.dialog.NotifyDialogFragment
import com.memu.webservices.GetVehicleTypeViewModel
import com.memu.webservices.PostUpdateLocationViewModel
import kotlinx.android.synthetic.main.home_fragment.*

import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

open class BaseFragment : GenericFragment() {


    lateinit var userInfo: UserInfoManager
        private set

    var permissionsThatNeedTobeCheck: List<String>? = null
        private set
    var permissionListener: PermissionListener? = null
        private set

    var v: View? = null
    companion object {
        lateinit var postUpdateLocationViewModel: PostUpdateLocationViewModel

    }

    var obsNoInternet: Observer<Boolean> = Observer { isHaveInternet ->
        try {
            if (!isHaveInternet) {
                if (activity == null) return@Observer
                showNotifyDialog(
                    getString(R.string.iapps__no_internet), getString(R.string.no_connection),
                    getString(R.string.ok),"",object : NotifyListener {
                        override fun onButtonClicked(which: Int) {
                            home().backToMainScreen()
                        }
                    })
            }
        } catch (e: Exception) {
            Helper.logException(activity, e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            this.userInfo = UserInfoManager.getInstance(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!BuildConfig.FLAVOR.equals("live", ignoreCase = true))
            Log.v("gon Screen", this.javaClass.toString())

        v?.let {
            setBackButtonToolbarStyleOne(v!!)
        }

        postUpdateLocationViewModel = ViewModelProviders.of(this).get(PostUpdateLocationViewModel::class.java!!)

        v?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                BaseUIHelper.hideKeyboard(activity)
            }})
    }

    fun setBackButtonToolbarStyleOne(v: View) {
       try {
            val llBack = v.findViewById<RelativeLayout>(R.id.llBack)

           llBack.setOnClickListener {

               home().onBackPressed()
            }
        } catch (e: Exception) {
           Helper.logException(activity, e)
        }
    }

    open fun onBackTriggered(){
        home().proceedDoOnBackPressed()
    }

    fun home(): ActivityMain {
        return activity as ActivityMain
    }

    fun checkPermissions(permissionsThatNeedTobeCheck: List<String>, permissionListener: PermissionListener) {

        this.permissionsThatNeedTobeCheck = permissionsThatNeedTobeCheck
        this.permissionListener = permissionListener
        val permissionsNeeded = ArrayList<String>()
        val permissionsList = ArrayList<String>()

        for (s in permissionsThatNeedTobeCheck) {
            if (s.equals(Manifest.permission.CAMERA, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.CAMERA))
                    permissionsNeeded.add("Camera")
            } else if (s.equals(Manifest.permission.READ_CONTACTS, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
                    permissionsNeeded.add("Read Contacts")
            } else if (s.equals(Manifest.permission.WRITE_CONTACTS, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.WRITE_CONTACTS))
                    permissionsNeeded.add("Write Contacts")
            } else if (s.equals(Manifest.permission.READ_EXTERNAL_STORAGE, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
                    permissionsNeeded.add("Read External Storage")
            } else if (s.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    permissionsNeeded.add("Write External Storage")
            } else if (s.equals(Manifest.permission.RECEIVE_SMS, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.RECEIVE_SMS))
                    permissionsNeeded.add("Read SMS")
            } else if (s.equals(Manifest.permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                    permissionsNeeded.add("ACCESS FINE LOCATION")
            } else if (s.equals(Manifest.permission.ACCESS_COARSE_LOCATION, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
                    permissionsNeeded.add("ACCESS COARSE LOCATION")
            } else if (s.equals(Manifest.permission.READ_SMS, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.READ_SMS))
                    permissionsNeeded.add("Read SMS")
            } else if (s.equals(Manifest.permission.CALL_PHONE, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.CALL_PHONE))
                    permissionsNeeded.add("Call Phone")
            } else if (s.equals(Manifest.permission.RECORD_AUDIO, ignoreCase = true)) {
                if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
                    permissionsNeeded.add("Record Audio")
            }
        }

        if (permissionsList.size > 0) {
            if (permissionsNeeded.size > 0) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    permissionsList.toTypedArray(),
                    Constants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
                )
                return
            }
            ActivityCompat.requestPermissions(
                activity!!, permissionsList.toTypedArray(),
                Constants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
            )
            return
        } else {
            permissionListener.onPermissionAlreadyGranted()
        }
    }

    fun checkPermissionsNoPopup(permissionsThatNeedTobeCheck: List<String>, permissionListener: PermissionListener) {

        this.permissionsThatNeedTobeCheck = permissionsThatNeedTobeCheck
        this.permissionListener = permissionListener
        val permissionsNeeded = ArrayList<String>()
        val permissionsList = ArrayList<String>()

        for (s in permissionsThatNeedTobeCheck) {
            if (s.equals(Manifest.permission.CAMERA, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.CAMERA))
                    permissionsNeeded.add("Camera")
            } else if (s.equals(Manifest.permission.READ_CONTACTS, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.READ_CONTACTS))
                    permissionsNeeded.add("Read Contacts")
            } else if (s.equals(Manifest.permission.WRITE_CONTACTS, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.WRITE_CONTACTS))
                    permissionsNeeded.add("Write Contacts")
            } else if (s.equals(Manifest.permission.READ_EXTERNAL_STORAGE, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
                    permissionsNeeded.add("Read External Storage")
            } else if (s.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    permissionsNeeded.add("Write External Storage")
            } else if (s.equals(Manifest.permission.RECEIVE_SMS, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.RECEIVE_SMS))
                    permissionsNeeded.add("Read SMS")
            } else if (s.equals(Manifest.permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                    permissionsNeeded.add("ACCESS FINE LOCATION")
            } else if (s.equals(Manifest.permission.ACCESS_COARSE_LOCATION, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
                    permissionsNeeded.add("ACCESS COARSE LOCATION")
            } else if (s.equals(Manifest.permission.READ_SMS, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.READ_SMS))
                    permissionsNeeded.add("Read SMS")
            } else if (s.equals(Manifest.permission.CALL_PHONE, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.CALL_PHONE))
                    permissionsNeeded.add("Call Phone")
            } else if (s.equals(Manifest.permission.RECORD_AUDIO, ignoreCase = true)) {
                if (!checkPermission(Manifest.permission.RECORD_AUDIO))
                    permissionsNeeded.add("Record Audio")
            }
        }

        if (permissionsThatNeedTobeCheck.size > 0) {
            if (permissionsNeeded.size > 0) {
                permissionListener.onUserNotGrantedThePermission()
                return
            }
            ActivityCompat.requestPermissions(
                activity!!, permissionsList.toTypedArray(),
                Constants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
            )
            return
        } else {
            permissionListener.onPermissionAlreadyGranted()
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
            false
        } else true
    }

    private fun addPermission(permissionsList: MutableList<String>, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission)
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission))
                return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        try {
            if (requestCode == Constants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {

                var isAllGranted = false
                val index = 0
                for (permission in permissionsThatNeedTobeCheck!!) {
                    if (permission.equals(Manifest.permission.CAMERA, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.READ_CONTACTS, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.WRITE_CONTACTS, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.RECEIVE_SMS, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.READ_SMS, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    } else if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION, ignoreCase = true)) {
                        if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        } else {
                            isAllGranted = true
                        }
                    }
                    //                    index = index + 1;
                }

                //                index = index - 1;
                if (isAllGranted) {
                    permissionListener!!.onCheckPermission(permissions[index], true)
                } else {
                    permissionListener!!.onCheckPermission(permissions[index], false)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    open fun showNotifyDialog(
        tittle: String?,
        messsage: String?,
        button_positive:String?,
        button_negative: String?,
        n: NotifyListener){
        val f = NotifyDialogFragment().apply {
            this.listener = n
        }
        f.notify_tittle = tittle
        f.notify_messsage = messsage
        f.button_positive = button_positive
        f.button_negative = button_negative
        f.isCancelable = false
        f.show(activity!!.supportFragmentManager, NotifyDialogFragment.TAG)
    }

    open fun showMatchingRiders(
        rider_list: List<Rider>,
        n: RequestListener){
        val f = MatchingRidersFragment().apply {
            this.listener = n
        }
        f.rider_list = rider_list
        f.isCancelable = true
        f.show(activity!!.supportFragmentManager, NotifyDialogFragment.TAG)
    }

    open fun showHTMLNotifyDialog(
        tittle: String?,
        messsage: String?,
        button_positive:String?,
        button_negative: String?,
        n: NotifyListener){
        val f = NotifyDialogFragment().apply {
            this.listener = n
        }
        f.useHtml = true
        f.notify_tittle = tittle
        f.notify_messsage = messsage
        f.button_positive = button_positive
        f.button_negative = button_negative
        f.isCancelable = false
        f.show(activity!!.supportFragmentManager, NotifyDialogFragment.TAG)
    }

    fun showLoadingLogicError(ld: LoadingCompound, errorLogicCode : String){
        ld.showError(getString(com.iapps.common_library.R.string.iapps__network_error),
            String.format("%s (%s)", getString(com.iapps.common_library.R.string.iapps__unknown_response), errorLogicCode))
    }


    fun setBoldSpannable(myText: String, start: Int, end: Int): SpannableString {
        try {
            val spannableContent = SpannableString(myText)
            // val typeface = Typeface.createFromAsset(context!!.assets, "font/poppins_bold")
            spannableContent.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            return spannableContent
        } catch (e : java.lang.Exception) {
            val spannableContent = SpannableString(myText)
            // val typeface = Typeface.createFromAsset(context!!.assets, "font/poppins_bold")
            spannableContent.setSpan(StyleSpan(Typeface.BOLD), start, end - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            return spannableContent
        }
    }

    fun AppCompatEditText.afterTextChangedDebounce(delayMillis: Long, input: (String) -> Unit) {
        var lastInput = ""
        var debounceJob: Job? = null
        val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null) {
                    val newtInput = editable.toString()
                    debounceJob?.cancel()
                    if (lastInput != newtInput) {
                        lastInput = newtInput
                        debounceJob = uiScope.launch {
                            delay(delayMillis)
                            if (lastInput == newtInput) {
                                input(newtInput)
                            }
                        }
                    }
                }
            }

            override fun beforeTextChanged(cs: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {}
        })}

    private fun getDataSource(): DataSource {
        return DataSource.Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .setName("steps")
            .build()
    }


}