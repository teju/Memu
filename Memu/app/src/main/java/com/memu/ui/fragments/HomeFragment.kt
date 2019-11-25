package com.memu.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memu.R
import com.memu.ui.BaseFragment
import android.animation.Animator
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ObjectAnimator
import android.animation.Animator.AnimatorListener
import android.widget.LinearLayout
import android.util.TypedValue
import android.widget.TextView
import kotlinx.android.synthetic.main.home_fragment.*
import java.util.*
import android.view.MotionEvent
import com.memu.etc.Helper


class HomeFragment : BaseFragment() , View.OnClickListener {
    private var mOnTouchListener: View.OnTouchListener? = null

    var textView : TextView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(com.memu.R.layout.home_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        textView = _textView
        mOnTouchListener = View.OnTouchListener { v, event ->
            // simple trigger to start the animation.
            startAnimation()
            textView!!.setOnTouchListener(null)
            true
        }
        textView!!.setOnTouchListener(mOnTouchListener)
    }

    override fun onClick(v: View?) {

    }

    private fun startAnimation() {

        val origin = textView?.getParent() as LinearLayout
        var destination: LinearLayout? = null
        // I'm not sure what kind of behaviour you want. This just randomises the destination.
        do {
            when (Random().nextInt(4)) {
                0 -> destination = layout1
                1 -> destination = layout2
                2 -> destination = layout3
                3 -> destination = layout4
            }
            // if destination == origin or is null, try again.
        } while (destination === origin || destination == null)

        // Create another TextView and initialise it to match textView
        val textViewNew = TextView(activity)
        textViewNew.setText(textView?.getText())
        textViewNew.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView?.getTextSize()!!)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(Helper.dpToPx(activity!!,75),500,0,0)

        textViewNew.layoutParams = params
        textViewNew.setOnTouchListener(mOnTouchListener);

        // Add the new TextView to the destination LinearLayout
        destination.addView(textViewNew)

        // Create animations based on origin and destination LinearLayouts
        val outAnimator = getOutAnimator(origin, destination)
        // The in animator also requires a reference to the new TextView
        val inAnimator = getInAnimator(textViewNew, origin, destination)
        // All animators must be created before any are started because they are calculated
        // using values that are modified by the animation itself.
        outAnimator.start()
        inAnimator.start()
        // Add a listener to update textView reference to the new TextView when complete.
        inAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {

                origin.removeView(textView)
                textView = textViewNew
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    /**
     * This method creates an ObjectAnimator to move the existing TextView out of its parent
     * towards its destination
     */
    private fun getOutAnimator(origin: View, destination: View): ObjectAnimator {

        // Calculate the difference between x of destination and of origin
        val layoutDifferenceX = destination.x - origin.x
        // initialX is simply textView.getX()
        // the distance moved == layoutDifferenceX
        val finalX = textView?.getX()!! + layoutDifferenceX

        val animator = ObjectAnimator.ofFloat(
            textView, "x",
            textView?.getX()!!, finalX!!
        )
        animator.setInterpolator(AccelerateDecelerateInterpolator())
        animator.setDuration(500)

        return animator
    }

    /**
     * This method creates an ObjectAnimator to move the new TextView from the initial position
     * of textView, relative to the new TextView's parent, to its destination.
     */
    private fun getInAnimator(newView: View, origin: View, destination: View): ObjectAnimator {

        // Calculate the difference between x of destination and of origin
        val layoutDifferenceX = destination.x - origin.x
        // initialX relative to destination
        val initialX = textView?.getX()!! - layoutDifferenceX

        // finalX relative to destination == initialX relative to origin
        val finalX = textView?.getX()

        val animator = ObjectAnimator.ofFloat(
            newView, "x",
            initialX!!, finalX!!
        )
        animator.setInterpolator(AccelerateDecelerateInterpolator())
        animator.setDuration(500)

        return animator
    }


}
