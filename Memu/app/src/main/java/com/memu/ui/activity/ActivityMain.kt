package com.memu.ui.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.franmontiel.localechanger.LocaleChanger
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.iapps.libs.helpers.BaseUIHelper
import com.mapbox.mapboxsdk.Mapbox
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager
import com.memu.ui.BaseFragment
import com.memu.ui.dialog.NotifyDialogFragment
import com.memu.webservices.PostacceptRejectViewModel
import io.paperdb.Paper
import java.util.ArrayList
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import com.google.gson.GsonBuilder
import com.memu.modules.notification.NotificationResponse
import android.os.Handler
import android.view.View
import androidx.core.app.NotificationCompat
import com.mapbox.mapboxsdk.MapStrictMode
import com.memu.R
import com.memu.ui.fragments.*
import com.memu.webservices.PostAcceptFriendRequestViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.followers_request_fragment.*
import kotlinx.android.synthetic.main.generic_dialog.*
import kotlinx.android.synthetic.main.home_fragment.ld


class ActivityMain : AppCompatActivity(){

    companion object {
        private var MAIN_FLOW_INDEX = 0
        private val MAIN_FLOW_TAG = "MainFlowFragment"

    }
    var submitPressed = true;

    private var mReceiver: BroadcastReceiver? = null
    private var mIntentFilter: IntentFilter? = null
    lateinit var postacceptRejectViewModel: PostacceptRejectViewModel
    lateinit var postAcceptFriendRequestViewModel: PostAcceptFriendRequestViewModel
    var isFriendsReques = true
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Paper.init(this);

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                showDialog(intent)
            }
        }
        Handler().postDelayed(
            Runnable // Using handler with postDelayed called runnable run method

            {
                memo_logo_icon.visibility = View.GONE
                triggerMainProcess()

                showDialog(getIntent())

            }, 2 * 2000
        ) // wait for 5 s
        BaseHelper.triggerNotifLog(this);
        setAcceptRejectAPIObserver()
        val mContext = this.getBaseContext();
        MapStrictMode.setStrictModeEnabled(true)
        Mapbox.getInstance(mContext, getString(R.string.map_box_access_token));

        mIntentFilter = IntentFilter("OPEN_NEW_ACTIVITY")
        BaseHelper.getHAshKey(this)
        setAcceptFriendRequestObserver()
    }

    fun showDialog(intent : Intent){
        val gson = GsonBuilder().create()

        if(intent.getExtras()?.getString("title") != null) {

            try {
                val obj = gson.fromJson(
                    intent.getExtras()?.getString("body"),
                    NotificationResponse::class.java)
                println("Notification_received showDialog " +obj)

                var btn_positive = "OK"
                var btn_negative = ""
                if(obj.isAccept) {
                    btn_positive = "Accept"
                    btn_negative = "Ignore"
                } else {
                    btn_positive = "Okay"
                    btn_negative = ""
                }

               // sendNotification(intent.getExtras()?.getString("title")!!,
                 //   intent.getExtras()?.getString("message")!!)
                var drawable =0
                if (obj.type.equals("find_ride") || obj.type.equals("offer_ride")) {
                    drawable = R.drawable.carpoolcar
                } else if(obj.type.equals("FR",ignoreCase = true)) {
                    drawable = R.drawable.myfriends
                }else if(obj.type.equals("FL",ignoreCase = true)) {
                    drawable = R.drawable.followers_noti
                }
                showNotifyDialog(
                intent.getExtras()?.getString("title"),
                intent.getExtras()?.getString("message"),
                btn_positive, btn_negative, object : NotifyListener {
                    override fun onButtonClicked(which: Int) {
                        if (obj.type.equals("find_ride") || obj.type.equals("offer_ride")) {
                            var trip_id = obj.trip_id
                            var trip_rider_id = obj.trip_rider_id
                            var status = ""
                            var type = obj.type
                            status = "accept"

                            if (which == NotifyDialogFragment.BUTTON_NEGATIVE) {
                                status = "reject"
                            }
                            if (which == NotifyDialogFragment.BUTTON_POSITIVE) {
                                status = "accept"
                            }

                            if (trip_id != null && status != null && trip_rider_id != null
                                && obj.type != null
                            ) {
                                postacceptRejectViewModel?.loadData(
                                    trip_id,
                                    status,
                                    trip_rider_id,
                                    obj.type
                                )
                            }
                        } else {
                            if(obj.type.equals("FR",ignoreCase = true)) {
                                isFriendsReques = true

                                if(which == NotifyDialogFragment.BUTTON_POSITIVE) {
                                    isFriendsReques = true
                                    postAcceptFriendRequestViewModel.loadData(
                                        "FR",
                                        obj.freind_id, "Accepted","to_me")
                                } else {
                                    isFriendsReques = true
                                    postAcceptFriendRequestViewModel.loadData(
                                        "FR",
                                        obj.freind_id, "Remove","to_me")
                                }

                            } else if(obj.type.equals("FL",ignoreCase = true)) {
                                isFriendsReques = false
                                if(which == NotifyDialogFragment.BUTTON_POSITIVE) {
                                    setFragment(FollowersRequestFragment())
                                }
                            }
                        }
                    }
                },drawable)
            } catch (e : Exception){
                println("Notification_received Exception " +e.toString())

            }

        }

    }

    open fun showNotifyDialog(
        tittle: String?,
        messsage: String?,
        button_positive:String?,
        button_negative: String?,
        n: NotifyListener,drawable : Int){
        try {
            val f = NotifyDialogFragment().apply {
                this.listener = n
            }
            f.notify_tittle = tittle
            f.notify_messsage = messsage
            f.button_positive = button_positive
            f.button_negative = button_negative
            f.image_drawable = drawable
            f.isCancelable = true
            f.show(supportFragmentManager, NotifyDialogFragment.TAG)
        } catch (e : Exception){
            System.out.println("Notification_received Exception " +e.toString())
        }
    }

    override fun attachBaseContext(newBase: Context) {
        var newBase = newBase
        newBase = LocaleChanger.configureBaseContext(newBase)
        super.attachBaseContext(newBase)
    }

    fun exitApp() {
        finish()
    }
    override fun onBackPressed() {
        val f = getSupportFragmentManager().beginTransaction()
        val list = getSupportFragmentManager().getFragments()
        var foundVisible = false
        for(i in  0..(list.size - 1)){
            if(list.get(i).isVisible){
                if(list.get(i) is BaseFragment) {
                    foundVisible = true
                    (list.get(i) as BaseFragment).onBackTriggered()
                }
            }
        }

        if(!foundVisible)
            proceedDoOnBackPressed()
    }


    fun proceedDoOnBackPressed(){
        Helper.hideSoftKeyboard(this@ActivityMain)

        val f = getSupportFragmentManager().beginTransaction()
        val list = getSupportFragmentManager().getFragments()

        for(frag in list){
            if(frag.tag!!.contentEquals(MAIN_FLOW_TAG + (MAIN_FLOW_INDEX - 1))){
                f.show(frag)
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1 || (currentFragment is MainFragment)) {
            this@ActivityMain.finish()
        } else {
            super.onBackPressed()
        }

        MAIN_FLOW_INDEX = MAIN_FLOW_INDEX - 1
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter);

    }


    private fun sendNotification(title:String,_description:String) {
        var notifyManager: NotificationManager? = null
        val NOTIFY_ID = 1002

        val name = "KotlinApplication"
        val id = "kotlin_app"
        val description = "kotlin_app_first_channel"

        val intent: Intent
        val pendingIntent: PendingIntent
        val builder: NotificationCompat.Builder

        if (notifyManager == null) {
            notifyManager = getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel = notifyManager.getNotificationChannel(id)
            if (mChannel == null) {
                mChannel = NotificationChannel(id, name, importance)
                mChannel.description = description
                mChannel.enableVibration(true)
                mChannel.lightColor = Color.GREEN
                mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                notifyManager.createNotificationChannel(mChannel)
            }
        }

        builder = NotificationCompat.Builder(this, id)

        intent = Intent(this, ActivityMain::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        builder.setContentTitle(title)  // required
            .setSmallIcon(R.drawable.memu_logo) // required
            .setContentText(_description)  // required
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setTicker(description)
            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))

        val dismissIntent = Intent(this, ActivityMain::class.java)
        dismissIntent.action = "DISMISS"
        dismissIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingDismissIntent = PendingIntent.getActivity(this, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        val dismissAction = NotificationCompat.Action(R.drawable.memu_logo,
            "OK", pendingDismissIntent)
        builder.addAction(dismissAction)

        val notification = builder.build()
        notifyManager.notify(NOTIFY_ID, notification)
    }
    fun triggerMainProcess(){
        if(!BaseHelper.isEmpty(UserInfoManager.getInstance(this).authToken))
            setFragment(HomeFragment())
        else
            setFragment(MainFragment())
    }


    fun setFragment(frag: Fragment) {
        try {
            val f = getSupportFragmentManager().beginTransaction()
            val list = getSupportFragmentManager().getFragments()
            for(frag in list){
                if(frag.isVisible){
                    f.hide(frag)
                }
            }

            MAIN_FLOW_INDEX = MAIN_FLOW_INDEX + 1
            f.add(R.id.layoutFragment, frag, MAIN_FLOW_TAG + MAIN_FLOW_INDEX).addToBackStack(
                MAIN_FLOW_TAG
            ).commitAllowingStateLoss()
            BaseUIHelper.hideKeyboard(this)
        } catch (e: Exception) {
            Helper.logException(this@ActivityMain, e)
        }

    }


    fun jumpToPreviousFlowThenGoTo(fullFragPackageNameThatStillExistInStack: String, targetFrag: Fragment){
        jumpToPreviousFragment(fullFragPackageNameThatStillExistInStack)
        setFragment(targetFrag)
    }

    fun jumpToPreviousFragment(fullFragPackageNameThatStillExistInStack: String) {
        try {
            var indexTag: String? = null

            val f = getSupportFragmentManager().beginTransaction()
            var list = getSupportFragmentManager().getFragments()
            for(i in  0..(list.size - 1)){

                if(list.get(i).javaClass.name.equals(fullFragPackageNameThatStillExistInStack, ignoreCase = false)){
                    indexTag = list.get(i).tag
                }

                if(list.get(i).isVisible){
                    f.hide(list.get(i))
                }
            }

            if(indexTag == null){
                onBackPressed()
            }else{

                val currentIndex = MAIN_FLOW_INDEX
                for(i in currentIndex downTo 0){
                    try {
                        if((MAIN_FLOW_TAG + i).equals(indexTag, ignoreCase = true)) break
                        getSupportFragmentManager().popBackStackImmediate()
                        MAIN_FLOW_INDEX = MAIN_FLOW_INDEX - 1
                    } catch (e: Exception) {
                        Helper.logException(this@ActivityMain, e)
                    }
                }

                list = getSupportFragmentManager().getFragments()
                for(i in  0..(list.size - 1)){
                    if(list.get(i).tag.equals(indexTag, ignoreCase = false)){
                        f.show(list.get(i))
                        break
                    }
                }

                BaseUIHelper.hideKeyboard(this)
            }

        } catch (e: Exception) {
            Helper.logException(this@ActivityMain, e)
        }
    }

    fun jumpToLastPreviousFragment(fullFragPackageNameThatStillExistInStack: String) {
        try {
            var indexTag: String? = null

            val f = getSupportFragmentManager().beginTransaction()
            var list = getSupportFragmentManager().getFragments()

            for(i in  0..(list.size - 1)){

                if(list.get(i).isVisible){
                    f.hide(list.get(i))
                }
            }

            for(i in  0..(list.size - 1)){

                if(list.get(i).javaClass.name.equals(fullFragPackageNameThatStillExistInStack, ignoreCase = false)){
                    indexTag = list.get(i).tag
                    break;
                }
            }

            if(indexTag == null){
                onBackPressed()
            }else{

                val currentIndex = MAIN_FLOW_INDEX
                for(i in currentIndex downTo 0){
                    try {
                        if((MAIN_FLOW_TAG + i).equals(indexTag, ignoreCase = true)) break
                        getSupportFragmentManager().popBackStackImmediate()
                        MAIN_FLOW_INDEX = MAIN_FLOW_INDEX - 1
                    } catch (e: Exception) {
                        Helper.logException(this@ActivityMain, e)
                    }
                }

                list = getSupportFragmentManager().getFragments()
                for(i in  0..(list.size - 1)){
                    if(list.get(i).tag.equals(indexTag, ignoreCase = false)){
                        f.show(list.get(i))
                        break
                    }
                }

                BaseUIHelper.hideKeyboard(this)
            }

        } catch (e: Exception) {
            Helper.logException(this@ActivityMain, e)
        }
    }

    fun jumpBackPreviousFragment2(howManyTimes: Int){
        try {
            for(i in 0..(howManyTimes-1)) {
                getSupportFragmentManager().popBackStackImmediate()
                MAIN_FLOW_INDEX = MAIN_FLOW_INDEX - 1
            }
        } catch (e: Exception) {
            Helper.logException(this@ActivityMain, e)
        }
    }

    fun setFragmentByReplace(frag: Fragment) {

        try {
            val f = getSupportFragmentManager().beginTransaction()
            MAIN_FLOW_INDEX = MAIN_FLOW_INDEX + 1
            f.replace(R.id.layoutFragment, frag, MAIN_FLOW_TAG + MAIN_FLOW_INDEX).addToBackStack(
                MAIN_FLOW_TAG
            ).commitAllowingStateLoss()
            BaseUIHelper.hideKeyboard(this)
        } catch (e: Exception) {
            Helper.logException(this@ActivityMain, e)
        }

    }

    fun getCurrentFragmentByTag(): Fragment?{
        val fragmentManager = this@ActivityMain.getSupportFragmentManager()
        val fragments = fragmentManager.getFragments()
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment != null && fragment!!.isVisible())
                    return fragment
            }
        }
        return null
    }

    fun clearFragment() {

        getSupportFragmentManager().popBackStack(MAIN_FLOW_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        for (i in MAIN_FLOW_INDEX downTo 0) {
            try {
                val fragment = getSupportFragmentManager().findFragmentByTag(MAIN_FLOW_TAG + i)
                if (fragment != null)
                    getSupportFragmentManager().beginTransaction().remove(fragment).commitNowAllowingStateLoss()
            } catch (e: Exception) {
                Helper.logException(this@ActivityMain, e)
            }

        }

        getSupportFragmentManager().popBackStack("MAIN_TAB", FragmentManager.POP_BACK_STACK_INCLUSIVE)

        MAIN_FLOW_INDEX = 0
    }
    fun setAcceptFriendRequestObserver() {
        postAcceptFriendRequestViewModel = ViewModelProviders.of(this).get(
            PostAcceptFriendRequestViewModel::class.java).apply {
            this@ActivityMain.let { thisFragReference ->
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
                        },0)
                })
                getTrigger().observe(thisFragReference, Observer { state ->

                    when (state) {
                        PostAcceptFriendRequestViewModel.NEXT_STEP -> {
                            if (isFriendsReques) {
                                setFragment(FollowersRequestFragment().apply {
                                    isFriendsRequest = true

                                })
                            } else {

                            }
                        }
                    }
                })
            }
        }
    }

    fun setAcceptRejectAPIObserver() {
        postacceptRejectViewModel = ViewModelProviders.of(this).get(PostacceptRejectViewModel::class.java).apply {
            this@ActivityMain.let { thisFragReference ->
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
                        },0)
                })
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        PostacceptRejectViewModel.NEXT_STEP -> {
                            showNotifyDialog(
                                "Request Status", postacceptRejectViewModel.obj?.message,
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) {

                                    }
                                }
                            ,0)
                        }

                    }
                })
            }
        }
    }

    fun removeFragments(fragList: ArrayList<Fragment>) {

        var list = getSupportFragmentManager().getFragments()

        for (frag in list) {
            try {
                for(fragx in fragList){

                    if(frag.tag.equals(fragx.tag)) {
                        val fragment = getSupportFragmentManager().findFragmentByTag(frag.tag)
                        if (fragment != null)
                            getSupportFragmentManager().beginTransaction().remove(fragment).commitNowAllowingStateLoss()
                    }
                }
            } catch (e: Exception) {
                Helper.logException(this@ActivityMain, e)
            }
        }

    }

    fun backToMainScreen(){
        clearFragment()
        triggerMainProcess()
    }

    fun resetAndGoToFragment(frag: Fragment) {
        clearFragment()
        setFragment(frag)
    }

    fun setOrShowExistingFragmentByTag(
        layoutId: Int,
        fragTag: String,
        backstackTag: String,
        newFrag: Fragment,
        listFragmentTagThatNeedToHide: ArrayList<String>
    ) {

        var foundExistingFragment = false

        val fragment = supportFragmentManager.findFragmentByTag(fragTag)
        val transaction = supportFragmentManager.beginTransaction()
        if (fragment != null) {
            for (i in 0 until supportFragmentManager.fragments.size) {

                try {
                    val f = supportFragmentManager.fragments[i]

                    for (tag in listFragmentTagThatNeedToHide) {
                        try {
                            if (f.tag.toString().toLowerCase().equals(tag.toLowerCase())) {
                                transaction.hide(f)
                            }
                        } catch (e: Exception) {
                            Helper.logException(this@ActivityMain, e)
                        }

                    }

                } catch (e: Exception) {
                    Helper.logException(this@ActivityMain, e)
                }

            }

            try {
                transaction.show(fragment).commitAllowingStateLoss()
            } catch (e: Exception) {
                try {
                    transaction.show(fragment).commitAllowingStateLoss()
                } catch (e1: Exception) {
                    Helper.logException(this@ActivityMain, e)
                }

            }

            foundExistingFragment = true

        }

        if (!foundExistingFragment) {
            setFragmentInFragment(layoutId, newFrag, fragTag, backstackTag)
        }

    }

    fun setFragmentInFragment(fragmentLayout: Int, frag: Fragment, tag: String, backstackTag: String) {
        try {
            supportFragmentManager.beginTransaction().add(fragmentLayout, frag, tag).addToBackStack(backstackTag)
                .commitAllowingStateLoss()
            BaseUIHelper.hideKeyboard(this)
        } catch (e: Exception) {
            try {
                supportFragmentManager.beginTransaction().add(fragmentLayout, frag, tag).addToBackStack(backstackTag)
                    .commitAllowingStateLoss()
                BaseUIHelper.hideKeyboard(this)
            } catch (e1: Exception) {
                Helper.logException(this@ActivityMain, e)
            }

        }

    }

    // Sometimes the last fragment in the list is null
    // Idk why
    val backstack: Fragment?
        get() {
            val list = getSupportFragmentManager().getFragments()
            return if (list != null && !list!!.isEmpty()) {
                if (list!!.get(list!!.size - 1) == null) list!!.get(list!!.size - 2) else list!!.get(list!!.size - 1)

            } else null

        }

    val currentFragment: Fragment
        get() = getSupportFragmentManager().findFragmentById(R.id.layoutFragment)!!


    val currentstack: Fragment?
        get() {
            val list = getSupportFragmentManager().getFragments()
            return if (list != null && !list!!.isEmpty()) {
                list!!.get(list!!.size - 1)
            } else null
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MainFragment().onActivityResult(requestCode, resultCode, data);

    }
    /******************************************
     * COMMON FUNCTIONS
     */


    /******************************************
     * LOGOUT FUNCTIONS
     */



}
