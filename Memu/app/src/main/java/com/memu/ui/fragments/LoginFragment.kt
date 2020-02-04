package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.memu.R
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager
import com.memu.ui.BaseFragment
import com.memu.webservices.PostLoginViewModel
import com.memu.webservices.PostOtpViewModel
import kotlinx.android.synthetic.main.login_fragment.*

import org.json.JSONObject

class LoginFragment : BaseFragment() , View.OnClickListener {

    lateinit var postLoginViewModel: PostLoginViewModel
    lateinit var postOtpViewModel: PostOtpViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.login_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setLoginAPIObserver()
        setOtpAPIObserver()
        login.text = "Get OTP"
        get_otp.visibility = View.GONE
        otp_number.visibility = View.GONE
        get_otp.setOnClickListener {
            if(validateMobileNumber()) {
                postLoginViewModel.loadData(LoginForm(mobileNo.text.toString()))
            }
        }
        mobileNo.setOnEditorActionListener { v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    if(validateMobileNumber()) {
                        postLoginViewModel.loadData(LoginForm(mobileNo.text.toString()))
                    }

                    true
                } else {
                    false
                }
            }
        login.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.login -> {
                if(otp_number.isVisible) {
                    if (validateMobileNumber() && validateOTp()) {
                        val jsonObject = JSONObject()
                        jsonObject.put("otp_code", otp_number.text.toString())
                        postOtpViewModel.loadData(LoginForm(mobileNo.text.toString()), jsonObject)
                    }
                } else {
                    if(validateMobileNumber()) {
                        postLoginViewModel.loadData(LoginForm(mobileNo.text.toString()))
                    }
                }
            }
        }
    }

    fun validateMobileNumber() :Boolean{
        if(BaseHelper.isEmpty(mobileNo.text.toString()) || !Helper.isValidMobile(
                mobileNo.text.toString())) {
            er_mtv3.visibility = View.VISIBLE
            mobileNo.requestFocus()
            er_mtv3.text = "Enter valid mobile number"
            return false
        } else {
            mobileNo.clearFocus()
            er_mtv3.visibility = View.GONE

        }

        return true
    }

    fun validateOTp() :Boolean {
        if(BaseHelper.isEmpty(otp_number.text.toString())) {
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

    fun setOtpAPIObserver() {
        postOtpViewModel = ViewModelProviders.of(this).get(PostOtpViewModel::class.java).apply {
            this@LoginFragment.let { thisFragReference ->
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
                            home().setFragment(DummyFragment())
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

    fun setLoginAPIObserver() {
        postLoginViewModel = ViewModelProviders.of(this).get(PostLoginViewModel::class.java).apply {
            this@LoginFragment.let { thisFragReference ->
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
                            login.text = "Sign-in"
                            get_otp.visibility = View.VISIBLE
                            otp_number.visibility = View.VISIBLE
                            showNotifyDialog(
                                "", postLoginViewModel.obj?.message,
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

    fun LoginForm(username : String) : JSONObject {
        val obj = JSONObject()
        obj.put("username", username)
        return obj
    }

}
