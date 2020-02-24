package com.memu.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.memu.R
import com.memu.ui.BaseFragment
import com.memu.webservices.PostUploadDocViewModel
import kotlinx.android.synthetic.main.profile_pic_upload_fragment.*
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.ImageViewTarget

import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager


class ProfilePicUploadFragment : BaseFragment()  {
    lateinit var postUploadDocViewModel: PostUploadDocViewModel

    val PICK_PHOTO_PHOTO = 10009
    var uploadSuccess = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.profile_pic_upload_fragment, container, false)
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
        setUploadDocObserver()
        llUpload.visibility = View.VISIBLE
        Glide.with(this)
            .load(R.drawable.onboarding_account)
            .into(creating_profile_gif);
        btnNExt.setOnClickListener {
            pickImage()
        }
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_PHOTO);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            try {

                val imageuri = data?.getData();// Get intent
                // Get real path and show over text view
                val real_Path = BaseHelper.getRealPathFromUri(activity, imageuri);
                val bitmap = MediaStore.Images.Media.getBitmap(activity?.getContentResolver(), imageuri);
              //  profilePic.setImageBitmap(bitmap);
                System.out.println("onActivityResult12 onActivityResult")

                postUploadDocViewModel.loadData(PostUploadDocViewModel.PROFILE_PHOTO, real_Path)
            } catch (e: Exception) {
                System.out.println("onActivityResult12 Exception "+e.toString())
            }
    }
    fun setUploadDocObserver() {
        postUploadDocViewModel = ViewModelProviders.of(this).get(PostUploadDocViewModel::class.java).apply {
            this@ProfilePicUploadFragment.let { thisFragReference ->
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
                            Helper.loadImage(activity!!,postUploadDocViewModel.obj?.original_path!!,profilePic)
                            Handler().postDelayed(
                                Runnable // Using handler with postDelayed called runnable run method

                                {

                                    UserInfoManager.getInstance(activity!!).saveProfilePic(
                                        postUploadDocViewModel.obj?.original_path!!)
                                    rlCreating_profile.visibility = View.VISIBLE
                                    llUpload.visibility = View.GONE

                                }, 1000)

                            Handler().postDelayed(
                                Runnable // Using handler with postDelayed called runnable run method

                                {

                                    home().setFragment(DummyFragment())

                                }, 6000)
                        }
                    }
                })

            }
        }
    }

}
