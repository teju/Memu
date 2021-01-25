package com.memu.etc

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.iapps.libs.helpers.BaseHelper
import com.iapps.libs.helpers.HTTPAsyncTask
import com.iapps.libs.objects.Response
import com.memu.R
import org.json.JSONObject
import java.io.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


open class Helper  {
    open class GenericHttpAsyncTask(internal var taskListener: TaskListener?) : HTTPAsyncTask() {

        lateinit var nonce: String

        interface TaskListener {
            fun onPreExecute()
            fun onPostExecute(response: Response?)
        }

        override fun onPreExecute() {
            if (taskListener != null) taskListener!!.onPreExecute()
        }

        override fun onPostExecute(@Nullable response: Response?) {

            if (response == null) {

                var apiDetails = ""

                try {
                    apiDetails = apiDetails + this.url.path + "\n"
                } catch (e: Exception) {
                    logException(null, e)
                }

                try {
                    apiDetails = apiDetails + this.params.toString() + "\n"
                } catch (e: Exception) {
                    logException(null, e)
                }

                var rawResponse = ""
                try {
                    rawResponse = rawResponse + this.rawResponseString
                } catch (e: Exception) {
                    logException(null, e)
                }

            }

            if (taskListener != null) taskListener!!.onPostExecute(response)
        }

    }

    fun saveImage(result: Bitmap, s: String) {
        val sd = File(Environment.getExternalStorageDirectory().toString() + "/iApps");
        if(!sd.exists()) {
            sd.mkdir()
        }
        val dest = File(sd, s);
        try {
            val out = FileOutputStream(dest);
            result.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (e : java.lang.Exception) {
            e.printStackTrace();
        }

    }

    companion object {
        fun applyHeader(context : Context, async: HTTPAsyncTask?) {
            if (async == null)
                return

            async.setCache(false)
            async.setHeader(Keys.ContentType, "application/json")

            UserInfoManager.getInstance(context).authToken?.let {
                async.setHeader(Keys.Authorization, "Bearer "+it)
            }
        }

        fun logException(ctx: Context?, e: Exception?) {
            try {
                if (Constants.IS_DEBUGGING) {
                    if (Constants.IS_DEBUGGING) {
                        if (ctx != null)
                            Log.v(ctx.getString(R.string.app_name), getStackTrace(e!!))
                        else
                            print(getStackTrace(e!!))
                    }
                }
            } catch (e1: Exception) {
            }
        }

        fun getStackTrace(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw, true)
            throwable.printStackTrace(pw)
            return sw.buffer.toString()
        }
        fun dpToPx(context: Context, dp: Int): Int {
            val displayMetrics = context.resources.displayMetrics
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
        }

        fun toDp(context: Context, dp: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
                    .getDisplayMetrics()
            ).toInt()
        }

        fun hideSoftKeyboard(activity: Activity) {
            try {
                val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            } catch (e: Exception) {
            }

        }
        fun isValidEmail(target: CharSequence): Boolean {
            return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }

        fun isValidMobile(phone: String): Boolean {
            return if (!Pattern.matches("[a-zA-Z]+", phone)) {
                phone.length == 10
            } else false
        }
        fun loadImage(context: Context, url : String, v : ImageView){
            Glide.with(context)
                .load(url)
                .into(v)
        }
        fun parseDate(dateString : String, dateFormat : String ) : Date{
        val simpleDateFormat = SimpleDateFormat(dateFormat);
        var date = Date();
        try {
            date = simpleDateFormat.parse(dateString);
        } catch ( e : ParseException) {
            e.printStackTrace();
        }

        return date;
    }
        fun FormatDistance(meters:Double, unitString:String):String {
            val df = DecimalFormat("#.##");
            return ""+df.format(meters/1000 )+unitString
        }
        fun FormatDistance(meters:Double):Double {
            val df = DecimalFormat("#.##");
            return df.format(meters/1000 ).toDouble()
        }
        fun splitToComponentTimes(biggy: BigDecimal):String {
            val longVal = biggy.toLong()
            val hours = longVal.toInt() / 3600
            var remainder = longVal.toInt() - hours * 3600
            val mins = remainder / 60
            remainder = remainder - mins * 60
            val secs = remainder
            val ints = intArrayOf(hours, mins, secs)
            var duration = ""
            if(ints.get(0) > 0) {
                duration = ""+ints.get(0)+"hr"
            }
            if(ints.get(1) > 0) {
                duration = duration+" "+ints.get(1)+"min"
            }

            return duration
        }

        fun loadImage(context: Context, url : String, v : ImageView,placeholder : Int){
            Glide.with(context)
                .load(url)
                .centerInside()
                .placeholder(placeholder)
                .into(v)
        }
        fun isNetworkAvailable(ctx: Context): Boolean {
            try {
                val manager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = manager.activeNetworkInfo

                var isAvailable = false
                if (networkInfo != null && networkInfo.isConnected) {
                    isAvailable = true
                }
                if (!isAvailable) {
                    logException(null, null)
                }
                return isAvailable
            } catch (e: Exception) {
                return true
            }

        }

    }

    fun getAddress(context : Context): List<Address>? {
        val gpsTracker = GPSTracker(context)
        var addresses: List<Address>? = null
        if(gpsTracker.canGetLocation) {
            val geocoder: Geocoder
            geocoder = Geocoder(context, Locale.getDefault())

            try {
                addresses = geocoder.getFromLocation(gpsTracker?.latitude!!, gpsTracker?.longitude!!, 1)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


        return addresses

    }

    fun Location(context : Context) : JSONObject {
        val obj = JSONObject()

        try {
            val gpsTracker = GPSTracker(context!!)

            obj.put("country", getAddress(context)?.get(0)?.countryName)

            obj.put("state", getAddress(context)?.get(0)?.getAdminArea())

            obj.put("city", getAddress(context)?.get(0)?.locality)
            if(BaseHelper.isEmpty( getAddress(context)?.get(0)?.subLocality)) {
                obj.put("location", getAddress(context)?.get(0)?.locality)
            } else{
                obj.put("location", getAddress(context)?.get(0)?.subLocality)
            }
            obj.put("pincode", getAddress(context)?.get(0)?.postalCode)
            obj.put("lattitude", gpsTracker.latitude.toString())
            obj.put("longitude", gpsTracker.longitude.toString())
            obj.put("formatted_address", getAddress(context)?.get(0)?.getAddressLine(0))
            obj.put("address_line1", getAddress(context)?.get(0)?.getAddressLine(0))


        } catch (e : java.lang.Exception){

        }
        return obj
    }
}