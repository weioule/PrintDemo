package com.weioule.receiptprint.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * @author weioule
 * @date 2019/7/22.
 */
class RecyclerViewDivider private constructor(private val mBuilder: Builder) :
    RecyclerView.ItemDecoration() {
    /**
     * The divider drawable
     */
    private var mDivider: Drawable? = null
    private var mOrientation = 0
    private val mBounds = Rect()

    /**
     * Set Divider Drawable
     */
    private fun setDividerDrawable() {
        val drawable: Drawable?
        drawable = if (mBuilder.mDrawable != null) {
            mBuilder.mDrawable
        } else {
            DividerDrawable(mBuilder.mColor)
        }
        mDivider = drawable
    }

    /**
     * Set Divider Orientation
     */
    fun setOrientation() {
        val orientation = mBuilder.mOrientation
        kotlin.require(!(orientation != HORIZONTAL && orientation != VERTICAL && orientation != GRIDEVIDW)) { "Invalid orientation. It should be either HORIZONTAL or VERTICAL or GRIDEVIDW" }
        mOrientation = orientation
    }

    var spanCount = 0
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val count = parent.adapter!!.itemCount
        if (mOrientation == GRIDEVIDW) {
            val width = mDivider!!.intrinsicWidth
            val height = mDivider!!.intrinsicHeight
            if (spanCount <= 0) {
                val layoutManager = parent.layoutManager
                if (null != layoutManager && layoutManager is StaggeredGridLayoutManager) {
                    spanCount = layoutManager.spanCount
                }
            }
            if (position == 0) {
                outRect[0, 0, width] = height
            } else if (position < spanCount - 1) {
                outRect[width, 0, width] = height
            } else if (position == spanCount - 1) {
                outRect[width, 0, 0] = height
            } else if (position == count - spanCount) {
                outRect[0, height, width] = 0
            } else if (position > count - spanCount && position < count - 1) {
                outRect[width, height, width] = 0
            } else if (position == count - 1) {
                outRect[width, height, 0] = 0
            } else if (position % spanCount == 0) {
                outRect[0, height, width] = height
            } else if (position % spanCount == 1) {
                outRect[width, height, 0] = height
            } else {
                outRect[width, height, width] = height
            }
        } else if (mOrientation == VERTICAL) {
            val height = mDivider!!.intrinsicHeight
            if (position == 0 && mBuilder.mShowTopDivider) {
                outRect[0, height, 0] = height
            } else if (!needSkip(position, count)) {
                outRect[0, 0, 0] = height
            }
        } else {
            val width = mDivider!!.intrinsicWidth
            if (position == 0 && mBuilder.mShowTopDivider) {
                outRect[width, 0, width] = 0
            } else if (!needSkip(position, count)) {
                outRect[0, 0, width] = 0
            }
        }
    }

    private fun needSkip(position: Int, count: Int): Boolean {
        return position < mBuilder.mStartSkipCount || position >= count - mBuilder.mEndSkipCount
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }
        if (mOrientation == GRIDEVIDW) {
            drawVertical(c, parent)
            drawHorizontal(c, parent)
        } else if (mOrientation == VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    /**
     * Draw vertical list divider
     *
     * @param canvas canvas
     * @param parent RecyclerView
     */
    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && parent.clipToPadding) {
            left = parent.paddingLeft + mBuilder.mMarginLeft
            right = parent.width - parent.paddingRight - mBuilder.mMarginRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = mBuilder.mMarginLeft
            right = parent.width - mBuilder.mMarginRight
        }
        val childCount = parent.childCount
        var top: Int
        var bottom: Int
        val count = parent.adapter!!.itemCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (needSkip(position, count)) {
                continue
            }
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
            bottom = mBounds.bottom + Math.round(ViewCompat.getTranslationY(child))
            top = bottom - mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        if (childCount > 0 && mBuilder.mShowTopDivider) {
            val child = parent.getChildAt(0)
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
            top = mBounds.top + Math.round(ViewCompat.getTranslationY(child))
            bottom = top + mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    /**
     * Draw horizontal list divider
     *
     * @param canvas canvas
     * @param parent RecyclerView
     */
    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && parent.clipToPadding) {
            top = parent.paddingTop + mBuilder.mMarginTop
            bottom = parent.height - parent.paddingBottom - mBuilder.mMarginBottom
            canvas.clipRect(parent.paddingLeft, top, parent.width - parent.paddingRight, bottom)
        } else {
            top = mBuilder.mMarginTop
            bottom = parent.height - mBuilder.mMarginBottom
        }
        val childCount = parent.childCount
        var left: Int
        var right: Int
        val count = parent.adapter!!.itemCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (needSkip(position, count)) {
                continue
            }
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
            right = mBounds.right + Math.round(ViewCompat.getTranslationX(child))
            left = right - mDivider!!.intrinsicWidth
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        if (childCount > 0 && mBuilder.mShowTopDivider) {
            val child = parent.getChildAt(0)
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
            left = mBounds.left + Math.round(ViewCompat.getTranslationX(child))
            right = left + mDivider!!.intrinsicWidth
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    /**
     * RecyclerView Divider Builder
     */
    class Builder(private val mContext: Context) {
        var mDrawable: Drawable? = null
        var mOrientation = VERTICAL
        var mSize = 1
        var mMarginLeft = 0
        var mMarginRight = 0
        var mMarginTop = 0
        var mMarginBottom = 0
        var mColor = -0x2e2e2f
        var mStartSkipCount = 0
        var mEndSkipCount = 0

        @Style
        private var mStyle = Style.END
        var mShowTopDivider = false

        /**
         * Set divider drawable
         *
         * @param drawable Divider drawable
         */
        fun setDrawable(drawable: Drawable?): Builder {
            mDrawable = drawable
            return this
        }

        /**
         * Set divider drawable resource
         *
         * @param drawableRes Divider drawable resource
         */
        fun setDrawableRes(drawableRes: Int): Builder {
            mDrawable = mContext.resources.getDrawable(drawableRes)
            return this
        }

        /**
         * Set divider style
         *
         * @param style divider style
         */
        fun setStyle(@Style style: Int): Builder {
            mStyle = style
            return this
        }

        /**
         * Set divider orientation
         *
         * @param orientation divider orientation
         */
        fun setOrientation(orientation: Int): Builder {
            mOrientation = orientation
            return this
        }

        /**
         * Set divider size
         *
         * @param size divider size
         */
        fun setSize(size: Float): Builder {
            return setSize(TypedValue.COMPLEX_UNIT_DIP, size)
        }

        /**
         * Set divider height
         *
         * @param unit   divider height unit
         * @param height divider height
         */
        fun setSize(unit: Int, height: Float): Builder {
            mSize = getSizeValue(unit, height)
            return this
        }

        /**
         * Set divider margin left
         *
         * @param marginLeft margin left value
         */
        fun setMarginLeft(marginLeft: Float): Builder {
            return setMarginLeft(TypedValue.COMPLEX_UNIT_DIP, marginLeft)
        }

        /**
         * Set divider margin left
         *
         * @param unit       margin left value unit
         * @param marginLeft margin left value
         */
        fun setMarginLeft(unit: Int, marginLeft: Float): Builder {
            mMarginLeft = getSizeValue(unit, marginLeft)
            return this
        }

        /**
         * Set divider margin right
         *
         * @param marginRight margin right value
         */
        fun setMarginRight(marginRight: Float): Builder {
            return setMarginRight(TypedValue.COMPLEX_UNIT_DIP, marginRight)
        }

        /**
         * Set divider margin right
         *
         * @param unit        margin right value unit
         * @param marginRight margin right value
         */
        fun setMarginRight(unit: Int, marginRight: Float): Builder {
            mMarginRight = getSizeValue(unit, marginRight)
            return this
        }

        /**
         * Set divider margin top
         *
         * @param marginTop margin top value
         */
        fun setMarginTop(marginTop: Int): Builder {
            return setMarginTop(TypedValue.COMPLEX_UNIT_DIP, marginTop)
        }

        /**
         * Set divider margin right
         *
         * @param unit      margin right value unit
         * @param marginTop margin top value
         */
        fun setMarginTop(unit: Int, marginTop: Int): Builder {
            mMarginTop = getSizeValue(unit, marginTop.toFloat())
            return this
        }

        /**
         * Set divider margin bottom
         *
         * @param marginBottom margin bottom value
         */
        fun setMarginBottom(marginBottom: Float): Builder {
            return setMarginBottom(TypedValue.COMPLEX_UNIT_DIP, marginBottom)
        }

        /**
         * Set divider margin bottom
         *
         * @param unit         margin bottom value unit
         * @param marginBottom margin bottom value
         */
        fun setMarginBottom(unit: Int, marginBottom: Float): Builder {
            mMarginBottom = getSizeValue(unit, marginBottom)
            return this
        }

        /**
         * Set divider color
         *
         * @param color divider color
         */
        fun setColor(@ColorInt color: Int): Builder {
            mColor = color
            return this
        }

        /**
         * Set divider color
         *
         * @param colorRes divider color resource
         */
        fun setColorRes(@ColorRes colorRes: Int): Builder {
            mColor = mContext.resources.getColor(colorRes)
            return this
        }

        /**
         * Set skip count from start
         *
         * @param startSkipCount count from start
         */
        fun setStartSkipCount(startSkipCount: Int): Builder {
            mStartSkipCount = startSkipCount
            return this
        }

        fun getmStartSkipCount(): Int {
            return mStartSkipCount
        }

        /**
         * Set skip count before end
         *
         * @param endSkipCount count before end
         */
        fun setEndSkipCount(endSkipCount: Int): Builder {
            mEndSkipCount = endSkipCount
            return this
        }

        private fun getSizeValue(unit: Int, size: Float): Int {
            return TypedValue.applyDimension(unit, size, mContext.resources.displayMetrics)
                .toInt()
        }

        fun build(): RecyclerViewDivider {
            when (mStyle) {
                Style.BETWEEN -> mEndSkipCount++
                Style.BOTH -> mStartSkipCount--
                Style.END -> {
                }
                Style.START -> mEndSkipCount++
            }
            mShowTopDivider = mStyle == Style.BOTH && mStartSkipCount < 0 || mStyle == Style.START
            return RecyclerViewDivider(this)
        }
    }

    /**
     * DividerDrawable
     */
    private inner class DividerDrawable internal constructor(color: Int) : ColorDrawable(color) {
        override fun getIntrinsicWidth(): Int {
            return mBuilder.mSize
        }

        override fun getIntrinsicHeight(): Int {
            return mBuilder.mSize
        }
    }

    /**
     * Divider Style
     * END         包尾
     * START       包前
     * BOTH        前后都包
     * BETWEEN     前后都不包
     */
    annotation class Style {
        companion object {
            var END = 0 //包尾
            var START = 1 //包前
            var BOTH = 2 //前后都包
            var BETWEEN = 3 //前后都不包
        }
    }

    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL
        const val GRIDEVIDW = 888
    }

    init {
        setOrientation()
        setDividerDrawable()
    }
}