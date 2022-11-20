package com.weioule.receiptprint.widget

import android.app.ProgressDialog
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.weioule.receiptprint.R

/**
 * Author by weioule.
 * Date on 2022/10/31.
 */
class WaitingDialog : ProgressDialog {
    private var tvLoading: TextView? = null
    private var ivLoading: ImageView? = null

    constructor(context: Context?) : super(context, R.style.dialog) {}
    constructor(context: Context?, theme: Int) : super(context, theme) {}

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.dialog_waiting)
        initView()
        doNext()
    }

    private fun initView() {
        tvLoading = findViewById(R.id.waiting_tv_loading)
        ivLoading = findViewById(R.id.waiting_iv_loading)
    }

    private fun doNext() {
        startAnim()
        setCancelable(false)
    }

    private fun startAnim() {
        val animationDrawable = ivLoading!!.background as AnimationDrawable
        animationDrawable.start()
    }

    override fun setMessage(msg: CharSequence) {
        tvLoading!!.text = msg
    }
}