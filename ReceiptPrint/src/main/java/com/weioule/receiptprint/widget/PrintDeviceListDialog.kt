package com.weioule.receiptprint.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.weioule.receiptprint.DeviceAdapter
import com.weioule.receiptprint.R
import com.weioule.receiptprint.Utils
import com.weioule.receiptprint.bean.DeviceBean
import java.util.*

/**
 * Created by weioule
 * on 2022/01/07
 */
class PrintDeviceListDialog(context: Context?, private val listener: DeviceSelectListener?) :
    Dialog(
        context!!, R.style.ActionSheetDialogStyle
    ) {
    private var lastClickTime: Long = 0
    private var recyclerView: RecyclerView? = null
    private var flClose: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_print_device_list)
        initSize()
        initView()
        setCancelable(false)
    }

    private fun initSize() {
        val width: Int
        val dialogWindow = window
        val displayMetrics = DisplayMetrics()
        dialogWindow!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val p = dialogWindow.attributes // 获取对话框当前的参数值
        p.width = (width * 0.9).toInt()
        dialogWindow.attributes = p
    }

    private fun initView() {
        flClose = findViewById(R.id.flClose)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        val divider = RecyclerViewDivider.Builder(context)
            .setStyle(RecyclerViewDivider.Style.Companion.BETWEEN)
            .setColor(context.resources.getColor(R.color.gray_light))
            .setOrientation(RecyclerViewDivider.Companion.VERTICAL)
            .setSize(1f)
            .build()
        recyclerView?.addItemDecoration(divider!!)
        flClose?.setOnClickListener(View.OnClickListener { dismiss() })
    }

    fun setContent(printerDevices: List<DeviceBean?>) {
        if (printerDevices.size > 5) {
            val needHeight =
                (window!!.windowManager.defaultDisplay.height * 0.6).toInt() //最大高度为屏幕的0.6倍
            val params =
                RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, needHeight)
            params.topMargin = Utils.dp2px(40)
            recyclerView!!.layoutParams = params
            recyclerView!!.overScrollMode = View.OVER_SCROLL_ALWAYS
        }
        val deviceAdapter = DeviceAdapter(printerDevices)
        recyclerView!!.adapter = deviceAdapter
        deviceAdapter.onItemClickListener =
            BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
                val currentTime = Calendar.getInstance().timeInMillis
                //防止重复点击
                if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                    lastClickTime = currentTime
                    if (null != listener && null != adapter.data && position >= 0 && position < adapter.data.size) {
                        listener.onSelect(adapter.data[position] as DeviceBean)
                        dismiss()
                    }
                }
            }
    }

    interface DeviceSelectListener {
        fun onSelect(deviceBean: DeviceBean?)
    }

    companion object {
        const val MIN_CLICK_DELAY_TIME = 600
    }
}