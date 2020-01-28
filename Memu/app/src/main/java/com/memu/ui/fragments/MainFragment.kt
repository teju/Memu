package com.memu.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import kotlinx.android.synthetic.main.main_fragment.*
import com.facebook.login.LoginResult
import com.facebook.Profile.getCurrentProfile
import com.facebook.internal.ImageRequest.getProfilePictureUri
import com.squareup.picasso.Picasso
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginManager
import kotlinx.android.synthetic.main.main_fragment.ld
import kotlinx.android.synthetic.main.register_fragment.*
import java.util.*
import org.json.JSONException
import org.json.JSONObject
import com.facebook.appevents.codeless.internal.ViewHierarchy.setOnClickListener
import com.facebook.AccessToken




class MainFragment : BaseFragment() , View.OnClickListener {

    var callbackManager: CallbackManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.main_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        FacebookSdk.sdkInitialize(activity)
        sign_up.setOnClickListener(this)
        login.setOnClickListener(this)
        fblogin.setOnClickListener(this)
        btnfblogin.setOnClickListener(this)
        callbackManager = CallbackManager.Factory.create()
        fblogin.setFragment(this);

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_up -> {
                home().setFragment(RegisterFragment())
            }
            R.id.login -> {
                home().setFragment(LoginFragment())
            }
            R.id.fblogin -> {
                //ld.showLoadingV2()
                fbLogin()
            }
            R.id.btnfblogin -> {
                ld.showLoadingV2()
                fbLogin()
                fblogin.performClick()
            }
        }
    }

    fun fbLogin() {
        /*val loggedOut = AccessToken.getCurrentAccessToken() == null
        if (!loggedOut) {

            getUserProfile(AccessToken.getCurrentAccessToken())
        }
*/
        if (AccessToken.getCurrentAccessToken() != null && com.facebook.Profile.getCurrentProfile() != null) {
            //Logged in so show the login button
            LoginManager.getInstance().logOut()
            getUserProfile(AccessToken.getCurrentAccessToken())

        }
        fblogin.setReadPermissions(Arrays.asList("email", "public_profile"))

        fblogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                System.out.println("facebook123 onSuccess " )

                // App code
                //loginResult.getAccessToken();
                //loginResult.getRecentlyDeniedPermissions()
                //loginResult.getRecentlyGrantedPermissions()
                //Log.d("API123", "$loggedIn ??")

                getUserProfile(AccessToken.getCurrentAccessToken())

            }

            override fun onCancel() {
                System.out.println("facebook123 onCancel " )

                ld.hide()
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
                System.out.println("facebook123 onError " + exception.toString())
                ld.hide()
            }
        })

    }

    private fun getUserProfile(currentAccessToken: AccessToken) {
        ld.hide()
        val request = GraphRequest.newMeRequest(
            currentAccessToken
        ) { `object`, response ->
            Log.d("TAG", `object`.toString())
            try {

                val first_name = `object`.getString("first_name")
                val last_name = `object`.getString("last_name")
                val email = `object`.getString("email")
                val id = `object`.getString("id")
                val image_url = "https://graph.facebook.com/$id/picture?type=normal"

                System.out.println("facebook123 getUserProfile " + `object`.toString())
                home().setFragment(RegisterFragment().apply {
                    this.fbObj = `object`
                })
            } catch (e: Exception) {
                e.printStackTrace()
                System.out.println("facebook123 getUserProfile JSONException " + `e`.toString())

            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "first_name,last_name,email,id,gender")
        request.parameters = parameters
        request.executeAsync()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        System.out.println("facebook123 onActivityResult " )

        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data);

    }


}
