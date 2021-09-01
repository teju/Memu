package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import com.facebook.AccessToken
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.facebook.CallbackManager
import com.facebook.Profile.getCurrentProfile
import com.facebook.internal.ImageRequest.getProfilePictureUri
import com.squareup.picasso.Picasso
import android.util.Log
import android.widget.Button
import java.util.*
import org.json.JSONException
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.github.barteksc.pdfviewer.PDFView
import com.memu.etc.UserInfoManager


class TermsConditionsFragment : BaseFragment()  {
    var isLogin = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.terms_conditions_fragment, container, false)
        return v
    }

    override fun onBackTriggered() {
        home().exitApp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        val pdfView = v?.findViewById<PDFView>(R.id.pdfv);
        val terms_conditions = v?.findViewById<Button>(R.id.terms_conditions);
        pdfView!!.fromAsset("terms_conditions.pdf")
            .spacing(0) // in dp
            .load();
        terms_conditions?.setOnClickListener {
            if(isLogin) {
                home().setFragment(HomeFragment())
            } else {
                home().setFragment(ProfilePicUploadFragment())
            }
            UserInfoManager.getInstance(activity!!).saveFirstTime(false)
        }
    }


}
