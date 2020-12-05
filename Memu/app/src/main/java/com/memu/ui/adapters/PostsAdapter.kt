package com.memu.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
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
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.memu.R
import com.memu.etc.Helper
import com.memu.etc.UserInfoManager
import com.memu.ui.fragments.HomeFragment
import io.reactivex.annotations.NonNull
import kotlinx.android.synthetic.main.post_wall_item.view.*
import retrofit2.Callback

import java.lang.Exception
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.memu.modules.profileWall.Activity
import retrofit2.Call
import retrofit2.Response
import java.util.*
import kotlin.Comparator


class PostsAdapter(val context: Context) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {
    private var locationComponent: LocationComponent? = null

    fun addMarkers(
        mapboxMap: MapboxMap,
        srcLatitude: Double,
        srcLongitude: Double
    ) {
        val symbolLayerIconFeatureList = ArrayList<Feature>()
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(
                    srcLongitude,
                    srcLatitude))
        )
        val drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.map_marker, null);
        val mBitmap = com.mapbox.mapboxsdk.utils.BitmapUtils.getBitmapFromDrawable(drawable);
        mapboxMap?.setStyle(
            Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

                // Add the SymbolLayer icon image to the map style
                .withImage(
                    ICON_ID, mBitmap!!)


                // Adding a GeoJson source for the SymbolLayer icons.
                .withSource(
                    GeoJsonSource(
                        HomeFragment.SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)
                    )
                )

                .withLayer(
                    SymbolLayer(
                        HomeFragment.LAYER_ID,
                        HomeFragment.SOURCE_ID
                    )
                        .withProperties(
                            PropertyFactory.iconImage(ICON_ID),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true),
                            PropertyFactory.iconOffset(arrayOf(0f, -9f))
                        )
                )

        ) {
            enableLocationComponent(it,mapboxMap)
        }
        val position = CameraPosition.Builder()
            .target(LatLng(srcLatitude, srcLongitude))
            .zoom(10.0)
            .tilt(20.0)
            .build();
        mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
    }
    fun addDestinationIconSymbolLayer(@io.reactivex.annotations.NonNull loadedMapStyle: Style) {
        val drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.map_marker, null);
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

    private fun enableLocationComponent(
        @NonNull loadedMapStyle: Style?,
        mapboxMap: MapboxMap
    ) {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            locationComponent = mapboxMap!!.locationComponent
            locationComponent!!.activateLocationComponent(context!!, loadedMapStyle!!)
            locationComponent!!.isLocationComponentEnabled = true
            // Set the component's camera mode
            locationComponent!!.cameraMode = CameraMode.TRACKING
            locationComponent!!.zoomWhileTracking(12.0);
        }
    }


    fun findRoute(
        origin: Point,
        destination: Point,
        navigationMapRoute: NavigationMapRoute,
        mapboxMap: MapboxMap
    ) {
        NavigationRoute.builder(context!!)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(origin)
            .destination(destination)
            .alternatives(true)
            .build()
            .getRoute(object:Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                try {
                    if (response.isSuccessful
                        && response.body() != null
                        && !response.body()!!.routes().isEmpty()) {
                        val routes = response.body()!!.routes()
                        Collections.sort(routes,object  : Comparator<DirectionsRoute>{
                            override fun compare(o1: DirectionsRoute?, o2: DirectionsRoute?): Int {
                                return o1?.duration()?.compareTo(o2?.duration()!!)!!; // To compare string values
                            }
                        })
                        navigationMapRoute?.addRoutes(routes)
                        val position = CameraPosition.Builder()
                            .target(com.mapbox.mapboxsdk.geometry.LatLng(origin.latitude(), origin.longitude()))
                            .zoom(9.0)
                            .tilt(20.0)
                            .build();
                        mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);

                    }

                } catch (e : Exception){

                }

            }
            override fun onFailure(call:Call<DirectionsResponse>, throwable:Throwable) {

            }
        })
    }
    var productAdapterListener : ProductAdapterListener? = null
    var obj : List<Activity> =  listOf()
    interface ProductAdapterListener {
        fun onClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_wall_item, parent, false))
    }

    override fun getItemCount(): Int {
        return obj.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.pos = position
        holder.name.setText(obj.get(position).user_info.name)
        holder.description.setText(obj.get(holder.pos).message)
        try {
            Helper.loadImage(
                context!!,
                obj.get(position).user_info.photo.original_path,
                holder.profile_pic,
                R.drawable.default_profile_icon)
        }catch (e : Exception){

        }
        try {
            Helper.loadImage(context!!,obj.get(position).logo,holder.post_img_icon,0)
        } catch (e : java.lang.Exception){

        }
        try {
            Helper.loadImage(context!!,obj.get(position).image.profile_path,holder.post_img,0)
        } catch (e : java.lang.Exception){

        }
        try {
            val inflater =
                context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
            val myView = inflater.inflate(R.layout.map_view, null) as LinearLayout
            holder.home_mapView.addView(myView);
            val mapView = myView.findViewById<com.mapbox.mapboxsdk.maps.MapView>(R.id.mapView)
            mapView!!.getMapAsync(OnMapReadyCallback {
                it.setStyle(context.getString(R.string.navigation_guidance_day)) { style ->
                    enableLocationComponent(style, it)
                    addDestinationIconSymbolLayer(style)
                    val navigationMapRoute = NavigationMapRoute(null, mapView, it)

                    var latitude = 0.0
                    var longitude = 0.0

                    if(!BaseHelper.isEmpty(obj.get(position).address.lattitude )) {
                        latitude = obj.get(position).address.lattitude.toDouble()
                        longitude = obj.get(position).address.longitude.toDouble()
                        addMarkers(it, latitude, longitude)

                    }
                    if(!BaseHelper.isEmpty(obj.get(position).from_address.lattitude )) {
                        val maporiginPoint = Point.fromLngLat(
                            obj.get(position).from_address.longitude.toDouble(),
                            obj.get(position).from_address.lattitude.toDouble()
                        )
                        val destinationPoint = Point.fromLngLat(
                            obj.get(position).to_address.longitude.toDouble(),
                            obj.get(position).to_address.lattitude.toDouble()
                        )


                        findRoute(maporiginPoint!!, destinationPoint!!,navigationMapRoute,it)
                    }


                }
            })
        } catch (e : Exception) {

        }

    }


    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        var pos : Int = 0
        var name = view.name
        var profile_pic = view.profile_pic
        var description = view.description
        var post_img = view.post_img
        var post_img_icon = view.post_img_icon
        var home_mapView = view.home_mapView

    }

    companion object {
        private val ICON_ID = "ICON_ID"

    }

}