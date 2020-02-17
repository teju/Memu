package com.memu.etc

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Nullable

import com.iapps.libs.helpers.HTTPAsyncTask
import com.iapps.libs.objects.Response
import com.memu.R
import java.io.*
import android.util.Patterns
import android.text.TextUtils

import java.util.regex.Pattern
import android.util.TypedValue
import android.widget.ImageView
import com.bumptech.glide.Glide


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

}