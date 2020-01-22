package com.memu.ui.fragments

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout

import androidx.appcompat.app.AppCompatActivity
import com.iapps.libs.helpers.BaseHelper.getViewToViewScalingAnimator

import com.memu.R

class MainActivity : AppCompatActivity() {
    private var rootView: RelativeLayout? = null
    private var fromView: View? = null
    private var toView: View? = null
    private var shuttleView: View? = null
    val ANIMATION_SPEED = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_layout)

        rootView = findViewById<View>(R.id.rootView) as RelativeLayout
        fromView = findViewById(R.id.itemFrom)
        toView = findViewById(R.id.itemTo)
        shuttleView = findViewById(R.id.shuttle)

/*
        fromView!!.setOnClickListener {
            val fromRect = Rect()
            val toRect = Rect()
            fromView!!.getGlobalVisibleRect(fromRect)
            toView!!.getGlobalVisibleRect(toRect)

            val animatorSet = getViewToViewScalingAnimator(
                rootView!!,
                shuttleView!!,
                fromRect,
                toRect,
                ANIMATION_SPEED.toLong(),
                0
            )

            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    shuttleView!!.visibility = View.VISIBLE
                    fromView!!.visibility = View.INVISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    shuttleView!!.visibility = View.GONE
                    fromView!!.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            animatorSet.start()
        }
*/
    }

}