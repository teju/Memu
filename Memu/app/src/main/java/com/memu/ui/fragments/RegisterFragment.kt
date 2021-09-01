package com.memu.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import com.memu.R
import com.memu.ui.BaseFragment
import kotlinx.android.synthetic.main.register_fragment.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import kotlinx.android.synthetic.main.onboarding_four.*
import kotlinx.android.synthetic.main.onboarding_one.*
import kotlinx.android.synthetic.main.onboarding_three.*
import kotlinx.android.synthetic.main.onboarding_two.*
import org.json.JSONArray
import org.json.JSONObject
import android.util.DisplayMetrics
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import com.google.android.gms.auth.api.credentials.Credential
import com.iapps.deera.etc.OTPExpiryListener
import com.iapps.gon.etc.callback.PermissionListener
import com.memu.etc.*
import com.memu.ui.activity.SearchActivity
import com.memu.webservices.*
import kotlinx.android.synthetic.main.cab_radio_button.*
import kotlinx.android.synthetic.main.onboarding_start.*
import kotlinx.android.synthetic.main.onboarding_three.er_mtv3
import kotlinx.android.synthetic.main.onboarding_three.get_otp
import kotlinx.android.synthetic.main.onboarding_three.mobileNo
import kotlinx.android.synthetic.main.onboarding_three.otp_number
import kotlinx.android.synthetic.main.onboarding_two_temp.*
import kotlinx.android.synthetic.main.radio_button.*
import kotlinx.android.synthetic.main.register_fragment.ld
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class RegisterFragment : BaseFragment() , View.OnClickListener,View.OnTouchListener {

    private var cameraOutputUri: Uri? = null
    var fbObj: JSONObject? = null
    val ANIMATION_SPEED = 2000
    val counter = CountDownTimerHelper()

    private val REQUEST_CODE_AUTOCOMPLETE: Int = 1002
    private val REQUEST_CODE_AUTOCOMPLETE_OFFICE: Int = 1003
    private val REQUEST_CODE_UPLOAD_VEHICLE: Int = 1004
    private val REQUEST_CODE_UPLOAD_REG: Int = 1005
    private val REQUEST_CODE_UPLOAD_DL: Int = 1006
    lateinit var getVehicleTypeViewModel: GetVehicleTypeViewModel
    lateinit var postUserSignupViewModel: PostUserSignupViewModel
    lateinit var postRequestOtpViewModel: PostRequestOtpViewModel
    lateinit var postVerifyOtpViewModel: PostVerifyOtpViewModel
    lateinit var postUploadDocViewModel: PostUploadDocViewModel
    val jsonArray = JSONArray()
    val docjsonArray = JSONArray()
    val state = State()
    var registration_certificateID = ""
    var registration_certificateID_name = ""
    var driving_licenceID = ""
    var driving_licenceID_name = ""
    var vehicleID = ""
    var vehicleID_name = ""
    val PICK_PHOTO_DOC = 10001
    var isFirst = true
    var isFirst_P_Y = false
    var isCabFisrst = true
    var gpsTracker : GPSTracker? = null
    var isOtpVerified = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.register_fragment, container, false)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        no_vehicle_btn.setOnClickListener(this)
        cab_vehicle_btn.setOnClickListener(this)
        private_vehicle_btn.setOnClickListener(this)
        btnNExt.setOnClickListener(this)
        get_otp.setOnClickListener(this)
        verify_otp.setOnClickListener(this)
        verify_otp.setOnClickListener(this)
        vehicle_pic.setOnClickListener(this)
        home_address.setOnClickListener(this)
        uploadreg_no.setOnClickListener(this)
        female_button.setOnClickListener(this)
        male_button.setOnClickListener(this)
        upload_dl.setOnClickListener(this)
        cab_upload_vehicle_pic.setOnClickListener(this)
        upload_cab_reg_no.setOnClickListener(this)
        cab_upload_dl.setOnClickListener(this)
        home_address.setOnClickListener(this)
        officeAddress.setOnClickListener(this)
        mobileNo.setOnClickListener(this)


        setVehicleTypeAPIObserver()
        setUSerSignUpAPIObserver()
        setRequestOtpAPIObserver()
        setVerifyOtpAPIObserver()
        setUploadDocObserver()

        initFb()
        requestHint()
        SmsReceiver.bindListener { messageText ->
            var messageText = messageText
            val msgArr =
                messageText.split("\\s".toRegex()).toTypedArray()
            messageText = msgArr[1]
            //Toast.makeText(Login.this, "Message: " + messageText, Toast.LENGTH_LONG).show();
            otp_number.setText(messageText)
        }
        getVehicleTypeViewModel.loadData()
        //onScrolledUp()

        try {
            onbording_1.getViewTreeObserver()
                .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

                    override fun onGlobalLayout() {
                        if(onbording_1 != null) {
                            // TODO Auto-generated method stub
                            val h = onbording_1.getHeight() + Helper.dpToPx(activity!!, 80)
                            val params = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            params.width = Helper.dpToPx(activity!!, 570)
                            params.setMargins(-200, h, 0, 0)
                            flyover.layoutParams = params
                        }
                    }
                })
        } catch (e : Exception){

        }
      

        car_hat.setOnCheckedChangeListener { buttonView, isChecked ->
            car_hat.isChecked = isChecked
            car_suv.isChecked = false
            motor_bike.isChecked = false
            tt_mini_bus.isChecked = false
            State.vehicle_model_type = "Sedan"

            car_sedan.isChecked = false
            State.role_id = "5"
        }
        cab_car_hat.setOnCheckedChangeListener { buttonView, isChecked ->
            cab_car_hat.isChecked = isChecked
            cab_car_suv.isChecked = false
            cab_motor_bike.isChecked = false
            cab_tt_mini_bus.isChecked = false
                    State.vehicle_model_type = "Sedan"

            cab_car_sedan.isChecked = false
                    State.role_id = "5"
        }

        car_sedan.setOnCheckedChangeListener { buttonView, isChecked ->
            car_hat.isChecked = false
            car_suv.isChecked = false
            motor_bike.isChecked = false
            tt_mini_bus.isChecked = false
            State.vehicle_model_type = "Sedan"

            car_sedan.isChecked = isChecked
            State.role_id = "5"
        }
        cab_car_sedan.setOnCheckedChangeListener { buttonView, isChecked ->
            cab_car_hat.isChecked = false
            cab_car_suv.isChecked = false
            cab_motor_bike.isChecked = false
            cab_tt_mini_bus.isChecked = false
            State.vehicle_model_type = "Sedan"

            cab_car_sedan.isChecked = isChecked
            State.role_id = "5"
        }
        car_suv.setOnCheckedChangeListener { buttonView, isChecked ->
            car_hat.isChecked = false
            car_suv.isChecked = isChecked
            motor_bike.isChecked = false
            car_sedan.isChecked = false
            tt_mini_bus.isChecked = false
            State.role_id = "5"
            State.vehicle_model_type = "SUV"

        }
        cab_car_suv.setOnCheckedChangeListener { buttonView, isChecked ->
            cab_car_hat.isChecked = false
            cab_car_suv.isChecked = isChecked
            cab_motor_bike.isChecked = false
            cab_car_sedan.isChecked = false
            cab_tt_mini_bus.isChecked = false
            State.role_id = "5"
            State.vehicle_model_type = "SUV"

        }
        tt_mini_bus.setOnCheckedChangeListener { buttonView, isChecked ->
            car_hat.isChecked = false
            tt_mini_bus.isChecked = isChecked
            car_suv.isChecked = false
            motor_bike.isChecked = false
            car_sedan.isChecked = false
            State.role_id = "5"
            State.vehicle_model_type = "minibus"

        }
        cab_tt_mini_bus.setOnCheckedChangeListener { buttonView, isChecked ->
            cab_car_hat.isChecked = false
            cab_tt_mini_bus.isChecked = isChecked
            cab_car_suv.isChecked = false
            cab_motor_bike.isChecked = false
            cab_car_sedan.isChecked = false
            State.role_id = "5"
            State.vehicle_model_type = "minibus"

        }
        motor_bike.setOnCheckedChangeListener { buttonView, isChecked ->
            car_hat.isChecked = false
            motor_bike.isChecked = isChecked
            car_suv.isChecked = false
            car_sedan.isChecked = false
            tt_mini_bus.isChecked = false
            State.role_id = "5"
            State.vehicle_model_type = "MotorBike"
        }
        cab_motor_bike.setOnCheckedChangeListener { buttonView, isChecked ->
            cab_car_hat.isChecked = false
            cab_motor_bike.isChecked = isChecked
            cab_car_suv.isChecked = false
            cab_car_sedan.isChecked = false
            cab_tt_mini_bus.isChecked = false
            State.role_id = "5"
            State.vehicle_model_type = "MotorBike"
        }

        dl.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                if(destination != null) {
                    destination!!.removeView(temp_image_view)
                }
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                if(State.type == State.White_board || State.type == State.NoVehicles) {
                    startAnimation(white_car,R.drawable.white_car,1400,onbording_4 ,400)
                } else {
                    startAnimation(yellow_car,R.drawable.yellow_car,1400,onbording_4,400)
                }
                true
            } else {
                false
            }
        }
        cab_dl.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                if(destination != null) {
                    destination!!.removeView(temp_image_view)
                }
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                if(State.type == State.White_board || State.type == State.NoVehicles) {
                    startAnimation(white_car,R.drawable.white_car,1500,onbording_4 ,400)
                } else {
                    startAnimation(yellow_car,R.drawable.yellow_car,1500,onbording_4 ,400)
                }
                true
            } else {
                false
            }
        }
        otp_number.setOnEditorActionListener { v, actionId, event ->

            if(actionId == EditorInfo.IME_ACTION_NEXT){
                System.out.println("startAnimation destination not null "+
                        onbording_1.height.toInt())

                if(destination != null) {
                    destination!!.removeView(temp_image_view)
                }

                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt() + 700).setDuration(2000).start();
                destination = onbording_4
                if(State.type == State.White_board ) {
                    startAnimation(white_car, R.drawable.white_car, 2500, onbording_4, Helper.toDp(activity!!, 600f))
                } else if(State.type == State.YELLOW_BOARD ) {
                    startAnimation(yellow_car,  R.drawable.yellow_car, 2500, onbording_4, Helper.toDp(activity!!, 600f))
                } else {
                    startAnimation(white_car,  R.drawable.white_car, 1800, onbording_4, Helper.toDp(activity!!, 600f))
                }

                true
            } else {
                false
            }
        }
        edtEmail.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                if(destination != null) {
                    destination!!.removeView(temp_image_view)
                }

                ObjectAnimator.ofInt(sv, "scrollY",  onbording_5.getY().toInt()).setDuration(2000).start();
                destination = onbording_5
                //startAnimation(view,drawable,top_margin,onbording_5 ,400)
                if(State.type == State.White_board ) {
                    startAnimation(white_car, R.drawable.white_car, 2600, onbording_4, 400)
                } else if(State.type == State.YELLOW_BOARD ) {
                    startAnimation(yellow_car,  R.drawable.yellow_car, 2600, onbording_4, 400)
                } else {
                    startAnimation(white_car,  R.drawable.white_car, 1800, onbording_4,400)
                }
                true
            } else {
                false
            }
        }
        gpsTracker = GPSTracker(activity)
        if(gpsTracker?.canGetLocation()!!) {
            try {
                val getAddress = getAddress(gpsTracker?.latitude!!, gpsTracker?.longitude!!)
                home_address.setText(getAddress?.get(0)?.getAddressLine(0))
                State.lattitude = gpsTracker!!.latitude
                State.longitude = gpsTracker!!.longitude
            } catch (e : Exception){
                System.out.println("Exception12223 "+e.toString())
            }
        }


    }

    fun initFb(){
        if(fbObj != null ){
            first_name.setText(fbObj!!.getString("first_name"))
            last_name.setText(fbObj!!.getString("last_name"))
            edtEmail.setText(fbObj!!.getString("email"))
        }
    }

    fun initSearch(code : Int) {
       /* val intent = PlaceAutocomplete.IntentBuilder()
            .accessToken(
                if (Mapbox.getAccessToken() != null) Mapbox.getAccessToken()!! else getString(
                    R.string.map_box_access_token
                )
            )
            .placeOptions(
                PlaceOptions.builder()
                    .backgroundColor(Color.parseColor("#EEEEEE"))
                    .limit(10)
                    .build(PlaceOptions.MODE_CARDS)
            )
            .build(activity)*/
        startActivityForResult(Intent(activity, SearchActivity::class.java),code);
    }

    fun pickImage() {
        cameraOutputUri = activity!!.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        val intent: Intent = BaseHelper.getPickIntent(cameraOutputUri,activity!!)
        startActivityForResult(intent, PICK_PHOTO_DOC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            val lat = data?.getDoubleExtra("Lat",0.0)
            val lng = data?.getDoubleExtra("Lng",0.0)
            State.lattitude = lat!!
            State.longitude =lng!!
            home_address.setText(data?.getStringExtra("Address"))
        } else if(requestCode == REQUEST_CODE_AUTOCOMPLETE_OFFICE) {
            val lat = data?.getDoubleExtra("Lat",0.0)
            val lng = data?.getDoubleExtra("Lng",0.0)
            State.officelattitude = lat!!
            State.officelongitude =lng!!
            officeAddress.setText(data?.getStringExtra("Address"))
        } else if(requestCode == RESOLVE_HINT) {
            if (requestCode === RESOLVE_HINT) {
                if (resultCode === RESULT_OK) {
                    val cred: Credential = data?.getParcelableExtra(Credential.EXTRA_KEY)!!
                    mobileNo.setText(cred.getId().substring(3))
                }
            }
        } else {
            try {
                var imageuri: Uri? = null;
                if(data != null) {
                    imageuri = data?.getData();// Get intent
                } else {
                    imageuri = cameraOutputUri
                }
                val real_Path = BaseHelper.getRealPathFromUri(activity, imageuri);
                postUploadDocViewModel.loadData(State.upload_type, real_Path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
/*
        when(v?.id) {
            R.id.home_address ->{
                if(destination != null) {
                    System.out.print("startAnimation destination not null")
                    destination!!.removeView(temp_image_view)
                }
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                startAnimation(
                    white_car,
                    R.drawable.white_car,
                    300,
                    onbording_1,
                    0
                )
            }
            R.id.mobileNo ->{
                if(destination != null) {
                    System.out.print("startAnimation destination not null")
                    destination!!.removeView(temp_image_view)
                }
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                if(State.type == State.White_board || State.type == State.NoVehicles) {
                    startAnimation(
                        white_car,
                        R.drawable.white_car,
                        600,
                        onbording_4,
                        0
                    )
                } else {
                    startAnimation(
                        yellow_car,
                        R.drawable.yellow_car,
                        600,
                        onbording_4,
                        0
                    )
                }
            }
            R.id.officeAddress -> {
                if(destination != null) {
                    System.out.print("startAnimation destination not null")
                    destination!!.removeView(temp_image_view)
                }
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_5.getY().toInt()).setDuration(2000).start();
                destination = onbording_5
                if(State.type == State.White_board || State.type == State.NoVehicles) {
                    startAnimation(
                        white_car,
                        R.drawable.white_car,
                        600,
                        onbording_5,
                        0
                    )
                } else {
                    startAnimation(
                        yellow_car,
                        R.drawable.yellow_car,
                        600,
                        onbording_5,
                        0
                    )
                }
            }
        }
*/
        return true
    }

    fun resetCarPosition(car_image:ImageView,temporigin : View) {
        (car_image.parent as ViewGroup).removeView(car_image)
        destination?.addView(temporigin)

    }


    override fun onClick(v: View?) {

        val vlp = white_car?.layoutParams as ViewGroup.MarginLayoutParams
        val topMargin = Helper.dpToPx(activity!!,vlp.topMargin)

        when (v?.id)
        {

            R.id.no_vehicle_btn ->{
                no_vehicle_btn.background = resources.getDrawable(R.drawable.selected_radio_btn)
                private_vehicle_btn.background = resources.getDrawable(R.drawable.radio_btn)
                cab_vehicle_btn.background = resources.getDrawable(R.drawable.radio_btn)
                onbording_3.visibility = View.GONE
                cab_onbording_3.visibility = View.GONE
                tt_mini_bus.visibility = View.GONE
                if(destination != null && temp_image_view != null) {
                    destination!!.removeView(temp_image_view)
                }

                white_car.visibility = View.VISIBLE
                yellow_car.visibility = View.GONE
                State.role_id = "4"
                State.type = State.NoVehicles
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_3.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                if(isFirst) {
                    startAnimation(
                        white_car,
                        R.drawable.white_car,
                        2100,
                        onbording_1,
                        500
                    )
                    isFirst = false

                } else if(isFirst_P_Y) {
                    startAnimation(
                        white_car,
                        R.drawable.white_car,
                        1300,
                        onbording_1,
                        500
                    )
                    isFirst_P_Y = false

                } else {
                    startAnimation(
                        white_car,
                        R.drawable.white_car,
                        450,
                        onbording_1,
                        500
                    )
                }
            }
            R.id.private_vehicle_btn ->{

                no_vehicle_btn.background = resources.getDrawable(R.drawable.radio_btn)
                private_vehicle_btn.background = resources.getDrawable(R.drawable.selected_radio_btn)
                cab_vehicle_btn.background = resources.getDrawable(R.drawable.radio_btn)

                isFirst_P_Y = true
                tt_mini_bus.visibility = View.GONE
                onbording_3.visibility = View.VISIBLE
                cab_onbording_3.visibility = View.GONE
                if(destination != null && temp_image_view != null) {
                    System.out.print("startAnimation destination not null")
                    destination!!.removeView(temp_image_view)
                }
                white_car.visibility = View.VISIBLE
                yellow_car.visibility = View.GONE
                State.type = State.White_board
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_3.getY().toInt()).setDuration(2000).start();
                destination = onbording_3
                startAnimation(
                    white_car,
                    R.drawable.white_car,
                    650,
                    onbording_1,
                    600
                )
            }
            R.id.cab_vehicle_btn ->{
                no_vehicle_btn.background = resources.getDrawable(R.drawable.radio_btn)
                private_vehicle_btn.background = resources.getDrawable(R.drawable.radio_btn)
                cab_vehicle_btn.background = resources.getDrawable(R.drawable.selected_radio_btn)

                isFirst_P_Y = true

                var top_mar = 800
                if(isCabFisrst) {
                    top_mar = 1600
                    isCabFisrst = false
                }
                tt_mini_bus.visibility = View.VISIBLE
                onbording_3.visibility = View.GONE
                cab_onbording_3.visibility = View.VISIBLE
                if(destination != null && temp_image_view != null) {
                    destination!!.removeView(temp_image_view)
                }
                white_car.visibility = View.GONE
                yellow_car.visibility = View.VISIBLE
                State.role_id = "6"
                State.type = State.YELLOW_BOARD
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_3.getY().toInt()).setDuration(2000).start();
                destination = cab_onbording_3
                startAnimation(
                    yellow_car,
                    R.drawable.yellow_car,
                    top_mar,
                    onbording_1,
                    700
                )
            }

             R.id.btnNExt ->{
                 prepareParams()
            }
            R.id.get_otp ->{
                State.mobile = mobileNo.text.toString()
                if(validateMobileNumber()) {
                    callAPIRequestOTP()
                }
            }
             R.id.verify_otp ->{
                 State.otp_code = otp_number.text.toString()
                 if(validateOTp()) {
                     callAPIVerifyOTP()
                 }
            }
            R.id.female_button ->{
                female_button.setTextColor(context?.resources?.getColor(R.color.colorAccent)!!)
                 male_button.setTextColor(context?.resources?.getColor(R.color.DarkBlue)!!)
                 State.gender = "Female"

            }
            R.id.male_button ->{
                female_button.setTextColor(context?.resources?.getColor(R.color.DarkBlue)!!)
                male_button.setTextColor(context?.resources?.getColor(R.color.colorAccent)!!)
                State.gender = "Male"
            }
            R.id.vehicle_pic ->{
                State.upload_type = PostUploadDocViewModel.VEHICLE_PHOTO
                pickImage()
            }
            R.id.cab_upload_vehicle_pic ->{
                State.upload_type = PostUploadDocViewModel.VEHICLE_PHOTO
                pickImage()
            }
            R.id.uploadreg_no ->{
                State.upload_type = PostUploadDocViewModel.VEHICLE_REG_CERT_PHOTO
                pickImage()
            }
            R.id.upload_cab_reg_no ->{
                State.upload_type = PostUploadDocViewModel.VEHICLE_REG_CERT_PHOTO
                pickImage()
            }
            R.id.upload_dl ->{
                State.upload_type = PostUploadDocViewModel.VEHICLE_DL_PHOTO
                pickImage()
            }

            R.id.home_address ->{
                initSearch(REQUEST_CODE_AUTOCOMPLETE)
            }
            R.id.officeAddress ->{
                initSearch(REQUEST_CODE_AUTOCOMPLETE_OFFICE)
            }
            R.id.cab_upload_dl ->{
                State.upload_type = PostUploadDocViewModel.VEHICLE_DL_PHOTO
                pickImage()
            }
        }
    }


    var destination: RelativeLayout? = null
    var temp_image_view:ImageView? = null


    private fun startAnimation(
        car_image: View,
        drawable: Int,
        top_margin: Int,
        temporigin: View,
        dpToPx: Int
    ) {
        Helper.hideSoftKeyboard(activity!!)

        car_image.visibility = View.GONE
        val origin = car_image?.getParent() as View
        temp_image_view = ImageView(activity)
        // Create another TextView and initialise it to match textView
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        params.height = Helper.toDp(activity!!,80f)
        params.width = Helper.toDp(activity!!,61f)
        val displayMetrics = DisplayMetrics();
        activity?.getWindowManager()?.getDefaultDisplay()?.getMetrics(displayMetrics);
        if(State.type == State.White_board || State.type == State.NoVehicles) {
            params.setMargins(Helper.toDp(activity!!, 70f), 0, 0, 0)
        } else {
            params.setMargins(Helper.toDp(activity!!, 66f), 0, 0, 0)

        }

        temp_image_view!!.setImageDrawable(activity?.getDrawable(drawable))
        temp_image_view!!.layoutParams = params

        destination?.addView(temp_image_view)

        val inAnimator = getInAnimator(temp_image_view!!, origin, destination!!,temp_image_view!!,top_margin,dpToPx)
        inAnimator.start()


    }

    private fun getInAnimator(
        newView: View,
        origin: View,
        destination: View,
        textView: ImageView,
        top_margin: Int,
        dpToPx: Int): ObjectAnimator {

        // Calculate the difference between y of destination and of origin
        val layoutDifferenceX = (destination.y+dpToPx) - (origin.y + Helper.toDp(activity!!,top_margin.toFloat()))
        // initialX relative to destination
        var initialX = textView?.y!! - layoutDifferenceX
        System.out.println("layoutDifferenceX initialX "+initialX+" finalX ")

        if(initialX == 390f ){
            initialX = -2139f
        }
        // finalX relative to destination == initialX relative to origin
        var finalX = textView?.y

        val animator = ObjectAnimator.ofFloat(
            newView, "y",
            initialX!!, finalX!!+dpToPx
        )
        animator.setInterpolator(AccelerateDecelerateInterpolator())
        animator.setDuration(2000)

        return animator
    }

    fun prepareParams() {
        State.first_name = first_name.text.toString()
        State.last_name = last_name.text.toString()
        State.email = edtEmail.text.toString()
        State.office_email = edtofficeEmail.text.toString()
        State.mobile = mobileNo.text.toString()
        try {
            State.referel_code = referral_code.text.toString()
        } catch (e : Exception){
        }
        State.vehicle_type = State.type
        if(State.type == State.White_board) {
            State.vehicle_brand = edtVehicleBrand.text.toString()
            State.vehicle_name = VehicleName.text.toString()
            State.vehicle_no = reg_no.text.toString()
            State.dl_number = dl.text.toString()
        } else {
            State.vehicle_brand = cab_edtVehicleBrand.text.toString()
            State.vehicle_name =  cab_VehicleName.text.toString()
            State.vehicle_no = cab_reg_no.text.toString()
            State.dl_number = cab_dl.text.toString()
        }

        State.address_line1 = home_address.text.toString()
        State.formatted_address = home_address.text.toString()
        State.office_address_line1 = officeAddress.text.toString()
        State.office_formatted_address = officeAddress.text.toString()
        State.otp_code = otp_number.text.toString()
        //postLoginViewModel.loadData(state.LoginForm(State.mobile))
        validateForm()
    }

    fun getAddress(latitude : Double,longitude : Double): List<Address>? {
        val geocoder: Geocoder
        var addresses: List<Address>? = null
        geocoder = Geocoder(context, Locale.getDefault())

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addresses
    }

    fun callAPIRequestOTP() {
        postRequestOtpViewModel.loadData(State.mobile)
    }

    fun callAPIVerifyOTP() {
        postVerifyOtpViewModel.loadData(state.OtpForm())
    }

    fun callRegister() {
        if(isOtpVerified) {
            if (State.type == State.NoVehicles) {
                postUserSignupViewModel.loadData(
                    state.ApiSignupForm(),
                    JSONObject(),
                    jsonArray,
                    docjsonArray,
                    state.OtpForm()
                )
            } else {
                postUserSignupViewModel.loadData(
                    state.ApiSignupForm(),
                    state.Vehicle(),
                    jsonArray,
                    docjsonArray,
                    state.OtpForm()
                )
            }
        }  else {
            showNotifyDialog(
                "", "Please verify OTP",
                getString(R.string.ok),"",object : NotifyListener {
                    override fun onButtonClicked(which: Int) { }
                }
            )
        }
    }
    fun validateUploadForm() : Boolean {
        /*if(BaseHelper.isEmpty(vehicleID)) {
            er_tv1.visibility = View.VISIBLE
            er_tv1.text = "Upload Vehicle Photo"
            edtVehicleBrand.requestFocus();
            return false
        } else {
            edtVehicleBrand.clearFocus();
            er_tv1.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(registration_certificateID)){
            er_tv2.visibility = View.VISIBLE
            reg_no.requestFocus()
            er_tv2.text = "Upload vehicle registration certificate photo"

            return false
        } else {
            reg_no.clearFocus()
            er_tv2.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(driving_licenceID)){
            er_tv3.visibility = View.VISIBLE
            er_tv3.text = "upload your DL"
            dl.requestFocus()
            return false
        } else {
            dl.clearFocus()
            er_tv3.visibility = View.GONE
        }*/

        return true
    }

    fun validateCabVehicleForm() :Boolean{

        if(BaseHelper.isEmpty(State.vehicle_brand)) {
            caber_tvbrand.visibility = View.VISIBLE
            caber_tvbrand.text = "Enter vehicle brand"
            cab_edtVehicleBrand.requestFocus();
            return false
        } else {
            edtVehicleBrand.clearFocus();
            caber_tvbrand.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(State.vehicle_name)) {
            cab_er_tv1.visibility = View.VISIBLE
            cab_er_tv1.text = "Enter vehicle name"
            cab_VehicleName.requestFocus();
            return false
        } else {
            cab_VehicleName.clearFocus();
            cab_er_tv1.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.vehicle_no)){
            cab_er_tv2.visibility = View.VISIBLE
            cab_reg_no.requestFocus()
            cab_er_tv2.text = "Enter vehicle number"

            return false
        } else {
            cab_reg_no.clearFocus()
            cab_er_tv2.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(State.dl_number)){
            cab_er_tv3.visibility = View.VISIBLE
            cab_er_tv3.text = "Enter DL Number"
            cab_dl.requestFocus()
            return false
        } else {
            cab_dl.clearFocus()
            cab_er_tv3.visibility = View.GONE
        }

        return true
    }

    fun validateVehicleForm() :Boolean{

        if(BaseHelper.isEmpty(State.vehicle_brand)) {
            er_tvbrand.visibility = View.VISIBLE
            er_tvbrand.text = "Enter vehicle brand"
            edtVehicleBrand.requestFocus();
            return false
        } else {
            edtVehicleBrand.clearFocus();
            er_tvbrand.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(State.vehicle_name)) {
            er_tv1.visibility = View.VISIBLE
            er_tv1.text = "Enter vehicle name"
            VehicleName.requestFocus();
            return false
        } else {
            VehicleName.clearFocus();
            er_tv1.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.vehicle_no)){
            er_tv2.visibility = View.VISIBLE
            reg_no.requestFocus()
            er_tv2.text = "Enter vehicle number"

            return false
        } else {
            reg_no.clearFocus()
            er_tv2.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(State.dl_number)){
            er_tv3.visibility = View.VISIBLE
            er_tv3.text = "Enter DL Number"
            dl.requestFocus()
            return false
        } else {
            dl.clearFocus()
            er_tv3.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.role_id)) {
            er_tv1.visibility = View.VISIBLE
            er_tv1.text = "Select a Vehicle"
            VehicleName.requestFocus();
            return false
        }

        return true
    }

    fun validateaddressForm() :Boolean{

        if(BaseHelper.isEmpty(State.address_line1)) {
            er_mtv5.visibility = View.VISIBLE
            home_address.requestFocus()
            er_mtv5.text = "Enter Address"
            return false
        } else {
            home_address.clearFocus()
            er_mtv5.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.email) || !Helper.isValidEmail(State.email)) {
            er_mtv4.visibility = View.VISIBLE
            edtEmail.requestFocus()
            er_mtv4.text = "Enter valid email id"
            return false
        } else {
            edtEmail.clearFocus()
            er_mtv4.visibility = View.GONE
        }
