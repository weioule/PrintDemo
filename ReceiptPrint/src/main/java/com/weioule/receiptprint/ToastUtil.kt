package com.weioule.receiptprint

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

/**
 * Created by weioule
 * on 2019/12/12
 */
object ToastUtil {
    private var toast: Toast? = null
    @SuppressLint("WrongConstant")
    fun shortMsg(context: Context?, content: String?) {
        if (toast == null) {
            toast = Toast.makeText(context, content, 0)
        } else {
            toast!!.duration = 0
            toast!!.setText(content)
        }
        toast!!.show()
    }

    @SuppressLint("WrongConstant")
    fun shortMsg(context: Context?, resID: Int) {
        val content = context!!.resources.getString(resID)
        if (toast == null) {
            toast = Toast.makeText(context, content, 0)
        } else {
            toast!!.duration = 0
            toast!!.setText(content)
        }
        toast!!.show()
    }

    @SuppressLint("WrongConstant")
    fun longMsg(context: Context?, content: String?) {
        if (toast == null) {
            toast = Toast.makeText(context, content, 1)
        } else {
            toast!!.duration = 1
            toast!!.setText(content)
        }
        toast!!.show()
    }

    @SuppressLint("WrongConstant")
    fun longMsg(context: Context, resID: Int) {
        val content = context.resources.getString(resID)
        if (toast == null) {
            toast = Toast.makeText(context, content, 1)
        } else {
            toast!!.duration = 1
            toast!!.setText(content)
        }
        toast!!.show()
    }
}