package com.memu.ui.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.onboarding_four.*
import kotlinx.android.synthetic.main.onboarding_one.*
import kotlinx.android.synthetic.main.onboarding_three.*
import kotlinx.android.synthetic.main.onboarding_two.*
import org.json.JSONArray
import org.json.JSONObject


class RegisterFragment : BaseFragment() , View.OnClickListener {

    lateinit var getVehicleTypeViewModel: GetVehicleTypeViewModel
    lateinit var postUserSignupViewModel: PostUserSignupViewModel
    val jsonArray = JSONArray()
    val state = State()

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
        cab_vehicle_btn.setOnClickListener(this)

    }
    fun validateForm() : Boolean {
        if(BaseHelper.isEmpty(State.first_name))
            return false

        if(BaseHelper.isEmpty(State.last_name))
            return false

        if(BaseHelper.isEmpty(State.gender))
            return false

        if(BaseHelper.isEmpty(State.email))
            return false

        if(BaseHelper.isEmpty(State.office_email))
            return false

        if(BaseHelper.isEmpty(State.office_email))
            return false

        if(BaseHelper.isEmpty(State.role_type))
            return false

        if(BaseHelper.isEmpty(State.referel_code))
            return false

        return true
    }
    override fun onClick(v: View?) {
        if(destination != null) {
            destination!!.removeView(textViewNew)
        }
        when (v?.id)
        {
            R.id.no_vehicle_btn ->{

                white_car.visibility = View.VISIBLE
                yellow_car.visibility = View.GONE
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_3.getY().toInt()).setDuration(2000).start();
                destination = onbording_3
                startAnimation(white_car,R.drawable.white_car,300 )
            }
            R.id.private_vehicle_btn ->{
                white_car.visibility = View.VISIBLE
                yellow_car.visibility = View.GONE
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_5.getY().toInt()).setDuration(2000).start();
                destination = onbording_5

                startAnimation(white_car,R.drawable.white_car,300 )
            }
            R.id.cab_vehicle_btn ->{
                white_car.visibility = View.GONE
                yellow_car.visibility = View.VISIBLE
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                startAnimation(yellow_car,R.drawable.yellow_car,700 )
            }
        }
    }

    var destination: RelativeLayout? = null
    var textViewNew:ImageView? = null

    private fun startAnimation(textView:ImageView,drawable : Int,top_margin : Int) {
        textView.visibility = View.GONE
        val origin = textView?.getParent() as RelativeLayout
        textViewNew = ImageView(activity)
        // Create another TextView and initialise it to match textView
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(Helper.dpToPx(activity!!,75),Helper.dpToPx(activity!!,200),0,0)
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
            val first_name = ""
            val last_name = ""
            val gender = ""
            val email = ""
            val office_email = ""
            val mobile = ""
            val role_type = ""
            val referel_code = ""
            val vehicle_type = ""
            val vehicle_brand = ""
            val vehicle_name = ""
            val vehicle_no = ""
            val type = ""
            val address_line1 = ""
            val lattitude = ""
            val longitude = ""
            val formatted_address = ""
            val otp_code = ""

        }

        fun OtpForm() :JSONObject {
            val obj = JSONObject()
            if(!BaseHelper.isEmpty(otp_code))
                obj.put("otp_code", otp_code)
            return obj
        }

        fun Address() :JSONObject {
            val obj = JSONObject()
            if(!BaseHelper.isEmpty(type))
                obj.put("type", type)

            if(!BaseHelper.isEmpty(address_line1))
                obj.put("address_line1", address_line1)

            if(!BaseHelper.isEmpty(lattitude))
                obj.put("lattitude", lattitude)

            if(!BaseHelper.isEmpty(longitude))
                obj.put("longitude", longitude)

            if(!BaseHelper.isEmpty(formatted_address))
                obj.put("formatted_address", formatted_address)

            return obj
        }

        fun Vehicle() : JSONObject {
            val obj = JSONObject()
            if(!BaseHelper.isEmpty(vehicle_type))
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



