package com.memu.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
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
import com.bumptech.glide.GenericTransitionOptions.with
import com.bumptech.glide.Glide.with
import com.iapps.gon.etc.callback.*
import com.iapps.libs.generics.GenericFragment
import com.iapps.libs.helpers.BaseHelper
import com.iapps.libs.helpers.BaseUIHelper
import com.iapps.libs.views.LoadingCompound
import com.memu.BuildConfig
import com.memu.R
import com.memu.etc.Constants
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager
import com.memu.modules.mapFeeds.MapFeed
import com.memu.modules.poolerVehicleList.Vehicle
import com.memu.modules.riderList.Rider
import com.memu.modules.userMainData.UserMainData
import com.memu.ui.activity.ActivityMain
import com.memu.ui.dialog.*
import com.memu.webservices.GetWalletBalanceViewModel
import com.memu.webservices.PosUserMainDataViewModel
import com.memu.webservices.PostFriendListViewModel
import com.memu.webservices.PostUpdateLocationViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.ld
import kotlinx.android.synthetic.main.profile_header.*
import kotlinx.android.synthetic.main.profile_wall.*
import kotlinx.coroutines.*
import java.util.*

open class BaseFragment : GenericFragment() {


    lateinit var userInfo: UserInfoManager
        private set
    lateinit var posUserMainDataViewModel: PosUserMainDataViewModel
    lateinit var postFriendListViewModel: PostFriendListViewModel
    lateinit var getWalletBalanceViewModel: GetWalletBalanceViewModel

    var permissionsThatNeedTobeCheck: List<String>? = null
        private set
    var permissionListener: PermissionListener? = null
        private set

    var v: View? = null
    companion object {
        var postUpdateLocationViewModel: PostUpdateLocationViewModel? = null

    }
    var userMainData : UserMainData? = null
    var obsNoInternet: Observer<Boolean> = Observer { isHaveInternet ->
        try {
            if (!isHaveInternet) {
                if (activity == null) return@Observer
                showNotifyDialog(
                    getString(R.string.iapps__no_internet), getString(R.string.no_connection),
                    getString(R.string.ok),"",object : NotifyListener {
                        override fun onButtonClicked(which: Int) {
                           // home().backToMainScreen()
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
        try {
            postUpdateLocationViewModel =
                ViewModelProviders.of(this).get(PostUpdateLocationViewModel::class.java!!)
        } catch (e :Exception){

        }
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

    open fun setUserMainData() {
        try {
            name.setText(userMainData?.name)
            ratings.setText(userMainData?.rating)
            posts.setText(userMainData?.posts.toString())
            likes.setText(userMainData?.likes.toString())
            followers_cnt.setText(userMainData?.followers.toString())
            following.setText(userMainData?.followings.toString())
            if(userMainData?.friends != 0) {
                friends_cnt.visibility = View.VISIBLE
                friends_cnt.text = userMainData?.friends.toString()
            } else {
                friends_cnt.visibility = View.GONE
            }
            if(userMainData?.followers != 0) {
                followers_cnt_.visibility = View.VISIBLE
                followers_cnt_.text = userMainData?.followers.toString()
            } else {
                followers_cnt_.visibility = View.GONE
            }
            if(userMainData?.messages != 0) {
                messages_cnt.visibility = View.VISIBLE
                messages_cnt.text = userMainData?.messages.toString()
            } else {
                messages_cnt.visibility = View.GONE
            }
            try {
                Helper.loadImage(activity!!,userMainData?.photo?.original_path!!,profile_pic,R.drawable.default_profile_icon)
            } catch (e : java.lang.Exception){
                e.printStackTrace()
            }
            rides_shared.text = userMainData?.rides_shared!!.toString()
            dist_shared.text = userMainData?.distance_shared!!.toString()
        } catch (e: Exception) {
            System.out.println("ExceptionlogException "+e.toString())
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
        if(!BaseHelper.isEmpty(tittle) || !BaseHelper.isEmpty(messsage)) {
            f.show(activity!!.supportFragmentManager, NotifyDialogFragment.TAG)
        }

    }
    open fun showAlertSentDialog(
        tittle: String?,
        coinsReceived: String?,
        description: String?,
        userName:String?,
        userImage: String?,
        isLiked: Boolean?){
        val f = AlertsNotifyDialogFragment()
        f.coinsReceived = coinsReceived!!
        f.username = userName!!
        f.title = tittle!!
        f.user_image = userImage!!
        f.isLiked = isLiked!!
        f.description = description!!

        f.isCancelable = false
        f.show(activity!!.supportFragmentManager, AlertsNotifyDialogFragment.TAG)

    }

    open fun showFindRideDialog(vehicle_list : List<Vehicle>,
        n: FindRideDialogListener
    ){
        val f = FindRideDialogFragment().apply {
            this.listener = n
            this.vehicle_list = vehicle_list
        }
        f.isCancelable = false
        f.show(activity!!.supportFragmentManager, FindRideDialogFragment.TAG)
    }


    open fun showAlertsDialog(mapfeed : List<MapFeed>, n: NotifyListener){
        val f = AlertsDialogFragment().apply {
            this.listener = n
            this.mapfeed = mapfeed
        }
        f.isCancelable = true
        f.show(activity!!.supportFragmentManager, AlertsDialogFragment.TAG)
    }

    open fun showCompletedDialog(n: NotifyListener){
        val f = TripCompletedDialogFragment().apply {
            this.listener = n
        }
        f.isCancelable = true
        f.show(activity!!.supportFragmentManager, TripCompletedDialogFragment.TAG)
    }

    open fun showMatchingRiders(
        rider_list: List<Rider>,
        n: RequestListener){
        val f = MatchingRidersFragment().apply {
            this.listener = n
        }
        f.rider_list = rider_list
        f.isCancelable = true
        f.show(activity!!.supportFragmentManager, MatchingRidersFragment.TAG)
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

    fun setUSerMAinDataAPIObserver() {
        posUserMainDataViewModel = ViewModelProviders.of(this).get(PosUserMainDataViewModel::class.java).apply {
            this@BaseFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {

                    } else {

                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer {
                    userMainData = posUserMainDataViewModel.obj
                    setUserMainData()
                })

            }
        }
    }

    fun setFriendListAPIObserver() {
        postFriendListViewModel = ViewModelProviders.of(this).get(PostFriendListViewModel::class.java).apply {
            this@BaseFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {

                    }
                })

            }
        }
    }
    fun setWalletBalanceObserver(walletBalanceListener: WalletBalanceListener) {
        getWalletBalanceViewModel = ViewModelProviders.of(this).get(
            GetWalletBalanceViewModel::class.java).apply {
            this@BaseFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        GetWalletBalanceViewModel.NEXT_STEP -> {
                            walletBalanceListener.walletBalanceResponse(obj!!)
                        }
                    }
                })
            }
        }
    }
    fun referFriend(){
        var referral_code =""
        if(UserInfoManager.getInstance(activity!!).getReferralCode() != null) {
            referral_code = UserInfoManager.getInstance(activity!!).getReferralCode()
        }
        try {

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, activity?.getString(R.string.app_name))
            var shareMessage = "Give a friend 2,000 points and get 2,000 points when they install app, " +
                    "use my referal code "+referral_code+"\nhttps://memu.world"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }

}