package com.memu.etc

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide

import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.HTTPAsyncTask
import com.iapps.libs.objects.Response
import com.memu.ActivityMain
import com.memu.R
import java.io.*
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

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

    fun getVersionCode(context: Context) : String {
        val manager = context?.packageManager
        val info = manager?.getPackageInfo(
            context?.packageName, 0)
        val versionName = info?.versionName
        return versionName!!
    }
    companion object {
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
        fun hideSoftKeyboard(activity: Activity) {
            try {
                val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            } catch (e: Exception) {
            }

        }
    }

}