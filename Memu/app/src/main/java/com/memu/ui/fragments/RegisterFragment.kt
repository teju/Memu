package com.memu.ui.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.location.*
import android.os.Bundle
import android.view.*
import androidx.core.animation.doOnEnd
import com.memu.R
import com.memu.etc.Helper
import com.memu.ui.BaseFragment
import kotlinx.android.synthetic.main.register_fragment.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import kotlinx.android.synthetic.main.onboarding_four.*
import kotlinx.android.synthetic.main.onboarding_one.*
import kotlinx.android.synthetic.main.onboarding_three.*
import kotlinx.android.synthetic.main.onboarding_two.*
import org.json.JSONArray
import org.json.JSONObject
import android.widget.Toast
import android.view.ViewTreeObserver
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.iapps.gon.etc.callback.PermissionListener
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.memu.etc.GPSTracker
import com.memu.etc.Keys
import com.memu.etc.UserInfoManager
import com.memu.webservices.*
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.onboarding_start.*
import kotlinx.android.synthetic.main.onboarding_two_temp.*
import kotlinx.android.synthetic.main.register_fragment.btnNExt
import kotlinx.android.synthetic.main.register_fragment.ld
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class RegisterFragment : BaseFragment() , View.OnClickListener,View.OnTouchListener {


    private val REQUEST_CODE_AUTOCOMPLETE: Int = 1002
    private val REQUEST_CODE_AUTOCOMPLETE_OFFICE: Int = 1003
    private val REQUEST_CODE_UPLOAD_VEHICLE: Int = 1004
    private val REQUEST_CODE_UPLOAD_REG: Int = 1005
    private val REQUEST_CODE_UPLOAD_DL: Int = 1006
    lateinit var getVehicleTypeViewModel: GetVehicleTypeViewModel
    lateinit var postLoginViewModel: PostLoginViewModel
    lateinit var postOtpViewModel: PostOtpViewModel
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
    var gpsTracker : GPSTracker? = null
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


        setVehicleTypeAPIObserver()
        setUSerSignUpAPIObserver()
        setRequestOtpAPIObserver()
        setVerifyOtpAPIObserver()
        setUploadDocObserver()
        setLoginAPIObserver()
        setOtpAPIObserver()
        getVehicleTypeViewModel.loadData()
        //onScrolledUp()

        onbording_1.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    // TODO Auto-generated method stub
                    val h = onbording_1.getHeight() + Helper.dpToPx(activity!!,80)
                    val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.width = Helper.dpToPx(activity!!,570)
                    params.setMargins(-200,h,0,0)
                    flyover.layoutParams = params
                }
            })
        permissions()

        car_rd_btn.setOnCheckedChangeListener { buttonView, isChecked ->
            if(bike_rd_btn.isChecked) {
                bike_rd_btn.isChecked = false
            }
            car_rd_btn.isChecked = isChecked
            State.role_id = "3"
            State.type = State.White_board

        }
        bike_rd_btn.setOnCheckedChangeListener { buttonView, isChecked ->
            if(car_rd_btn.isChecked) {
                car_rd_btn.isChecked = false
            }
            bike_rd_btn.isChecked = isChecked
            State.role_id = "5"
            State.type = State.BIKE_White_board
        }
        dl.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                mobileNo.requestFocus()
                true
            } else {
                false
            }
        }
        gpsTracker = GPSTracker(activity)
        if(gpsTracker?.canGetLocation()!!) {
            val getAddress = getAddress(gpsTracker?.latitude!!, gpsTracker?.longitude!!)
            home_address.text = getAddress?.get(0)?.getAddressLine(0)
        }

   }

    fun permissions() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onUserNotGrantedThePermission() {
            }

            override fun onCheckPermission(permission: String, isGranted: Boolean) {
                if (isGranted) {
                    onPermissionAlreadyGranted()
                } else {
                    onUserNotGrantedThePermission()
                }
            }

            @SuppressLint("MissingPermission")
            override fun onPermissionAlreadyGranted() {

            }
        }
        val permissions = ArrayList<String>()
        permissions.add(android.Manifest.permission.CAMERA)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        checkPermissions(permissions, permissionListener)

    }



    override fun onClick(v: View?) {
        if(destination != null) {
            destination!!.removeView(temp_image_view)
        }
        val vlp = white_car?.layoutParams as ViewGroup.MarginLayoutParams
        val topMargin = Helper.dpToPx(activity!!,vlp.topMargin)

        when (v?.id)
        {

            R.id.no_vehicle_btn ->{
                white_car.visibility = View.VISIBLE
                yellow_car.visibility = View.GONE
                State.role_id = "4"
                State.type = State.NoVehicles
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                startAnimation(white_car,R.drawable.white_car,300,onbording_1)
            }
            R.id.private_vehicle_btn ->{
                white_car.visibility = View.VISIBLE
                yellow_car.visibility = View.GONE
                State.type = State.White_board
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_3.getY().toInt()).setDuration(2000).start();
                destination = onbording_3
                startAnimation(white_car,R.drawable.white_car,400,onbording_1 )
            }
            R.id.cab_vehicle_btn ->{
                white_car.visibility = View.GONE
                yellow_car.visibility = View.VISIBLE
                State.role_id = "6"
                State.type = State.YELLOW_BOARD
                ObjectAnimator.ofInt(sv, "scrollY",  cab_onbording_3.getY().toInt()).setDuration(2000).start();
                destination = cab_onbording_3
                startAnimation(yellow_car,R.drawable.yellow_car,600,onbording_1 )
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

    fun initSearch(code : Int) {
        val intent = PlaceAutocomplete.IntentBuilder()
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
            .build(activity)
        startActivityForResult(intent, code)
    }


    fun pickImage() {
      val intent = Intent(Intent.ACTION_PICK);
      intent.setType("image/*");
      startActivityForResult(intent, PICK_PHOTO_DOC);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if(requestCode == REQUEST_CODE_AUTOCOMPLETE) {
                State.selectedCarmenFeaturehome = PlaceAutocomplete.getPlace(data);
                home_address.setText(State.selectedCarmenFeaturehome !!.placeName())
            } else if(requestCode == REQUEST_CODE_AUTOCOMPLETE_OFFICE) {
                State.selectedCarmenFeatureoffice = PlaceAutocomplete.getPlace(data);
                officeAddress.setText(State.selectedCarmenFeatureoffice !!.placeName())
            } else {
                try {

                    val imageuri = data?.getData();// Get intent
                    // Get real path and show over text view
                    val real_Path = BaseHelper.getRealPathFromUri(activity, imageuri);
                    postUploadDocViewModel.loadData(State.upload_type, real_Path)
                } catch (e: Exception) {
                }
            }
        }
    }
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
       when(v?.id) {
           R.id.home_address ->{
               ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
               destination = onbording_4
               startAnimation(white_car,R.drawable.white_car,300,onbording_1 )
           }
       }
        return true
    }

    fun resetCarPosition(car_image:ImageView,temporigin : View) {
        (car_image.parent as ViewGroup).removeView(car_image)
        destination?.addView(temporigin)

    }

    var destination: RelativeLayout? = null
    var temp_image_view:ImageView? = null

    private fun startAnimation(car_image:ImageView,drawable : Int,top_margin : Int,temporigin : View) {
        car_image.visibility = View.GONE
        val origin = car_image?.getParent() as View
        temp_image_view = ImageView(activity)
        // Create another TextView and initialise it to match textView
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.height = Helper.dpToPx(activity!!,100)
        params.width = Helper.dpToPx(activity!!,81)
        params.setMargins(Helper.dpToPx(activity!!,90),Helper.dpToPx(activity!!,top_margin),0,0)
        temp_image_view!!.setImageDrawable(activity?.getDrawable(drawable))
        temp_image_view!!.layoutParams = params
        //(car_image.parent as ViewGroup).removeView(car_image)
        // Add the new TextView to the destination LinearLayout
        destination?.addView(temp_image_view)

        // Create animations based on origin and destination LinearLayouts
        val outAnimator = getOutAnimator(origin, destination!!,temp_image_view!!)
        // The in animator also requires a reference to the new TextView
        val inAnimator = getInAnimator(temp_image_view!!, origin, destination!!,temp_image_view!!,top_margin)
        // All animators must be created before any are started because they are calculated
        // using values that are modified by the animation itself.
        outAnimator.start()
        inAnimator.start()
        // Add a listener to update textView reference to the new TextView when complete.
        inAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    /**
     * This method creates an ObjectAnimator to move the existing TextView out of its parent
     * towards its destination
     */
    private fun getOutAnimator(origin: View, destination: View,textView : ImageView): ObjectAnimator {

        // Calculate the difference between x of destination and of origin
        val layoutDifferenceX = destination.y - origin.y + 200
        // initialX is simply textView.getX()
        // the distance moved == layoutDifferenceX
        val finalX = textView?.getX()!! + layoutDifferenceX

        val animator = ObjectAnimator.ofFloat(
            textView, "y",
            textView?.getX()!!, finalX!!
        )
        animator.setInterpolator(AccelerateDecelerateInterpolator())
        animator.setDuration(2000)

        return animator
    }

    /**
     * This method creates an ObjectAnimator to move the new TextView from the initial position
     * of textView, relative to the new TextView's parent, to its destination.
     */
    private fun getInAnimator(newView: View, origin: View,
                              destination: View,textView : ImageView,top_margin : Int): ObjectAnimator {

        // Calculate the difference between y of destination and of origin
        val layoutDifferenceX = destination.y - (origin.y + Helper.dpToPx(activity!!,top_margin))
        // initialX relative to destination
        val initialX = textView?.getX()!! - layoutDifferenceX

        // finalX relative to destination == initialX relative to origin
        val finalX = textView?.getX()
        val animator = ObjectAnimator.ofFloat(
            newView, "y",
            initialX!!, finalX!!
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
        State.referel_code = ""
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
        if(State.type == State.NoVehicles) {
            postUserSignupViewModel.loadData(
                state.ApiSignupForm(),
                JSONObject(),
                jsonArray,
                docjsonArray,
                state.OtpForm()
            )
        } else  {
            postUserSignupViewModel.loadData(
                state.ApiSignupForm(),
                state.Vehicle(),
                jsonArray,
                docjsonArray,
                state.OtpForm()
            )
        }
    }
    fun validateUploadForm() : Boolean {
        if(BaseHelper.isEmpty(vehicleID)) {
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
        }

        return true
    }

    fun validateCabVehicleForm() :Boolean{

        if(BaseHelper.isEmpty(State.vehicle_brand)) {
            cab_er_tv1.visibility = View.VISIBLE
            cab_er_tv1.text = "Enter vehicle brand"
            cab_edtVehicleBrand.requestFocus();
            return false
        } else {
            edtVehicleBrand.clearFocus();
            cab_edtVehicleBrand.visibility = View.GONE
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
            cab_er_tv1.visibility = View.VISIBLE
            cab_reg_no.requestFocus()
            cab_er_tv1.text = "Enter vehicle number"

            return false
        } else {
            cab_reg_no.clearFocus()
            cab_er_tv1.visibility = View.GONE
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
            er_tv1.visibility = View.VISIBLE
            er_tv1.text = "Enter vehicle brand"
            edtVehicleBrand.requestFocus();
            return false
        } else {
            edtVehicleBrand.clearFocus();
            er_tv1.visibility = View.GONE
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
            er_tv1.text = "Select Car or Bike"
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
            er_mtv3.text = "Enter valid otp number"
            return false
        } else {
            otp_number.clearFocus()
            er_mtv3.visibility = View.GONE
        }
        return true
    }

    fun validateAPIForm() :Boolean{
        if(BaseHelper.isEmpty(State.first_name)) {
            er_start.visibility = View.VISIBLE
            first_name.requestFocus()
            er_start.text = "Enter your first name"
            return false
        } else {
            first_name.clearFocus()
            er_start.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.last_name))
        {
            er_start.visibility = View.VISIBLE
            last_name.requestFocus()
            er_start.text = "Enter your last name"
            return false
        } else {
            last_name.clearFocus()
            er_start.visibility = View.GONE
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
                jsonArray.put(1,state.OfficeAddress(activity!!))


                if(validateAPIForm() && validateMobileNumber()  && validateOTp() && validateaddressForm() ){
                    callRegister()
                }
            }

            State.YELLOW_BOARD -> {
                jsonArray.put(0,state.Address(activity!!))
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


                    docjsonArray.put(0,vehicle_photo)
                    docjsonArray.put(1,vehicle_reg_photo)
                    docjsonArray.put(2,vehicle_dl_photo)

                    callRegister()
                }
            }
            State.White_board,State.BIKE_White_board -> {
                jsonArray.put(0,state.Address(activity!!))
                jsonArray.put(1,state.OfficeAddress(activity!!))
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


                    docjsonArray.put(0,vehicle_photo)
                    docjsonArray.put(1,vehicle_reg_photo)
                    docjsonArray.put(2,vehicle_dl_photo)

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
                            home().setFragment(HomeFragment())
                            UserInfoManager.getInstance(activity!!).saveAuthToken(postUserSignupViewModel.obj?.access_token!!)
                            UserInfoManager.getInstance(activity!!).saveAuthToken(postUserSignupViewModel.obj?.access_token!!)
                            UserInfoManager.getInstance(activity!!).saveAccountName(postUserSignupViewModel.obj?.name!!)
                            UserInfoManager.getInstance(activity!!).saveAccountId(
                                postUserSignupViewModel.obj?.user_id.toString()!!)
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
    fun setLoginAPIObserver() {
        postLoginViewModel = ViewModelProviders.of(this).get(PostLoginViewModel::class.java).apply {
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
                        PostLoginViewModel.NEXT_STEP -> {
                            val jsonObject = JSONObject()
                            jsonObject.put("otp_code","123456")
                            val _state = State()
                            postOtpViewModel.loadData(_state.LoginForm(State.mobile),jsonObject)

                        }
                    }
                })

            }
        }
    }
    fun setOtpAPIObserver() {
        postOtpViewModel = ViewModelProviders.of(this).get(PostOtpViewModel::class.java).apply {
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
                        PostOtpViewModel.NEXT_STEP -> {
                            home().setFragment(HomeFragment())
                            UserInfoManager.getInstance(activity!!).saveAuthToken(postOtpViewModel.obj?.access_token!!)
                            UserInfoManager.getInstance(activity!!).saveAuthToken(postOtpViewModel.obj?.access_token!!)
                            UserInfoManager.getInstance(activity!!).saveAccountName(postOtpViewModel.obj?.name!!)
                            UserInfoManager.getInstance(activity!!).saveAccountId(
                                postOtpViewModel.obj?.user_id.toString()!!)
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
            var longitude = 0.0
            var formatted_address = ""
            var otp_code = ""
            var role_id = ""
            var dl_number = ""
            var office_address_line1 = ""
            var office_formatted_address = ""

            var YELLOW_BOARD = 1
            var White_board = 2
            var BIKE_White_board = 3
            var NoVehicles = 4
            var selectedCarmenFeaturehome: CarmenFeature? = null
            var selectedCarmenFeatureoffice: CarmenFeature? = null

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
                val latLng = BaseHelper.getLocationFromAddress(office_address_line1,context)
                if(latLng != null) {
                    obj.put("lattitude", latLng.latitude.toString())
                    obj.put("longitude", latLng.longitude.toString())
                }
                obj.put("type", "home")

            if(!BaseHelper.isEmpty(formatted_address))
                obj.put("formatted_address", formatted_address)

            return obj
        }

        fun OfficeAddress(context : Context) :JSONObject {
            val obj = JSONObject()

            if(!BaseHelper.isEmpty(office_address_line1))
                obj.put("address_line1", office_address_line1)
                val latLng = BaseHelper.getLocationFromAddress(office_address_line1,context)
            if(latLng != null) {

                obj.put("lattitude", latLng.latitude.toString())

                obj.put("longitude", latLng.longitude.toString())
            }

            obj.put("type", "office")

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

            return obj
        }

        fun LoginForm(username : String) : JSONObject {
            val obj = JSONObject()
            obj.put("username", username)
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



