package com.memu.ui.fragments

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.memu.R
import com.memu.etc.SpacesItemDecoration
import com.memu.etc.UserInfoManager
import com.memu.modules.friendList.FriendList
import com.memu.modules.friendList.User
import com.memu.ui.BaseFragment
import com.memu.ui.adapters.FriendsAdapter
import com.memu.ui.adapters.PostsAdapter
import com.memu.webservices.GetUserWallViewModel
import com.memu.webservices.PostFriendListViewModel
import com.memu.webservices.PostFriendRequestViewModel
import com.memu.webservices.PostUploadDocViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.map_view.*
import kotlinx.android.synthetic.main.profile_header.*
import kotlinx.android.synthetic.main.profile_wall.*

class ProfileWallFragment : BaseFragment() ,View.OnClickListener,
    PostFriendListViewModel.FriendsSearchResListener , OnMapReadyCallback{

    private var cameraOutputUri: Uri? = null
    lateinit var getUserWallViewModel: GetUserWallViewModel
    lateinit var postUploadDocViewModel: PostUploadDocViewModel
    lateinit var postFriendRequestViewModel: PostFriendRequestViewModel
    val PICK_PHOTO_PHOTO = 10010
    var friend_id = ""
    var isPubLicWall = false
    private var myView: LinearLayout? = null
    private var mapboxMap: MapboxMap? = null
    private var locationComponent: LocationComponent? = null
    var uploadImageType = PostUploadDocViewModel.ACTIVITY_PHOTO
    var type = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.profile_wall, container, false)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    override fun onHiddenChanged(hidden: Boolean) {
        var type = "private"
        if(isPubLicWall) {
            type = "public"
        }
        getUserWallViewModel.loadData(friend_id,type)
        posUserMainDataViewModel.loadData(friend_id)
        postFriendListViewModel.loadData("FR", "", 0, this, friend_id)
        postFriendListViewModel.loadData("FR", "", 1, this, friend_id)
    }

    override fun onResume() {
        super.onResume()
        var type = "private"
        if(isPubLicWall) {
            type = "public"
        }
        getUserWallViewModel.loadData(friend_id,type)
        posUserMainDataViewModel.loadData(friend_id)
        postFriendListViewModel.loadData("FR","",0,this,friend_id)
        postFriendListViewModel.loadData("FR", "", 1, this, friend_id)
    }

    private fun initUI() {
        find_more.setOnClickListener(this)
        rlInvite.setOnClickListener(this)
        arrow_left.setOnClickListener(this)
        tvfollowers.setOnClickListener(this)
        tvFriends.setOnClickListener(this)
        upload_activity.setOnClickListener(this)
        setGetActivitiesObserver()
        setUploadActivityPhotoObserver()
        setUSerMAinDataAPIObserver()
        setFriendListAPIObserver()
        setFriendRequestObserver()

        val sglm2 = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_grid1)
        friens_rl.setLayoutManager(sglm2)
        friens_rl.setNestedScrollingEnabled(false)
        friens_rl.addItemDecoration(SpacesItemDecoration(3, spacingInPixels, true))

        if(BaseHelper.isEmpty(friend_id)) {
            friend_id = UserInfoManager.getInstance(activity!!).getAccountId()
        } else {
            privateWallSettings()
        }


        try {
            val  inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
            myView = inflater.inflate(R.layout.map_view, null) as LinearLayout
            frame_layout.addView(myView);
            mapView!!.getMapAsync(this)
        } catch (e : Exception){

        }
        profile_pic.setOnClickListener {
            uploadImageType = PostUploadDocViewModel.PROFILE_PHOTO
            if(!isPubLicWall) {
                pickImage()
            }
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        try {

            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(getString(R.string.navigation_guidance_day)) { style ->
                enableLocationComponent(style)
                addDestinationIconSymbolLayer(style)

            }
        } catch (e : Exception){

        }
    }

    override fun onResult(result: FriendList?, searchByLoc: Int) {
        if(result?.user_list?.size == 0){
            friens_rl.visibility = View.GONE
        } else{
            friens_rl.visibility = View.VISIBLE

        }
        if(searchByLoc == 0) {
            val adapter = FriendsAdapter(context!!)
            if(result?.user_list?.size != 0) {
                adapter.obj = result?.user_list as ArrayList<User>
            } else {
                adapter.obj = ArrayList()
            }
            friens_rl.adapter = adapter
            (friens_rl.adapter as FriendsAdapter).productAdapterListener =
                object : FriendsAdapter.ProductAdapterListener {
                    override fun onClick(position: Int) {
                        home().setFragment(ProfileWallFragment().apply {
                            friend_id = result?.user_list.get(position)?.freind_id!!
                            isPubLicWall = true
                        })
                    }
                }
        } else {
            addMarkers(result!!)
        }
    }
    private fun addDestinationIconSymbolLayer(@io.reactivex.annotations.NonNull loadedMapStyle: Style) {
        val drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.map_marker, null);
        val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
        loadedMapStyle.addImage(
            "destination-icon-id", mBitmap!!)
        val geoJsonSource = GeoJsonSource("destination-source-id")
        loadedMapStyle.addSource(geoJsonSource)
        val destinationSymbolLayer =
            SymbolLayer("destination-symbol-layer-id", "destination-source-id")
        destinationSymbolLayer.withProperties(
            PropertyFactory.iconImage("destination-icon-id"),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true)
        )
        loadedMapStyle.addLayer(destinationSymbolLayer)
    }

    fun privateWallSettings() {
        upload_activity.visibility = View.GONE
        frame_layout.visibility = View.GONE
        find_more.visibility = View.GONE
        rlInvite.visibility = View.GONE
        user_searchll.visibility = View.GONE
        tvFindMyFriends.visibility = View.GONE
        tv_recents_posts.text = "Recent Posts"
    }

    override fun setUserMainData() {
        super.setUserMainData()
        if(isPubLicWall) {
            followers_cnt_.visibility = View.GONE
            messages_cnt.visibility = View.GONE
            friends_cnt.visibility = View.GONE
            if(getUserWallViewModel.obj != null && getUserWallViewModel.obj?.is_freind!!) {
                tvFriends.text = "Friend"
            } else {
                tvFriends.text = "Add Friends"
            }
            if(getUserWallViewModel.obj != null && getUserWallViewModel.obj?.is_follower!!) {
                tvfollowers.text = "Following"
            } else {
                tvfollowers.text = "Follow"
            }
            if(getUserWallViewModel.obj != null && (getUserWallViewModel.obj?.is_freind!!
                        || getUserWallViewModel.obj?.is_follower!!)) {
                posts_rl.visibility = View.VISIBLE
                tv_recents_posts.visibility = View.VISIBLE
            } else {
                posts_rl.visibility = View.GONE
                tv_recents_posts.visibility = View.GONE
            }
            messages.text = "Message"
            rlFriends.setOnClickListener(this)
        } else {
            posts_rl.visibility = View.VISIBLE
            tv_recents_posts.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.find_more -> {
                home().setFragment(FriendsFragment())
            }
            R.id.rlInvite -> {
                referFriend()
            }
            R.id.arrow_left -> {
                home().onBackPressed()
            }
            R.id.tvfollowers -> {
                System.out.println("tvfollowers clicked "+isPubLicWall)
                if(isPubLicWall) {
                    type = "FL"
                    postFriendRequestViewModel.loadData("FL", friend_id)
                } else {
                    home().setFragment(FollowersRequestFragment())
                }

            }
            R.id.upload_activity -> {
                uploadImageType = PostUploadDocViewModel.ACTIVITY_PHOTO
                pickImage()
            }
            R.id.rlFriends -> {

            }
            R.id.tvFriends -> {
                if(isPubLicWall) {
                    if(getUserWallViewModel.obj != null && !getUserWallViewModel.obj?.is_freind!!) {
                        type = "FR"
                        postFriendRequestViewModel.loadData("FR", friend_id)
                    }
                } else {
                    home().setFragment(FollowersRequestFragment().apply {
                        isFriendsRequest = true

                    })
                }
            }
        }
    }

    fun setGetActivitiesObserver() {
        getUserWallViewModel = ViewModelProviders.of(this).get(GetUserWallViewModel::class.java).apply {
            this@ProfileWallFragment.let { thisFragReference ->
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
                getTrigger().observe(thisFragReference, Observer {
                    posts_rl.layoutManager = LinearLayoutManager(activity)
                    posts_rl.isNestedScrollingEnabled = false
                    val postsAdapter =  PostsAdapter(activity!!)
                    postsAdapter.obj = getUserWallViewModel.obj?.activities!!
                    posts_rl.adapter = postsAdapter

                })
            }
        }
    }

    fun pickImage() {
        cameraOutputUri = activity!!.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        val intent: Intent = BaseHelper.getPickIntent(cameraOutputUri,activity!!)
        startActivityForResult(intent, PICK_PHOTO_PHOTO)
    }
    private fun enableLocationComponent(@io.reactivex.annotations.NonNull loadedMapStyle: Style?) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(activity)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap!!.locationComponent
            locationComponent!!.activateLocationComponent(activity!!, loadedMapStyle!!)
            locationComponent!!.isLocationComponentEnabled = true
            // Set the component's camera mode
            locationComponent!!.cameraMode = CameraMode.TRACKING
            locationComponent!!.zoomWhileTracking(12.0);
        }
    }
    fun addMarkers(user_list : FriendList) {

        try {
            for (x in 0 until user_list.user_list.size!!) {
                val latLng = LatLng(
                    user_list.user_list.get(x).lattitude?.toDouble()!!,
                    user_list.user_list.get(x).longitude?.toDouble()!!)
                try {
                    Picasso.get().load(user_list.user_list.get(x).photo.original_path)
                        .into(object : com.squareup.picasso.Target {
                            override fun onBitmapLoaded(
                                bitmap: Bitmap?,
                                from: Picasso.LoadedFrom?
                            ) {
                                try {
                                    val bmp = BaseHelper.ScaleBitmap(bitmap, 150)
                                    // loaded bitmap is here (bitmap)
                                    val iconFactory = IconFactory.getInstance(activity!!);
                                    val icon = iconFactory.fromBitmap(bmp!!);

                                    mapboxMap!!.addMarker(
                                        MarkerOptions()
                                            .position(latLng)
                                            .icon(icon)
                                    )
                                } catch (e: Exception){

                                }
                            }


                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                            }

                            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                        })
                } catch (e: Exception){

                }

            }
        } catch (e : java.lang.Exception){

        }
        val position = CameraPosition.Builder()
            .target(LatLng( user_list.user_list.get(0).lattitude?.toDouble()!!,
                user_list.user_list.get(0).longitude?.toDouble()!!))
            .zoom(15.0)
            .tilt(20.0)
            .build();
        mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
             var imageuri:Uri? = null;
            if(data != null) {
                imageuri = data?.getData();// Get intent
            } else {
                imageuri = cameraOutputUri
            }
            val real_Path = BaseHelper.getRealPathFromUri(activity, imageuri);
            postUploadDocViewModel.loadData(uploadImageType, real_Path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUploadActivityPhotoObserver() {
        postUploadDocViewModel = ViewModelProviders.of(this).get(PostUploadDocViewModel::class.java).apply {
            this@ProfileWallFragment.let { thisFragReference ->
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
                            ld.hide()
                            if(uploadImageType == PostUploadDocViewModel.PROFILE_PHOTO) {
                                posUserMainDataViewModel.loadData(friend_id)
                                UserInfoManager.getInstance(activity!!).saveProfilePic(
                                    postUploadDocViewModel.obj?.original_path!!)
                            } else {
                                getUserWallViewModel.loadData(friend_id,"private")
                            }
                        }
                    }
                })
            }
        }
    }

    fun setFriendRequestObserver() {
        postFriendRequestViewModel = ViewModelProviders.of(this).get(PostFriendRequestViewModel::class.java).apply {
            this@ProfileWallFragment.let { thisFragReference ->
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
                        PostFriendRequestViewModel.NEXT_STEP -> {
                            var drawable = R.drawable.myfriends
                            if(type.equals("FL",ignoreCase = true)) {
                                drawable = R.drawable.followers_noti
                            }
                            showNotifyDialog(
                                "", postFriendRequestViewModel.obj?.message,
                                "Great","",object : NotifyListener {
                                    override fun onButtonClicked(which: Int) { }
                                },drawable,postFriendRequestViewModel.obj?.user_detail?.name!!,
                                postFriendRequestViewModel.obj?.user_detail?.photo?.profile_path!!)
                        }
                    }
                })

            }
        }
    }

}
