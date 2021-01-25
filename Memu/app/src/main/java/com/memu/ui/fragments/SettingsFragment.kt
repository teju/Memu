package com.memu.ui.fragments

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import androidx.lifecycle.ViewModelProviders
import org.json.JSONObject
import com.iapps.gon.etc.callback.NotifyListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.iapps.libs.helpers.BaseHelper
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager
import com.memu.modules.userDetails.Vehicle
import com.memu.ui.activity.SearchActivity
import com.memu.ui.adapters.VehiclesAdapter
import com.memu.webservices.*
import kotlinx.android.synthetic.main.item_address.view.*
import kotlinx.android.synthetic.main.settings_fragment.*

import org.json.JSONArray
import java.lang.Exception
import kotlin.collections.ArrayList


class SettingsFragment : BaseFragment()  {
    lateinit var vehiclesAdapter: VehiclesAdapter
    lateinit var postUSerDetailsViewModel: PostUSerDetailsViewModel
    lateinit var postUSerUpdateViewModel: PostUSerUpdateViewModel
    lateinit var postUploadDocViewModel: PostUploadDocViewModel

    private val REQUEST_CODE_AUTOCOMPLETE: Int = 1002
    private val REQUEST_CODE_AUTOCOMPLETE_OFFICE: Int = 1003
    val state = State()
    val jsonArray = JSONArray()
    private var cameraOutputUri: Uri? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.settings_fragment, container, false)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setUSerDetailsRequestObserver()
        setUSerUpdateRequestObserver()
        setUploadDocObserver()
        postUSerDetailsViewModel.loadData()
        listners()
    }

    fun listners() {
        profile_icon.setOnClickListener {
            pickImage()
        }
        btnNExt.setOnClickListener {
            Helper.hideSoftKeyboard(activity!!)
            if(validate() && validateVehicleForm()) {
                prepareParams()
                postUSerUpdateViewModel.loadData(state.ApiSignupForm(),postUSerDetailsViewModel.obj?.vehicle!!,jsonArray)
            }
        }
        llhomeaddress.setOnClickListener {
            startActivityForResult(Intent(activity, SearchActivity::class.java),REQUEST_CODE_AUTOCOMPLETE);

        }
        llofficeAddress.setOnClickListener {
            startActivityForResult(Intent(activity, SearchActivity::class.java),REQUEST_CODE_AUTOCOMPLETE_OFFICE);
        }
        rl_add_other_vehicle.setOnClickListener {
            try {
                if(validateVehicleForm()) {
                    val vehicle = postUSerDetailsViewModel.obj?.vehicle as ArrayList
                    vehicle.add(Vehicle("","", "", "",
                        "", true,-1))
                    vehiclesAdapter.obj = vehicle
                    vehiclesAdapter.notifyDataSetChanged()
                } else {
                    showNotifyDialog(
                        "", "Please fill and save all vehicle details in the form",
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
        male.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                female.isChecked = false
            }
        }
        female.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                male.isChecked = false
            }
        }
    }
    fun validate() :Boolean {
        if(BaseHelper.isEmpty(name.text.toString())) {
            name.setError("Enter your Name")
            name.requestFocus()
            return false
        } else if(BaseHelper.isEmpty(emailID.text.toString())) {
            emailID.setError("Enter your Name")
            emailID.requestFocus()
            return false
        } else if(BaseHelper.isEmpty(mobileNumber.text.toString())) {
            mobileNumber.setError("Enter your Name")
            mobileNumber.requestFocus()
            return false
        } else if(BaseHelper.isEmpty(home_address.text.toString())) {
            home_address.setError("Enter your Name")
            home_address.requestFocus()
            return false
        } else{
            return true
        }
    }

    fun saveVehicleDetails() {

        for (i in 0 until postUSerDetailsViewModel.obj?.vehicle?.size!!) {

            val view = vehicle_details.getChildAt(i);
            var registrationTxt  = view.registration
            var name  = view.name
            var brandTxt  = view.brand
            var modelTxt  = view.model
            var yellowboard  = view.yellowboard
            var whiteboard  = view.whiteboard
            postUSerDetailsViewModel.obj?.vehicle?.get(i)?.vehicle_model_type = modelTxt.text.toString()
            postUSerDetailsViewModel.obj?.vehicle?.get(i)?.vehicle_model_type = modelTxt.text.toString()
            postUSerDetailsViewModel.obj?.vehicle?.get(i)?.vehicle_brand = brandTxt.text.toString()
            postUSerDetailsViewModel.obj?.vehicle?.get(i)?.vehicle_no = registrationTxt.text.toString()
            postUSerDetailsViewModel.obj?.vehicle?.get(i)?.vehicle_name = name.text.toString()
            if(yellowboard.isChecked) {
                postUSerDetailsViewModel.obj?.vehicle?.get(i)?.vehicle_type = 1
            } else {
                postUSerDetailsViewModel.obj?.vehicle?.get(i)?.vehicle_type = 2
            }
        }
    }

    fun validateVehicleForm() : Boolean {
        saveVehicleDetails()
        for (vehicle in postUSerDetailsViewModel.obj?.vehicle!!) {
            if(BaseHelper.isEmpty(vehicle.vehicle_name)) {
                return false
            } else if(BaseHelper.isEmpty(vehicle.vehicle_no)) {
                return false
            } else if(BaseHelper.isEmpty(vehicle.vehicle_brand)) {
                return false
            } else if(BaseHelper.isEmpty(vehicle.vehicle_model_type)) {
                return false
            }
        }
        return true
    }

    fun prepareParams() {
        State.first_name = name.text.toString()
        State.last_name = lastname.text.toString()
        State.email = emailID.text.toString()
        State.office_email = work_email.text.toString()
        State.mobile = mobileNumber.text.toString()
        State.id = UserInfoManager.getInstance(activity!!).getAccountId()
        if(male.isChecked) {
            State.gender = "male"
        } else {
            State.gender = "female"
        }
        State.address_line1 = home_address.text.toString()
        State.formatted_address = home_address.text.toString()
        State.office_address_line1 = office_address.text.toString()
        State.office_formatted_address = office_address.text.toString()
        jsonArray.put(0,state.Address(activity!!))
        if(!state.OfficeAddress(activity!!).isNull("lattitude") ||
            !state.OfficeAddress(activity!!).isNull("longitude")) {
            jsonArray.put(1, state.OfficeAddress(activity!!))
        }
        //postLoginViewModel.loadData(state.LoginForm(State.mobile))
    }

    fun initUSerData() {
        val UserDetails = postUSerDetailsViewModel.obj?.personal_details
        Helper.loadImage(activity!!,UserInfoManager.getInstance(activity!!).getProfilePic(),profile_icon,R.drawable.user_default)

        try {

            if(UserDetails != null) {
                name.setText(UserDetails.first_name)
                //gender.setText(UserDetails.first_name)
                mobileNumber.setText(UserDetails.mobile)
                emailID.setText(UserDetails.email)
                work_email.setText(UserDetails.office_email)
                driving_ls.setText(UserDetails.dl_number)
                lastname.setText(UserDetails.last_name)
                if(UserDetails.gender.equals("male",ignoreCase = true)) {
                    male.isChecked = true
                    female.isChecked = false
                } else {
                    female.isChecked = true
                    male.isChecked = false
                }
            }
        }catch (e:Exception){
            e.printStackTrace()

        }
        try {
            val address = postUSerDetailsViewModel.obj?.address

            if(address != null) {
                home_address.setText(address?.home?.formatted_address)
                if(address?.office?.formatted_address != null) {
                    office_address.setText(address?.office?.formatted_address)

                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
        try {
            val vehicle = postUSerDetailsViewModel.obj?.vehicle
            if(vehicle != null) {
                vehicle_details.layoutManager = LinearLayoutManager(activity)
                vehicle_details.isNestedScrollingEnabled = false
                vehiclesAdapter = VehiclesAdapter(activity!!)
                vehiclesAdapter.obj = vehicle as ArrayList<Vehicle>
                vehicle_details.adapter = vehiclesAdapter
            }
        } catch (e : Exception) {
            e.printStackTrace()

        }

    }
    fun pickImage() {
        cameraOutputUri = activity!!.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        val intent: Intent = BaseHelper.getPickIntent(cameraOutputUri,activity!!)
        startActivityForResult(intent, 0)
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
            office_address.setText(data?.getStringExtra("Address"))
        } else {
            try {
                var imageuri: Uri? = null;
                if(data != null) {
                    imageuri = data?.getData();// Get intent
                } else {
                    imageuri = cameraOutputUri
                }
                val real_Path = BaseHelper.getRealPathFromUri(activity, imageuri);
                postUploadDocViewModel.loadData(PostUploadDocViewModel.PROFILE_PHOTO, real_Path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setUploadDocObserver() {
        postUploadDocViewModel = ViewModelProviders.of(this).get(PostUploadDocViewModel::class.java).apply {
            this@SettingsFragment.let { thisFragReference ->
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
                            Helper.loadImage(activity!!,postUploadDocViewModel.obj?.original_path!!,profile_icon)
                            UserInfoManager.getInstance(activity!!).saveProfilePic(
                                postUploadDocViewModel.obj?.original_path!!)
                        }
                    }
                })
            }
        }
    }

    fun setUSerDetailsRequestObserver() {
        postUSerDetailsViewModel = ViewModelProviders.of(this).get(
            PostUSerDetailsViewModel::class.java).apply {
            this@SettingsFragment.let { thisFragReference ->
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
                        PostUSerDetailsViewModel.NEXT_STEP -> {
                            initUSerData()
                        }
                    }
                })
            }
        }
    }

    fun setUSerUpdateRequestObserver() {
        postUSerUpdateViewModel = ViewModelProviders.of(this).get(
            PostUSerUpdateViewModel::class.java).apply {
            this@SettingsFragment.let { thisFragReference ->
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
                        PostUSerDetailsViewModel.NEXT_STEP -> {
                           postUSerDetailsViewModel.loadData()
                        }
                    }
                })
            }
        }
    }
    
    class State {
        companion object {
            var id = ""
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
            var upload_type = PostUploadDocViewModel.PROFILE_PHOTO
        }


        fun Address(context : Context) :JSONObject {
            val obj = JSONObject()

            if(!BaseHelper.isEmpty(address_line1))
                obj.put("address_line1", address_line1)
            obj.put("lattitude", lattitude.toString())
            obj.put("longitude", longitude.toString())
            obj.put("type", "home")
            obj.put("id", id)

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
                obj.put("id", id)
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
            else
                obj.put("office_email", "")
            if(!BaseHelper.isEmpty(mobile))
                obj.put("mobile", mobile)

            if(!BaseHelper.isEmpty(role_id))
                obj.put("role_id", role_id)

            obj.put("referel_code", referel_code)

            if(!BaseHelper.isEmpty(dl_number))
                obj.put("dl_number", dl_number)
            else
                obj.put("dl_number", "")

            return obj
        }

    }

}
