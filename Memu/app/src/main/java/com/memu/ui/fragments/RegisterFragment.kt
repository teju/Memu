package com.memu.ui.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import com.memu.webservices.GetVehicleTypeViewModel
import com.memu.webservices.PostUserSignupViewModel
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
import com.memu.webservices.PostRequestOtpViewModel
import com.memu.webservices.PostVerifyOtpViewModel
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.iapps.gon.etc.callback.PermissionListener


class RegisterFragment : BaseFragment() , View.OnClickListener {

    lateinit var getVehicleTypeViewModel: GetVehicleTypeViewModel
    lateinit var postUserSignupViewModel: PostUserSignupViewModel
    lateinit var postRequestOtpViewModel: PostRequestOtpViewModel
    lateinit var postVerifyOtpViewModel: PostVerifyOtpViewModel
    val jsonArray = JSONArray()
    val state = State()
    private var locationManager : LocationManager? = null

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
        private_vehicle_btn.setOnClickListener(this)
        btnNExt.setOnClickListener(this)
        get_otp.setOnClickListener(this)
        verify_otp.setOnClickListener(this)
        cab_vehicle_btn.setOnClickListener(this)
        setVehicleTypeAPIObserver()
        setUSerSignUpAPIObserver()
        setRequestOtpAPIObserver()
        setVerifyOtpAPIObserver()
        locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager?;

        getVehicleTypeViewModel.loadData()
        //onScrolledUp()