/*
        if(State.type != State.YELLOW_BOARD) {
            if (BaseHelper.isEmpty(State.office_address_line1)) {
                er_otv1.visibility = View.VISIBLE
                edtofficeEmail.requestFocus()
                er_otv1.text = "Enter Address"
                return false
            } else {
                edtofficeEmail.clearFocus()
                er_otv1.visibility = View.GONE
            }

            if (BaseHelper.isEmpty(State.office_email) || !Helper.isValidEmail(State.office_email)) {
                er_otv2.visibility = View.VISIBLE
                edtofficeEmail.requestFocus()
                er_otv2.text = "Enter valid office mail id"
                return false
            } else {
                edtofficeEmail.clearFocus()
                er_otv2.visibility = View.GONE
            }
        }
*/

        return true
    }

    fun validateMobileNumber() :Boolean{
        if(BaseHelper.isEmpty(State.mobile) || !Helper.isValidMobile(State.mobile)) {
            er_mtv1.visibility = View.VISIBLE
            mobileNo.requestFocus()
            er_mtv1.text = "Enter valid mobile number"
            return false
        } else {
            mobileNo.clearFocus()
            er_mtv1.visibility = View.GONE

        }


        return true
    }

    fun validateOTp() :Boolean {
        if(BaseHelper.isEmpty(State.otp_code)) {
            er_mtv3.visibility = View.VISIBLE
            otp_number.requestFocus()
            er_mtv3.text = "Enter valid OTP number"
            return false
        } else {
            otp_number.clearFocus()
            er_mtv3.visibility = View.GONE
        }
        return true
    }

    fun validateAPIForm() :Boolean{
        if(BaseHelper.isEmpty(State.first_name)) {
            erfname_start.visibility = View.VISIBLE
            first_name.requestFocus()
            erfname_start.text = "Enter your first name"
            return false
        } else {
            first_name.clearFocus()
            erfname_start.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.last_name))
        {
            erlname_start.visibility = View.VISIBLE
            last_name.requestFocus()
            erlname_start.text = "Enter your last name"
            return false
        } else {
            last_name.clearFocus()
            erlname_start.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.gender))
        {
            er_start.visibility = View.VISIBLE
            last_name.requestFocus()
            er_start.text = "Select Gender"
            return false
        } else {
            last_name.clearFocus()
            er_start.visibility = View.GONE
        }



