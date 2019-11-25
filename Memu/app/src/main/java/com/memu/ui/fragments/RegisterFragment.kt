package com.memu.ui.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import com.memu.R
import com.memu.etc.Helper
import com.memu.ui.BaseFragment
import kotlinx.android.synthetic.main.register_fragment.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager


class RegisterFragment : BaseFragment() , View.OnClickListener {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.register_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        no_vehicle.setOnClickListener(this)
        private_vehicle.setOnClickListener(this)
        cab_vehicle.setOnClickListener(this)
        ll!!.setAllParentsClip(false)
        rv.setLayoutManager(LinearLayoutManager(activity))
        rv.setHasFixedSize(true)
    }

    fun View.setAllParentsClip(enabled: Boolean) {
        var parent = parent
        while (parent is ViewGroup) {
            parent.clipChildren = enabled
            parent.clipToPadding = enabled
            parent = parent.parent
        }
    }
    override fun onClick(v: View?) {
        if(destination != null) {
            destination!!.removeView(textViewNew)
        }
        when (v?.id)
        {
            R.id.no_vehicle ->{

                white_car.visibility = View.VISIBLE
                yellow_car.visibility = View.GONE
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_3.getY().toInt()).setDuration(2000).start();
                destination = onbording_3
                startAnimation(white_car,R.drawable.white_car,300 )
            }
            R.id.private_vehicle ->{
                white_car.visibility = View.VISIBLE
                yellow_car.visibility = View.GONE
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_5.getY().toInt()).setDuration(2000).start();
                destination = onbording_5

                startAnimation(white_car,R.drawable.white_car,300 )
            }
            R.id.cab_vehicle ->{
                white_car.visibility = View.GONE
                yellow_car.visibility = View.VISIBLE
                ObjectAnimator.ofInt(sv, "scrollY",  onbording_4.getY().toInt()).setDuration(2000).start();
                destination = onbording_4
                startAnimation(yellow_car,R.drawable.yellow_car,700 )


            }
        }
    }

    var destination: RelativeLayout? = null
    var textViewNew:ImageView? = null

    private fun startAnimation(textView:ImageView,drawable : Int,top_margin : Int) {
        textView.visibility = View.GONE
        val origin = textView?.getParent() as RelativeLayout
        textViewNew = ImageView(activity)
        // Create another TextView and initialise it to match textView
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(Helper.dpToPx(activity!!,75),Helper.dpToPx(activity!!,200),0,0)
        textViewNew!!.setImageDrawable(activity?.getDrawable(drawable))
        textViewNew!!.layoutParams = params

        // Add the new TextView to the destination LinearLayout
        destination?.addView(textViewNew)

        // Create animations based on origin and destination LinearLayouts
        val outAnimator = getOutAnimator(origin, destination!!,textView)
        // The in animator also requires a reference to the new TextView
        val inAnimator = getInAnimator(textViewNew!!, origin, destination!!,textView,top_margin)
        // All animators must be created before any are started because they are calculated
        // using values that are modified by the animation itself.
        outAnimator.start()
        inAnimator.start()
        // Add a listener to update textView reference to the new TextView when complete.
        inAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    /**
     * This method creates an ObjectAnimator to move the existing TextView out of its parent
     * towards its destination
     */
    private fun getOutAnimator(origin: View, destination: View,textView : ImageView): ObjectAnimator {

        // Calculate the difference between x of destination and of origin
        val layoutDifferenceX = destination.y - origin.y + 200
        // initialX is simply textView.getX()
        // the distance moved == layoutDifferenceX
        val finalX = textView?.getX()!! + layoutDifferenceX

        val animator = ObjectAnimator.ofFloat(
            textView, "y",
            textView?.getX()!!, finalX!!
        )
        animator.setInterpolator(AccelerateDecelerateInterpolator())
        animator.setDuration(2000)

        return animator
    }

    /**
     * This method creates an ObjectAnimator to move the new TextView from the initial position
     * of textView, relative to the new TextView's parent, to its destination.
     */
    private fun getInAnimator(newView: View, origin: View,
                              destination: View,textView : ImageView,top_margin : Int): ObjectAnimator {

        // Calculate the difference between y of destination and of origin
        val layoutDifferenceX = destination.y - (origin.y + Helper.dpToPx(activity!!,top_margin))
        // initialX relative to destination
        val initialX = textView?.getX()!! - layoutDifferenceX

        // finalX relative to destination == initialX relative to origin
        val finalX = textView?.getX()

        val animator = ObjectAnimator.ofFloat(
            newView, "y",
            initialX!!, finalX!!
        )
        animator.setInterpolator(AccelerateDecelerateInterpolator())
        animator.setDuration(2000)

        return animator
    }


}
