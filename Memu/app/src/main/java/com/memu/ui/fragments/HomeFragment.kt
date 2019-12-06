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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(com.memu.R.layout.home_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {

    }

    override fun onClick(v: View?) {

    }



}