        onbording_1.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    // TODO Auto-generated method stub
                    val h = onbording_1.getHeight()
                    val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0,h,0,0)
                    flyover.layoutParams = params
                }
            })

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
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener);
            }
        }
        val permissions = ArrayList<String>()
        permissions.add(android.Manifest.permission.CAMERA)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        checkPermissions(permissions, permissionListener)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            State.lattitude = location.latitude
            State.longitude = location.longitude
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    fun onScrolledUp() {
        sv.getViewTreeObserver()
            .addOnScrollChangedListener(ViewTreeObserver.OnScrollChangedListener {
                if (sv != null) {
                    if (onbording_1.getScrollY() === 0) {
                        Toast.makeText(context, "top", Toast.LENGTH_SHORT).show()
                    } else {

                    }
                }
            })
    }

    override fun onClick(v: View?) {
        if(destination != null) {
            destination!!.removeView(textViewNew)
        }
        when (v?.id)
        {
            R.id.no_vehicle_btn ->{
                State.type = State.NoVehicles
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_3.getY().toInt()).setDuration(2000).start();
                destination = onbording_3
                startAnimation(white_car,R.drawable.white_car,300 )
            }
            R.id.private_vehicle_btn ->{
                State.type = State.White_board
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_5.getY().toInt()).setDuration(2000).start();
                destination = onbording_5

                startAnimation(white_car,R.drawable.white_car,300 )
            }
            R.id.cab_vehicle_btn ->{
                State.type = State.YELLOW_BOARD
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                startAnimation(yellow_car,R.drawable.yellow_car,600 )
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
                 if(validateOTPForm()) {
                     callAPIVerifyOTP()
                 }
            }
        }
    }

    var destination: RelativeLayout? = null
    var textViewNew:ImageView? = null

    private fun startAnimation(textView:ImageView,drawable : Int,top_margin : Int) {
        textView.visibility = View.GONE
        val origin = textView?.getParent() as FrameLayout
        textViewNew = ImageView(activity)
        // Create another TextView and initialise it to match textView
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(Helper.dpToPx(activity!!,90),Helper.dpToPx(activity!!,top_margin),0,0)
        textViewNew!!.setImageDrawable(activity?.getDrawable(drawable))
        textViewNew!!.layoutParams = params

        // Add the new TextView to the destination LinearLayout
        destination?.addView(textViewNew)

        // Create animations based on origin and destination LinearLayouts
        val outAnimator = getOutAnimator(origin, destination!!,textView)
        // The in animator also requires a reference to the new TextView
        val inAnimator = getInAnimator(textViewNew!!, origin, destination!!,textView,top_margin)
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
        State.first_name = "Teju"
        State.last_name = "N"
        State.gender = "F"
        State.email = edtEmail.text.toString()
        State.office_email = edtofficeEmail.text.toString()
        State.mobile = mobileNo.text.toString()
        State.referel_code = ""
        State.vehicle_type = State.type
        State.vehicle_brand = edtVehicleBrand.text.toString()
        State.vehicle_name = VehicleName.text.toString()
        State.vehicle_no = reg_no.text.toString()
        State.type = State.NoVehicles
        State.address_line1 = home_address.text.toString()
        State.formatted_address = home_address.text.toString()
        State.office_address_line1 = officeAddress.text.toString()
        State.office_formatted_address = officeAddress.text.toString()
        State.otp_code = otp_number.text.toString()
        State.dl_number = dl.text.toString()

        validateForm()
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
                state.OtpForm()
            )
        } else {
            postUserSignupViewModel.loadData(
                state.ApiSignupForm(),
                state.Vehicle(),
                jsonArray,
                state.OtpForm()
            )
        }
    }

    fun validateVehicleForm() :Boolean{

        if(BaseHelper.isEmpty(State.vehicle_brand)) {
            er_tv1.visibility = View.VISIBLE
            return false
        } else {
            er_tv1.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(State.vehicle_name)) {
            er_tv1.visibility = View.VISIBLE
            return false
        } else {
            er_tv1.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(State.vehicle_no)){
            er_tv2.visibility = View.VISIBLE
            return false
        } else {
            er_tv2.visibility = View.GONE
        }

        return true
    }

    fun validateaddressForm() :Boolean{
        if(BaseHelper.isEmpty(State.address_line1)) {
            er_mtv5.visibility = View.VISIBLE
            return false
        } else {
            er_mtv5.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.formatted_address)){
            er_mtv5.visibility = View.VISIBLE
            return false
        } else {
            er_mtv5.visibility = View.GONE
        }


        /* if(State.lattitude == 0.0)
               return false

           if(State.longitude == 0.0)
               return false*/

        return true
    }

    fun validateOTPForm() :Boolean{
        if(BaseHelper.isEmpty(State.otp_code)) {
            er_mtv3.visibility = View.VISIBLE
            return false
        } else {
            er_mtv3.visibility = View.GONE
        }
        if(BaseHelper.isEmpty(State.mobile) || !Helper.isValidMobile(State.mobile)) {
            er_mtv1.visibility = View.VISIBLE
            return false
        } else {
            er_mtv1.visibility = View.GONE

        }
        return true
    }

    fun validateMobileNumber() :Boolean{

        return true
    }

    fun validateAPIForm() :Boolean{
        if(BaseHelper.isEmpty(State.first_name))
            return false

        if(BaseHelper.isEmpty(State.last_name))
            return false

        if(BaseHelper.isEmpty(State.gender))
            return false

        if(BaseHelper.isEmpty(State.email) || !Helper.isValidEmail(State.email)) {
            er_mtv4.visibility = View.VISIBLE
            return false
        } else {
            er_mtv4.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.office_email)|| !Helper.isValidEmail(State.office_email)){
            er_otv2.visibility = View.VISIBLE
            return false
        } else {
            er_otv2.visibility = View.GONE
        }

        if(BaseHelper.isEmpty(State.role_type))
            return false

        if(!validateMobileNumber())
            return false

        return true
    }

    fun validateForm() {
        when (State.type) {
            State.NoVehicles -> {
                println("State.type "+validateOTPForm() +" "+validateaddressForm() +" "+validateAPIForm())

                if(validateOTPForm() && validateaddressForm() ){
                    callRegister()
                }
            }

            State.YELLOW_BOARD -> {
                if(validateOTPForm() && validateaddressForm() && validateAPIForm() && validateVehicleForm()){
                    callRegister()
                } else {
                    Toast.makeText(context, "invalid validations", Toast.LENGTH_SHORT).show()
                }
            }
            State.White_board -> {
                if(validateOTPForm() && validateaddressForm() && validateAPIForm() && validateVehicleForm()){
                    callRegister()
                } else {
                    Toast.makeText(context, "invalid validations", Toast.LENGTH_SHORT).show()
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



    class State {
        companion object {
            var first_name = "teju"
            var last_name = "N"
            var gender = "Female"
            var email = ""
            var office_email = ""
            var mobile = ""
            var role_type = ""
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
            var dl_number = ""
            var office_address_line1 = ""
            var office_formatted_address = ""

            var YELLOW_BOARD = 1
            var White_board = 2
            var NoVehicles = 4

            var type = NoVehicles

        }

        fun OtpForm() :JSONObject {
            val obj = JSONObject()
            if(!BaseHelper.isEmpty(otp_code))
                obj.put("otp_code", otp_code)
            if(!BaseHelper.isEmpty(mobile))
                obj.put("mobile", mobile)
            return obj
        }

        fun Address() :JSONObject {
            val obj = JSONObject()

            if(!BaseHelper.isEmpty(address_line1))
                obj.put("address_line1", address_line1)

                obj.put("lattitude", lattitude)

                obj.put("longitude", longitude)

            if(!BaseHelper.isEmpty(formatted_address))
                obj.put("formatted_address", formatted_address)

            return obj
        }

        fun OfficeAddress() :JSONObject {
            val obj = JSONObject()

            if(!BaseHelper.isEmpty(office_address_line1))
                obj.put("address_line1", office_address_line1)

                obj.put("lattitude", lattitude)

                obj.put("longitude", longitude)

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

            if(!BaseHelper.isEmpty(role_type))
                obj.put("role_type", role_type)

            if(!BaseHelper.isEmpty(referel_code))
                obj.put("referel_code", referel_code)

            return obj
        }

    }

}



