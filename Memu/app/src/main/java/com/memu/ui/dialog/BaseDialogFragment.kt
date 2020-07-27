package com.memu.ui.dialog

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.BuildConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.memu.ui.activity.ActivityMain
import com.memu.R
import com.memu.etc.UserInfoManager


open class BaseDialogFragment : DialogFragment() {

    var userInfo: UserInfoManager? = null
        private set

    var v: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.userInfo = UserInfoManager.getInstance(activity!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!BuildConfig.FLAVOR.equals("live", ignoreCase = true))
            Log.v("gon Dialog", this.javaClass.toString())

        v?.let {
            setBackButtonToolbarStyleOne(v!!)
        }

        dialog?.let {
            it.window.setBackgroundDrawableResource(R.color.transparent)
            it.window.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20))
        }


    }

    fun home(): ActivityMain {
        return activity as ActivityMain
    }

    fun setBackButtonToolbarStyleOne(v: View) {
        /*try {
            val llBack = v.findViewById<LinearLayout>(R.id.llBack)
            llBack.setOnClickListener { fragment_home().onBackPressed() }
        } catch (e: Exception) {
            Helper.logException(activity, e)
        }

//        try {
//            val ivBack = v.findViewById<ImageView>(R.id.ivBack)
//            ivBack.setOnClickListener { fragment_home().onBackPressed() }
//        } catch (e: Exception) {
//            Helper.logException(activity, e)
//        }

        try {
            val toolbar = v.findViewById<Toolbar>(R.id.toolbar)
            toolbar.navigationIcon = null
            toolbar.title = ""
            toolbar.hideOverflowMenu()

            toolbar.setNavigationOnClickListener { fragment_home().onBackPressed() }
        } catch (e: Exception) {
            Helper.logException(activity, e)
        }*/
    }


    fun setBackButtonToolbarStyleTwo(v: View, clickListener: View.OnClickListener?) {
      /*  try {
            val llBack = v.findViewById<LinearLayout>(R.id.llBack)
            llBack.setOnClickListener { view -> clickListener?.onClick(view) }
        } catch (e: Exception) {
            Helper.logException(activity, e)
        }

//        try {
//            val ivBack = v.findViewById<ImageView>(R.id.ivBack)
//            ivBack.setOnClickListener { view -> clickListener?.onClick(view) }
//        } catch (e: Exception) {
//           Helper.logException(activity, e)
//        }

        try {
            val toolbar = v.findViewById<Toolbar>(R.id.toolbar)
            toolbar.navigationIcon = null
            toolbar.title = ""
            toolbar.setNavigationOnClickListener { view -> clickListener?.onClick(view) }
        } catch (e: Exception) {
           Helper.logException(activity, e)
        }*/

    }

    open fun <T> AppCompatImageView.loadImage(
        model: T
    ) {
        Glide.with(context)
            .load(model)
            .into(this)
    }

    /**
     * Load model into ImageView as a circle image with borderSize (optional) using Glide
     *
     * @param model - Any object supported by Glide (Uri, File, Bitmap, String, resource id as Int, ByteArray, and Drawable)
     * @param borderSize - The border size in pixel
     * @param borderColor - The border color
     */
    open fun <T> AppCompatImageView.loadCircularImage(
        model: T,
        borderSize: Float = 0F,
        borderColor: Int = Color.WHITE
    ) {
        Glide.with(context)
            .asBitmap()
            .load(model)
            .apply(RequestOptions.circleCropTransform())
            .into(object : BitmapImageViewTarget(this) {
                override fun setResource(resource: Bitmap?) {
                    setImageDrawable(
                        resource?.run {
                            RoundedBitmapDrawableFactory.create(
                                resources,
                                if (borderSize > 0) {
                                    createBitmapWithBorder(borderSize, borderColor)
                                } else {
                                    this
                                }
                            ).apply {
                                isCircular = true
                            }
                        }
                    )
                }
            })
    }

    /**
     * Create a new bordered bitmap with the specified borderSize and borderColor
     *
     * @param borderSize - The border size in pixel
     * @param borderColor - The border color
     * @return A new bordered bitmap with the specified borderSize and borderColor
     */
    fun Bitmap.createBitmapWithBorder(borderSize: Float, borderColor: Int = Color.WHITE): Bitmap {
        val borderOffset = (borderSize * 2).toInt()
        val halfWidth = width / 2
        val halfHeight = height / 2
        val circleRadius = Math.min(halfWidth, halfHeight).toFloat()
        val newBitmap = Bitmap.createBitmap(
            width + borderOffset,
            height + borderOffset,
            Bitmap.Config.ARGB_8888
        )

        // Center coordinates of the image
        val centerX = halfWidth + borderSize
        val centerY = halfHeight + borderSize

        val paint = Paint()
        val canvas = Canvas(newBitmap).apply {
            // Set transparent initial area
            drawARGB(0, 0, 0, 0)
        }

        // Draw the transparent initial area
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, circleRadius, paint)

        // Draw the image
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, borderSize, borderSize, paint)

        // Draw the createBitmapWithBorder
        paint.xfermode = null
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.strokeWidth = borderSize
        canvas.drawCircle(centerX, centerY, circleRadius, paint)
        return newBitmap
    }

}