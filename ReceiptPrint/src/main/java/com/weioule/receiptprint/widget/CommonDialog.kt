package com.weioule.receiptprint.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.weioule.receiptprint.R

/**
 * Author by weioule.
 * Date on 2022/09/11.
 */
class CommonDialog private constructor(
    context: Context,
    private val title: String?,
    private val message: String?,
    private val confirmText: String?,
    private val cancelText: String?,
    private val mOnConfirmClickListener: ((Any) -> Unit)?,
    private val mOnCancelClickListener: ((Any) -> Unit)?,
    private val contentType: Int?,
    private val titleTextColor: Int,
    private val messageTextColor: Int,
    private val cancelTextColor: Int,
    private val messageCenter: Boolean,
    private val allBold: Boolean,
) : Dialog(context, R.style.CommonDialog) {

    companion object {
        @kotlin.jvm.JvmStatic
        fun builder(context: Context): Builder {
            return Builder(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_text)
        setCanceledOnTouchOutside(false)
        initView()
    }

    private fun initView() {
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val btnConfirm = findViewById<TextView>(R.id.btn_ok)
        val tvContent = findViewById<TextView>(R.id.tvContent)
        val btnCancel = findViewById<TextView>(R.id.btn_cancel)
        if (title?.isNotEmpty() == true) {
            tvTitle.text = title
        }
        if (message?.isNotEmpty() == true) {
            tvContent.text = message
        }
        if (confirmText?.isNotEmpty() == true) {
            btnConfirm.text = confirmText
        }
        if (cancelText?.isNotEmpty() == true) {
            btnCancel.text = cancelText
        }
        if (contentType == 1) {
            btnCancel.visibility = View.VISIBLE
        } else {
            btnCancel.visibility = View.GONE
        }
        btnConfirm.setOnClickListener {
            mOnConfirmClickListener?.let(mOnConfirmClickListener)
            dismiss()
        }
        btnCancel.setOnClickListener {
            mOnCancelClickListener?.let(mOnCancelClickListener)
            dismiss()
        }
        if (titleTextColor != 0) {
            tvTitle.setTextColor(titleTextColor)
        }
        if (messageTextColor != 0) {
            tvContent.setTextColor(messageTextColor)
        }
        if (cancelTextColor != 0) {
            btnCancel.setTextColor(cancelTextColor)
        }
        if (messageCenter) {
            tvContent.gravity = Gravity.CENTER
        }
        if (allBold) {
            //加粗
            tvTitle.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            tvContent.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            btnCancel.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            btnConfirm.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        }
    }

    fun shown(): CommonDialog {
        show()
        return this
    }

    class Builder(private var mContext: Context) {

        private var mTitle: String? = null
        private var mMessage: String? = null
        private var mAllBold = false
        private var mCancelText: String? = null
        private var mConfirmText: String? = null
        private var mTitleTextColor = 0
        private var mCancelTextColor = 0
        private var mContentType: Int? = null
        private var mMessageTextColor = 0
        private var mMessageCenter = false
        private var mOnConfirmClickListener: ((Any) -> Unit)? = null
        private var mOnCancelClickListener: ((Any) -> Unit)? = null

        fun setTitle(title: String?): Builder {
            mTitle = title
            return this
        }

        fun setMessage(message: String?): Builder {
            mMessage = message
            return this
        }

        fun setAllBold(allBold: Boolean): Builder {
            mAllBold = allBold
            return this
        }

        fun setContext(context: Context): Builder {
            mContext = context
            return this
        }

        fun setCancelText(cancelText: String?): Builder {
            mCancelText = cancelText
            return this
        }

        fun setTitleTextColor(titleTextColor: Int): Builder {
            mTitleTextColor = titleTextColor
            return this
        }

        fun setCancelTextColor(cancelTextColor: Int): Builder {
            mCancelTextColor = cancelTextColor
            return this
        }

        fun setContentType(contentType: Int?): Builder {
            mContentType = contentType
            return this
        }

        fun setConfirmText(confirmText: String?): Builder {
            mConfirmText = confirmText
            return this
        }

        fun setMessageTextColor(messageTextColor: Int): Builder {
            mMessageTextColor = messageTextColor
            return this
        }

        fun setMessageCenter(messageCenter: Boolean): Builder {
            mMessageCenter = messageCenter
            return this
        }

        fun setOnCancelClickListener(
            confirmText: String?,
            onCancelClickListener: ((Any) -> Unit)?,
        ): Builder {
            mConfirmText = confirmText
            mOnCancelClickListener = onCancelClickListener

            return this
        }

        fun setOnConfirmClickListener(
            cancelText: String?,
            onConfirmClickListener: ((Any) -> Unit)?,
        ): Builder {
            mCancelText = cancelText
            mOnConfirmClickListener = onConfirmClickListener
            return this
        }

        fun build(): CommonDialog {
            return CommonDialog(
                mContext, mTitle, mMessage, mConfirmText,
                mCancelText, mOnConfirmClickListener, mOnCancelClickListener, mContentType,
                mTitleTextColor, mMessageTextColor, mCancelTextColor, mMessageCenter, mAllBold
            )
        }
    }
}