//        if(BaseHelper.isEmpty(State.role_type))
//            return false

        return true
    }

    fun validateForm() {


        when (State.type) {
            State.NoVehicles -> {
                jsonArray.put(0,state.Address(activity!!))
                if(!state.OfficeAddress(activity!!).isNull("lattitude") ||
                    !state.OfficeAddress(activity!!).isNull("longitude")) {
                    jsonArray.put(1, state.OfficeAddress(activity!!))
                }


                if(validateAPIForm() && validateMobileNumber()  && validateOTp() && validateaddressForm() ){
                    callRegister()
                }
            }

            State.YELLOW_BOARD -> {
                jsonArray.put(0,state.Address(activity!!))
                if(!state.OfficeAddress(activity!!).isNull("lattitude") ||
                    !state.OfficeAddress(activity!!).isNull("longitude")) {
                    jsonArray.put(1, state.OfficeAddress(activity!!))
                }
                if(validateAPIForm() && validateCabVehicleForm() && validateOTp()
                    && validateaddressForm() && validateUploadForm() ){
                    val vehicle_photo = JSONObject()
                    vehicle_photo.put("type",Keys.VEHICLE)
                    vehicle_photo.put("file_id",vehicleID)
                    vehicle_photo.put("file_name",vehicleID_name)

                    val vehicle_reg_photo = JSONObject()
                    vehicle_reg_photo.put("type",Keys.REGISTRATION_CERTIFICATE)
                    vehicle_reg_photo.put("file_id",registration_certificateID)
                    vehicle_reg_photo.put("file_name",registration_certificateID_name)

                    val vehicle_dl_photo = JSONObject()
                    vehicle_dl_photo.put("type",Keys.DRIVING_LICENCE)
                    vehicle_dl_photo.put("file_id",driving_licenceID)
                    vehicle_dl_photo.put("file_name",driving_licenceID_name)


                    if(!BaseHelper.isEmpty(vehicleID)) {
                        docjsonArray.put(0,vehicle_photo)
                    }
                    if(!BaseHelper.isEmpty(registration_certificateID)) {
                        docjsonArray.put(1,vehicle_reg_photo)
                    }
                    if(!BaseHelper.isEmpty(driving_licenceID)) {
                        docjsonArray.put(2,vehicle_dl_photo)
                    }

                    callRegister()
                }
            }
            State.White_board,State.BIKE_White_board -> {
                jsonArray.put(0,state.Address(activity!!))
                if(!state.OfficeAddress(activity!!).isNull("lattitude") ||
                    !state.OfficeAddress(activity!!).isNull("longitude")) {
                    jsonArray.put(1, state.OfficeAddress(activity!!))
                }
                System.out.println("validateAPIForm() Address " +
                        ""+state.Address(activity!!) +" OfficeAddress "+state.OfficeAddress(activity!!))
                if(validateAPIForm() && validateVehicleForm() && validateOTp()
                    && validateaddressForm() && validateUploadForm()){

                    val vehicle_photo = JSONObject()
                    vehicle_photo.put("type",Keys.VEHICLE)
                    vehicle_photo.put("file_id",vehicleID)
                    vehicle_photo.put("file_name",vehicleID_name)

                    val vehicle_reg_photo = JSONObject()
                    vehicle_reg_photo.put("type",Keys.REGISTRATION_CERTIFICATE)
                    vehicle_reg_photo.put("file_id",registration_certificateID)
                    vehicle_reg_photo.put("file_name",registration_certificateID_name)

                    val vehicle_dl_photo = JSONObject()
                    vehicle_dl_photo.put("type",Keys.DRIVING_LICENCE)
                    vehicle_dl_photo.put("file_id",driving_licenceID)
                    vehicle_dl_photo.put("file_name",driving_licenceID_name)


                    if(!BaseHelper.isEmpty(vehicleID)) {
                        docjsonArray.put(0,vehicle_photo)
                    }
                    if(!BaseHelper.isEmpty(registration_certificateID)) {
                        docjsonArray.put(1,vehicle_reg_photo)
                    }
                    if(!BaseHelper.isEmpty(driving_licenceID)) {
                        docjsonArray.put(2,vehicle_dl_photo)
                    }

                    callRegister()
                }
            }
        }
    }

    fun setUSerSignUpAPIObserver() {
        postUserSignupViewModel = ViewModelProviders.of(this).get(PostUserSignupViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                        PostUserSignupViewModel.NEXT_STEP -> {
                            UserInfoManager.getInstance(activity!!).saveAuthToken(postUserSignupViewModel.obj?.access_token!!)
                            UserInfoManager.getInstance(activity!!).saveAuthToken(postUserSignupViewModel.obj?.access_token!!)
                            UserInfoManager.getInstance(activity!!).saveAccountName(postUserSignupViewModel.obj?.name!!)
                            UserInfoManager.getInstance(activity!!).saveRoleType(postUserSignupViewModel.obj?.role_type!!)
                            UserInfoManager.getInstance(activity!!).saveReferralCode(postUserSignupViewModel.obj?.referel_code!!)
                            UserInfoManager.getInstance(activity!!).saveAccountId(
                                postUserSignupViewModel.obj?.user_id.toString()!!)
                            if(UserInfoManager.getInstance(activity!!).getFirstTime()) {
                                home().setFragment(TermsConditionsFragment().apply {
                                    isLogin = false
                                })
                            } else {
                                home().setFragment(ProfilePicUploadFragment())
                            }
                        }
                    }
                })
            }
        }
    }

    fun setRequestOtpAPIObserver() {
        postRequestOtpViewModel = ViewModelProviders.of(this).get(PostRequestOtpViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                        PostRequestOtpViewModel.NEXT_STEP -> {
                            showNotifyDialog(
                                "", postRequestOtpViewModel.obj?.message!!,
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) { }
                                }
                            )
                            val duration = (30000).toLong()
                            UserInfoManager.getInstance(activity!!.applicationContext).setOtpDuration(duration)
                            val nowCal = Calendar.getInstance()
                            nowCal.add(Calendar.MINUTE, TimeUnit.MILLISECONDS.toMinutes(duration).toInt())
                            nowCal.add(Calendar.SECOND, TimeUnit.MILLISECONDS.toSeconds(duration).toInt())
                            val otpExpirty = nowCal.getTimeInMillis()
                            UserInfoManager.getInstance(activity!!.applicationContext).setOTPExpiryTimeStamp(otpExpirty)
                            val str = context!!.getString(R.string.resend_OTP_available)
                            var start = 0
                            var end = 0
                            if (str.length > 20) {
                                start = str.length - 2
                                end = str.length + 2
                            } else {
                                start = 0
                                end = 5
                            }
                            counter.showCountDownTimer(object : OTPExpiryListener {
                                override fun onExpiry() {
                                    try {
                                        val string  = "Resend OTP"
                                        get_otp.setText(string)
                                    } catch (e: Exception) {
                                    }
                                }

                                override fun onStillValid() {}

                                override fun onFail() {}
                            }, context!!.applicationContext, get_otp, str, start, end)

                        }
                    }
                })

            }
        }
    }

    fun setVerifyOtpAPIObserver() {
        postVerifyOtpViewModel = ViewModelProviders.of(this).get(PostVerifyOtpViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                        PostVerifyOtpViewModel.NEXT_STEP -> {
                            isOtpVerified = true
                            showNotifyDialog(
                                "", postVerifyOtpViewModel.obj?.message!!,
                                getString(R.string.ok),"",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) { }
                                }
                            )
                        }
                    }
                })

            }
        }
    }

    fun setVehicleTypeAPIObserver() {
        getVehicleTypeViewModel = ViewModelProviders.of(this).get(GetVehicleTypeViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                        GetVehicleTypeViewModel.NEXT_STEP -> {

                        }
                    }
                })

            }
        }
    }

    fun setUploadDocObserver() {
        postUploadDocViewModel = ViewModelProviders.of(this).get(PostUploadDocViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                        PostUploadDocViewModel.NEXT_STEP -> {
                            when(State.upload_type) {
                                PostUploadDocViewModel.VEHICLE_PHOTO -> {
                                    vehicleID = postUploadDocViewModel.obj?.file_id!!
                                    vehicleID_name = postUploadDocViewModel.obj?.file_name!!
                                    if(State.type == State.White_board) {
                                        tvuploadvehicle_pic.text = "Uploaded"
                                        tvuploadvehicle_pic.setTextColor(activity?.resources?.getColor(R.color.Green)!!)
                                    } else {
                                        tvcab_upload_vehicle_pic.text = "Uploaded"
                                        tvcab_upload_vehicle_pic.setTextColor(activity?.resources?.getColor(R.color.Green)!!)
                                    }
                                }
                                PostUploadDocViewModel.VEHICLE_REG_CERT_PHOTO -> {
                                    registration_certificateID = postUploadDocViewModel.obj?.file_id!!
                                    registration_certificateID_name = postUploadDocViewModel.obj?.file_name!!
                                    if(State.type == State.White_board) {
                                        tvuploadreg_no.text = "Uploaded"
                                        tvuploadreg_no.setTextColor(activity?.resources?.getColor(R.color.Green)!!)
                                    } else {
                                        tvupload_cab_reg_no.text = "Uploaded"
                                        tvupload_cab_reg_no.setTextColor(activity?.resources?.getColor(R.color.Green)!!)
                                    }
                                }
                                PostUploadDocViewModel.VEHICLE_DL_PHOTO -> {
                                    driving_licenceID = postUploadDocViewModel.obj?.file_id!!
                                    driving_licenceID_name = postUploadDocViewModel.obj?.file_name!!
                                    if(State.type == State.White_board) {
                                        tvupload_dl.text = "Uploaded"
                                        tvupload_dl.setTextColor(activity?.resources?.getColor(R.color.Green)!!)
                                    } else {
                                        tvcab_upload_dl.text = "Uploaded"
                                        tvcab_upload_dl.setTextColor(activity?.resources?.getColor(R.color.Green)!!)
                                    }
                                }
                                PostUploadDocViewModel.PROFILE_PHOTO -> {
                                    driving_licenceID = postUploadDocViewModel.obj?.file_id!!
                                    driving_licenceID_name = postUploadDocViewModel.obj?.file_name!!

                                }
                            }
                        }
                    }
                })

            }
        }
    }


    class State {
        companion object {
            var first_name = ""
            var last_name = ""
            var gender = ""
            var email = ""
            var office_email = ""
            var mobile = ""
            var referel_code = ""
            var vehicle_type = 0
            var vehicle_brand = ""
            var vehicle_name = ""
            var vehicle_no = ""
            var address_line1 = ""
            var lattitude = 0.0
            var officelattitude = 0.0
            var officelongitude = 0.0
            var longitude = 0.0
            var formatted_address = ""
            var otp_code = ""
            var role_id = ""
            var vehicle_model_type = ""
            var dl_number = ""
            var office_address_line1 = ""
            var office_formatted_address = ""

            var YELLOW_BOARD = 1
            var White_board = 2
            var BIKE_White_board = 3
            var NoVehicles = 4


            var type = NoVehicles
            var upload_type = PostUploadDocViewModel.VEHICLE_PHOTO
        }

        fun OtpForm() :JSONObject {
            val obj = JSONObject()
            if(!BaseHelper.isEmpty(otp_code))
                obj.put("otp_code", otp_code)
            if(!BaseHelper.isEmpty(mobile))
                obj.put("mobile", mobile)
            return obj
        }

        fun Address(context : Context) :JSONObject {
            val obj = JSONObject()

            if(!BaseHelper.isEmpty(address_line1))
                obj.put("address_line1", address_line1)
            obj.put("lattitude", lattitude.toString())
            obj.put("longitude", longitude.toString())
            obj.put("type", "home")

            if(!BaseHelper.isEmpty(formatted_address))
                obj.put("formatted_address", formatted_address)

            return obj
        }

        fun OfficeAddress(context : Context) :JSONObject {
            val obj = JSONObject()

            if(!BaseHelper.isEmpty(office_address_line1)) {
                obj.put("address_line1", office_address_line1)
                obj.put("lattitude", officelattitude.toString())
                obj.put("longitude", officelongitude.toString())
                obj.put("type", "office")
            }

            if(!BaseHelper.isEmpty(office_formatted_address))
                obj.put("formatted_address", office_formatted_address)

            return obj
        }

        fun Vehicle() : JSONObject {
            val obj = JSONObject()
            obj.put("vehicle_type", vehicle_type)
            if(!BaseHelper.isEmpty(vehicle_brand))
                obj.put("vehicle_brand", vehicle_brand)

            if(!BaseHelper.isEmpty(vehicle_name))
                obj.put("vehicle_name", vehicle_name)

            if(!BaseHelper.isEmpty(vehicle_no))
                obj.put("vehicle_no", vehicle_no)

            if(!BaseHelper.isEmpty(vehicle_model_type))
                obj.put("vehicle_model_type", vehicle_model_type)

            return obj
        }


        fun ApiSignupForm() : JSONObject {
            val obj = JSONObject()
            if(!BaseHelper.isEmpty(first_name))
                obj.put("first_name", first_name)

            if(!BaseHelper.isEmpty(last_name))
                obj.put("last_name", last_name)

            if(!BaseHelper.isEmpty(gender))
                obj.put("gender", gender)

            if(!BaseHelper.isEmpty(email))
                obj.put("email", email)

            if(!BaseHelper.isEmpty(office_email))
                obj.put("office_email", office_email)

            if(!BaseHelper.isEmpty(mobile))
                obj.put("mobile", mobile)

            if(!BaseHelper.isEmpty(role_id))
                obj.put("role_id", role_id)

            obj.put("referel_code", referel_code)

            if(!BaseHelper.isEmpty(dl_number))
                obj.put("dl_number", dl_number)

            return obj
        }

    }

}



