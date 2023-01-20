package com.weioule.receiptprint

import HPRTAndroidSDK.HPRTPrinterHelper
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.android_print_sdk.Barcode
import com.android_print_sdk.PrinterType
import com.android_print_sdk.bluetooth.BluetoothPrinter
import com.caysn.autoreplyprint.AutoReplyPrint
import com.caysn.autoreplyprint.AutoReplyPrint.*
import com.github.promeg.pinyinhelper.Pinyin
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.sun.jna.Pointer
import com.weioule.receiptprint.bean.DeviceBean
import com.weioule.receiptprint.bean.OriginalDataBean
import com.weioule.receiptprint.bean.PrintLineInfoBean
import com.weioule.receiptprint.widget.CommonDialog
import com.weioule.receiptprint.widget.PrintDeviceListDialog
import com.weioule.receiptprint.widget.PrintDeviceListDialog.DeviceSelectListener
import com.weioule.receiptprint.widget.WaitingDialog
import print.Print
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.util.*

/**
 * Author by weioule.
 * Date on 2022/09/10.
 */
class ReceiptPrintUtil {
    private var fontSize = 0 //字体类型

    /**
     * 打印换行
     *
     * @return length 需要打印的空行数
     */
    /**
     * 打印换行(只换一行)
     */
    private fun printLine(lineNum: Int = 1) {
        for (i in 0 until lineNum) {
            printText("\n")
        }
    }

    /**
     * 打印空白(一个Tab的位置，约4个汉字)
     *
     * @param length 需要打印空白的长度,
     */
    private fun printTabSpace(length: Int) {
        for (i in 0 until length) {
            printText("  ")
        }
    }

    /**
     * 绝对打印位置
     *
     * @return
     */
    private fun setLocation(offset: Int): ByteArray {
        val bs = ByteArray(4)
        bs[0] = 0x1B
        bs[1] = 0x24
        bs[2] = (offset % 256).toByte()
        bs[3] = (offset / 256).toByte()
        return bs
    }

    /**
     * 字体大小
     *
     * @param fontSize
     */
    private fun setFontSizeCmd(fontSize: Int) {
        this.fontSize = fontSize
        val data = byteArrayOf(0x1d.toByte(), 0x21.toByte(), 0x0.toByte())
        if (fontSize == FONT_NORMAL) {
            data[2] = 0x00.toByte()
        } else if (fontSize == FONT_MIDDLE_) {
            data[2] = 18.toByte()
        } else if (fontSize == FONT_MIDDLE) {
            data[2] = 0x01.toByte()
        } else if (fontSize == FONT_BIG) {
            data[2] = 0x11.toByte()
        }
        try {
            Print.WriteData(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 加粗模式
     *
     * @param fontBold
     */
    private fun setFontBoldCmd(fontBold: Int) {
        val data = byteArrayOf(0x1b.toByte(), 0x45.toByte(), 0x0.toByte())
        if (fontBold == FONT_BOLD) {
            data[2] = 0x01.toByte()
        } else if (fontBold == FONT_BOLD_CANCEL) {
            data[2] = 0x00.toByte()
        }
        try {
            Print.WriteData(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 对齐方式
     *
     * @param alignMode
     * @return
     */
    private fun setAlignCmd(alignMode: Int) {
        val data = byteArrayOf(0x1b.toByte(), 0x61.toByte(), 0x0.toByte())
        if (alignMode == ALIGN_LEFT) {
            data[2] = 0x00.toByte()
        } else if (alignMode == ALIGN_CENTER) {
            data[2] = 0x01.toByte()
        } else if (alignMode == ALIGN_RIGHT) {
            data[2] = 0x02.toByte()
        }
        try {
            Print.WriteData(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun printText(bs: ByteArray) {
        try {
            Print.WriteData(bs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 打印文字
     *
     * @param text
     */
    private fun printText(text: String) {
        try {
            Print.WriteData(text.toByteArray(charset("GBK")))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getGbk(stText: String?): ByteArray {
        return stText?.toByteArray(charset("GBK"))!!
    }

    //这里计算打印内容的长度，如果需要处理间距、或不同大小的字体调整，可以在这里做适配处理
    private fun getStringPixLength(str: String?): Int {
        var pixLength = 0
        var c: Char
        for (element in str!!) {
            c = element
            pixLength += if (fontSize == FONT_BIG) {
                if (Pinyin.isChinese(c)) {
                    48
                } else {
                    24
                }
            } else if (Pinyin.isChinese(c)) {
                24
            } else {
                12
            }
        }
        return pixLength
    }

    private fun getOffset(str: String?): Int {
        val length = WIDTH_PIXEL - getStringPixLength(str)
        //如果长度大于纸张宽度，就右对齐打印，计算左边间距
        return if (length < 0) WIDTH_PIXEL - Math.abs(length) else length
    }

    //这个排版格式是可以公用的，打印机基本都是可以打印byte数组
    @kotlin.Throws(UnsupportedEncodingException::class)
    fun printTwoColumn(left: String?, middle: String?): ByteArray {
        var left = left
        var iNum = 0
        val byteBuffer = ByteArray(100)

        //手动添加间距
        left += " "
        val pixLength = getStringPixLength(left) % WIDTH_PIXEL
        val middleLength = getStringPixLength(middle) % WIDTH_PIXEL
        val leftRemaining = WIDTH_PIXEL - middleLength
        var offset = 0
        //计算后需要换行
        if (pixLength > LEFT_MAX_SIZE || left!!.isNotEmpty() && pixLength == 0) {
            left += "\n"
        } else if (leftRemaining < 0) {
            //右侧内容长度大于纸张宽度，就右对齐打印，计算左边间距
            offset = WIDTH_PIXEL - Math.abs(leftRemaining)
        } else if (leftRemaining < pixLength) {
            //左间距小于左侧内容，就换行打印
            left += "\n"
        } else offset = leftRemaining
        var tmp: ByteArray = getGbk(left)
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        iNum += tmp.size
        tmp = if (offset > 0) setLocation(offset) else setLocation(getOffset(middle))
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        iNum += tmp.size
        tmp = getGbk(middle)
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        return byteBuffer
    }

    //这个排版格式是可以公用的，打印机基本都是可以打印byte数组
    @kotlin.Throws(UnsupportedEncodingException::class)
    fun printThreeColumn(left: String?, middle: String?, right: String?): ByteArray {
        var left = left
        var middle = middle
        var iNum = 0
        val byteBuffer = ByteArray(200)
        var tmp = ByteArray(0)

        //手动添加间距
        left += " "
        middle += " "
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        iNum += tmp.size
        val pixLength = getStringPixLength(left) % WIDTH_PIXEL
        var middleLength = getStringPixLength(middle) % WIDTH_PIXEL
        val rightLength = getStringPixLength(right) % WIDTH_PIXEL
        val leftRemaining = WIDTH_PIXEL - middleLength - rightLength

        //计算后需要换行
        if (pixLength > LEFT_MAX_SIZE || left!!.isNotEmpty() && pixLength == 0 || leftRemaining < pixLength) {
            left += "\n"
        }
        tmp = getGbk(left)
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        iNum += tmp.size
        if (leftRemaining < 0) {
            //中间内容+右侧内容长度大于纸张宽度，将右侧内容换行打印，中间内容与头部“数量”对齐，右侧内容右对齐
            middle = middle?.trim { it <= ' ' } //换行后删除右空格
            middleLength = getStringPixLength(middle) % WIDTH_PIXEL //删除有空格后重新获取长度
            middle += "\n"
            //若中间内容长度较长，超出右侧的空间（WIDTH_PIXEL-170），则右对齐打印，左侧间距为：WIDTH_PIXEL - middleLength
            tmp = setLocation(170.coerceAtMost(WIDTH_PIXEL - middleLength))
        } else if (middle!!.length + right!!.length >= 12 || fontSize == FONT_BIG && middle.length + right.length >= 8) {
            //数量与小计的字符长度超出其默认范围12，则往从右往左打印
            //大字体字符长度范围为8
            tmp = setLocation(leftRemaining)
        } else {
            tmp = if (fontSize == FONT_BIG) //大字体头部的“数量”位置调整为距左侧170像素
                setLocation(170) else setLocation(240)
        }
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        iNum += tmp.size
        tmp = getGbk(middle)
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        iNum += tmp.size
        tmp = setLocation(getOffset(right))
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        iNum += tmp.size
        tmp = getGbk(right)
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.size)
        return byteBuffer
    }

    fun printDashLine() {
        if (fontSize == FONT_BIG) //大字体虚线长度调整
            printText("----------------")
        else
            printText("--------------------------------")
    }

    private interface OnPrintListener {
        fun onSuccess()
        fun onFail(msg: String)
    }

    //外部回调接口
    interface PrintResultListener {
        fun onResult(success: Boolean)
    }

    companion object {
        private var currentPrintType = 0 //打印机类型：1 汉印   2 爱印   3 复坤
        private var currentPrintAddress: String? = null//当前连接的打印机mac地址（已连接成功）
        private var currentAddress: String? = null //正在连接中的地址（没有连接完成，连接状态未知）

        //加粗模式
        private const val FONT_BOLD = 0 // 字体加粗
        private const val FONT_BOLD_CANCEL = 1 // 取消加粗

        //字体大小
        private const val FONT_NORMAL = 0 // 正常
        private const val FONT_MIDDLE = 1 // 中等
        private const val FONT_MIDDLE_ = 3 // 中小
        private const val FONT_BIG = 2 // 大

        // 对齐方式
        private const val ALIGN_LEFT = 0 // 靠左
        private const val ALIGN_CENTER = 1 // 居中
        private const val ALIGN_RIGHT = 2 // 靠右
        private val mOutputStream: OutputStream? = null
        const val WIDTH_PIXEL = 384
        private const val IMAGE_SIZE = 180
        const val LEFT_MAX_SIZE = 240
        private var activity: Activity? = null
        private var listener: PrintResultListener? = null
        private var mBluetoothAdapter: BluetoothAdapter? = null
        private var mPrinter: BluetoothPrinter? = null
        private var oldAYPinter: BluetoothPrinter? = null
        private var pointer: Pointer? = null
        private var oldFKPinter: Pointer? = null
        private val mListData: MutableList<OriginalDataBean> = ArrayList()
        private const val MIN_CLICK_DELAY_TIME = 800
        private var lastClickTime: Long = 0

        /**
         * 连接不打印
         */
        fun connect(act: Activity?) {
            activity = act
            connectAndPrint()
        }

        /**
         * 连接和打印单个小票,不带回调
         *
         * @param data 带打印配置的每行数据信息集合 (也就是单个小票打印)
         */
        @JvmStatic
        fun connectAndPrint(
            act: Activity?,
            data: List<PrintLineInfoBean>,
        ) {
            connectAndPrint(act, data, null)
        }

        /**
         * 连接和打印单个小票,带回调
         *
         * @param data 带打印配置的每行数据信息集合 (也就是单个小票打印)
         */
        @JvmStatic
        fun connectAndPrint(
            act: Activity?,
            data: List<PrintLineInfoBean>,
            printResultListener: PrintResultListener? = null,
        ) {
            val list = ArrayList<OriginalDataBean>()
            val bean = OriginalDataBean()
            bean.printLineInfoBeanList = data
            list.add(bean)
            connectAndPrintList(act, list, printResultListener)
        }

        /**
         * 连接和打印多个小票,不带回调
         *
         * @param data 带打印配置的每行数据信息集合 (也就是单个小票打印)
         */
        @JvmStatic
        fun connectAndPrintList(act: Activity?, data: List<OriginalDataBean>?) {
            connectAndPrintList(act, data, null)
        }

        /**
         * 连接和打印多个小票,带回调
         *
         * @param list 原始数据集合，里面包含带打印配置的每行数据信息集合 (也就是多个小票打印)
         */
        @JvmStatic
        fun connectAndPrintList(
            act: Activity?,
            list: List<OriginalDataBean>?,
            printResultListener: PrintResultListener? = null,
        ) {
            //数据错误不打印
            if (null == list || list.isEmpty()) {
                if (!isCloseClass) ToastUtil.shortMsg(
                    activity,
                    R.string.receipt_information_exception
                )
                return
            }
            activity = act
            mListData.clear()
            mListData.addAll(list)
            listener = printResultListener
            connectAndPrint()
        }

        private fun connectAndPrint() {
            //防止重复点击，后面造成打印混乱
            val currentTime = Calendar.getInstance().timeInMillis
            if (currentTime - lastClickTime < MIN_CLICK_DELAY_TIME) {
                return
            } else lastClickTime = currentTime
            if (!startDiscovery()) {
                if (isCloseClass) {
                    return
                }
                CommonDialog.Builder(activity!!)
                    .setTitle("温馨提示：")
                    .setMessage("请开启蓝牙并配对打印机")
                    .setOnConfirmClickListener("去设置") {
                        activity!!.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        startDiscovery()
                    }
                    .setOnCancelClickListener("已设置") {
                        startDiscovery()
                    }
                    .setContentType(1)
                    .build()
                    .shown()
            } else {
                fillAdapter()
            }
        }

        private val bluetoothAdapter: BluetoothAdapter?
            private get() {
                if (mBluetoothAdapter == null) {
                    synchronized(ReceiptPrintUtil::class.java) {
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    }
                }
                return mBluetoothAdapter
            }

        /**
         * 开始搜索蓝牙地址
         *
         * @return
         */
        private fun startDiscovery(): Boolean {
            //判断蓝牙是否开启
            return bluetoothAdapter!!.isEnabled
        }

        /**
         * 从所有已配对设备中找出打印设备并显示
         */
        private fun fillAdapter() {
            if (isCloseClass) {
                return
            }
            val printerDevices = pairedDevices
            if (printerDevices != null && printerDevices.isEmpty()) {
                if (isCloseClass) {
                    return
                }
                CommonDialog.Builder(activity!!)
                    .setTitle("温馨提示：")
                    .setMessage("请先到蓝牙设置里配对打印机")
                    .setOnConfirmClickListener("去配对") {
                        activity!!.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        fillAdapter()
                    }
                    .setOnCancelClickListener("已配对") {
                        fillAdapter()
                    }
                    .setContentType(1)
                    .build()
                    .shown()
            } else if (printerDevices != null && printerDevices.size == 1) {
                printerDevices[0]?.let { connect(it) }
            } else {
                //展示设备列表，手动选择
                showDeviceListDialog(printerDevices)
            }
        }

        @Synchronized
        private fun connect(device: DeviceBean) {
            if (isCloseClass) {
                return
            }
            showWaitingDialog("请等待,打印机连接中...")

            //防止快速点击崩溃
            Thread(label@ Runnable {
                val deviceName = device.deviceName
                val deviceAddress = device.deviceAddress
                val deviceStatus = device.deviceStatus
                if (deviceStatus == BluetoothDevice.BOND_BONDED) {
                    if (deviceName?.startsWith("MPT-") == true) {
                        if (deviceAddress == currentPrintAddress && Print.IsOpened() && "18" == isConnect) {
                            //当前为已连接且正常状态状态并且需要打印，则直接打印，（若未连接就走下方的连接代码）
                            printYcPackageInfos(mListData)
                            return@Runnable
                        }
                        try {
                            //连接新的打印机,关闭旧的打印机
                            Print.PortClose()
                            //关闭爱印打印机
                            if (null != mPrinter) {
                                mPrinter!!.closeConnection()
                                mPrinter = null
                            }
                            closeFKPrinter(pointer)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            var isOpenSuccess = -1
                            Print.Initialize()
                            Print.SetPrintDensity(127.toByte())
                            if (isCloseClass) {
                                //如果页面已经关闭，就不需要再调用关闭弹框方法，因为页面关闭弹框也被跟着销毁了
                                return@Runnable
                            }
                            isOpenSuccess = Print.PortOpen(activity, "Bluetooth,$deviceAddress")
                            currentPrintType = 1
                            currentPrintAddress = deviceAddress
                            if (isOpenSuccess == 0) {
                                if (isCloseClass) {
                                    return@Runnable
                                }
                                printYcPackageInfos(mListData)
                            } else {
                                if (isCloseClass) {
                                    return@Runnable
                                }
                                activity!!.runOnUiThread {
                                    hideWaitingDialog()
                                    showConnectFailDialog()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hideWaitingDialog()
                            if (null != listener) listener!!.onResult(false)
                        }
                    } else if (deviceName?.startsWith("M22_BT_") == true || deviceName?.startsWith("M21_BT_") == true) {
                        if (null != mPrinter && mPrinter!!.printerStatus == 0) {
                            //当前为已连接且正常状态状态并且需要打印，则直接打印，（若未连接就走下方的连接代码）
                            printYcPackageInfos(mListData)
                            return@Runnable
                        }

                        //关闭旧的打印机，换当前型号打印机情况
                        if (null != oldAYPinter) {
                            oldAYPinter!!.closeConnection()
                            oldAYPinter = null
                        }
                        try {
                            //关闭汉印打印机
                            Print.PortClose()
                            //关闭复坤打印机
                            closeFKPrinter(pointer)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        mPrinter = BluetoothPrinter(deviceAddress, 0)
                        oldAYPinter = mPrinter
                        //连接新的打印机
                        try {
                            mPrinter!!.currentPrintType = PrinterType.Printer_58
                            //set handler for receive message of connect state from sdk.
                            mPrinter!!.setHandler(bHandler)
                            mPrinter!!.encoding = "GBK"
                            mPrinter!!.setNeedVerify(false)
                            currentAddress = deviceAddress
                            mPrinter!!.openConnection()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hideWaitingDialog()
                            if (null != listener) listener!!.onResult(false)
                        }
                    } else if (deviceName?.startsWith("FK-") == true) {
                        var status = 0
                        if (null != pointer) status = AutoReplyPrint.INSTANCE.CP_Pos_QueryRTStatus(
                            pointer, 10000
                        )
                        if (303174162 == status) {
                            //当前为已连接且正常状态并且需要打印，则直接打印，（若未连接就走下方的连接代码）
                            printYcPackageInfos(mListData)
                            return@Runnable
                        }
                        try {
                            if (null != oldFKPinter) closeFKPrinter(oldFKPinter)
                            //关闭汉印打印机
                            Print.PortClose()
                            //关闭爱印打印机
                            if (null != mPrinter) {
                                mPrinter!!.closeConnection()
                                mPrinter = null
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            currentAddress = deviceAddress
                            AutoReplyPrint.INSTANCE.CP_Port_AddOnPortOpenedEvent(
                                opened_callback,
                                Pointer.NULL
                            )
                            AutoReplyPrint.INSTANCE.CP_Port_AddOnPortOpenFailedEvent(
                                openFailed_callback,
                                Pointer.NULL
                            )
                            AutoReplyPrint.INSTANCE.CP_Port_AddOnPortClosedEvent(
                                closed_callback,
                                Pointer.NULL
                            )
                            pointer = AutoReplyPrint.INSTANCE.CP_Port_OpenBtSpp(deviceAddress, 0)
                            oldFKPinter = pointer
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hideWaitingDialog()
                            if (null != listener) listener!!.onResult(false)
                        }
                    }
                }
            }).start()
        }

        private fun closeFKPrinter(pointer: Pointer?) {
            var pointer = pointer
            if (pointer !== Pointer.NULL) {
                AutoReplyPrint.INSTANCE.CP_Port_Close(pointer)
                pointer = Pointer.NULL
                pointer = null
            }
        }

        private fun showDeviceListDialog(printerDevices: List<DeviceBean?>?) {
            if (isCloseClass) {
                return
            }
            val dialog = PrintDeviceListDialog(activity, object : DeviceSelectListener {
                override fun onSelect(device: DeviceBean?) {
                    device?.let { connect(it) }
                }
            })
            dialog.show()
            dialog.setContent(printerDevices!!)
        }// 关于蓝牙设备分类参考 http://stackoverflow.com/q/23273355/4242112
        // 具体分类：https://blog.csdn.net/strivebus/article/details/65628628
        /**
         * 获取所有已配对的打印机设备
         */
        private val pairedDevices: List<DeviceBean?>
            private get() {
                val deviceList: MutableList<DeviceBean?> = ArrayList()
                val pairedDevices = BluetoothAdapter.getDefaultAdapter().bondedDevices
                if (pairedDevices.size > 0) {
                    for (device in pairedDevices) {
                        val klass = device.bluetoothClass
                        // 关于蓝牙设备分类参考 http://stackoverflow.com/q/23273355/4242112
                        // 具体分类：https://blog.csdn.net/strivebus/article/details/65628628
                        if (klass.majorDeviceClass == BluetoothClass.Device.Major.IMAGING) {
                            if (device.name.startsWith("M22_") || device.name.startsWith("M21_") || device.name.startsWith(
                                    "MPT-"
                                ) || device.name.startsWith("FK-")
                            ) {
                                for (i in 0..0) {
                                    val bean = DeviceBean()
                                    bean.deviceName = device.name
                                    bean.deviceAddress = device.address
                                    bean.setDeviceConnectStatus(BluetoothDevice.BOND_BONDED)
                                    deviceList.add(bean)
                                }
                            }
                        }
                    }
                }
                return deviceList
            }

        private fun printYcPackageInfos(list: List<OriginalDataBean>) {
            showWaitingDialog("正在打印...")
            val progress = intArrayOf(0)
            Thread {
                for (data in list) {
                    if (null == data) continue

                    //未连接打印机、打印失败场景结束循环
                    if (progress[0] == -1) break
                    when (currentPrintType) {
                        1 -> printYcPackageInfoByHYText(data, getListener(progress, list.size))
                        2 -> printYcPackageInfoByAYText(data, getListener(progress, list.size))
                        3 -> printYcPackageInfoByFKText(data, getListener(progress, list.size))
                        else -> checkOnFail("请先选择打印机", progress)
                    }
                }
            }.start()
        }

        private fun getListener(progress: IntArray, size: Int): OnPrintListener {
            return object : OnPrintListener {
                override fun onSuccess() {
                    checkOnSuccess(progress, size)
                }

                override fun onFail(msg: String) {
                    checkOnFail(msg, progress)
                }
            }
        }

        @Synchronized
        private fun checkOnSuccess(progress: IntArray, size: Int) {
            progress[0]++
            if (isCloseClass) {
                return
            }
            if (progress[0] == 1) {
                activity!!.runOnUiThread {
                    if (size == 1) {
                        hideWaitingDialog()
                        ToastUtil.shortMsg(activity, "打印成功！")
                        //回调给外部打印结果
                        if (null != listener) listener!!.onResult(true)
                    }
                }
            } else if (progress[0] == size) {
                activity!!.runOnUiThread {
                    hideWaitingDialog()
                    ToastUtil.shortMsg(activity, "全部打印成功！")
                    //回调给外部打印结果
                    if (null != listener) listener!!.onResult(true)
                }
            }
        }

        @Synchronized
        private fun checkOnFail(msg: String, progress: IntArray) {
            progress[0] = -1
            if (isCloseClass) return
            activity!!.runOnUiThread {
                hideWaitingDialog()
                if (!TextUtils.isEmpty(msg) && !isCloseClass) ToastUtil.shortMsg(activity, msg)

                //回调给外部打印结果
                if (null != listener) listener!!.onResult(false)
            }
        }

        @kotlin.Throws(IOException::class)
        private fun printRawBytes(bytes: ByteArray) {
            mOutputStream!!.write(bytes)
            mOutputStream.flush()
        }

        @kotlin.Throws(IOException::class)
        private fun printBitmap(bmp: Bitmap) {
            var bmp = bmp
            bmp = compressPic(bmp)
            val bmpByteArray = draw2PxPoint(bmp)
            printRawBytes(bmpByteArray)
        }

        /*************************************************************************
         * 假设一个360*360的图片，分辨率设为24, 共分15行打印 每一行,是一个 360 * 24 的点阵,y轴有24个点,存储在3个byte里面。
         * 即每个byte存储8个像素点信息。因为只有黑白两色，所以对应为1的位是黑色，对应为0的位是白色
         */
        private fun draw2PxPoint(bmp: Bitmap?): ByteArray {
            //先设置一个足够大的size，最后在用数组拷贝复制到一个精确大小的byte数组中
            val size = bmp!!.width * bmp.height / 8 + 1000
            val tmp = ByteArray(size)
            var k = 0
            // 设置行距为0
            tmp[k++] = 0x1B
            tmp[k++] = 0x33
            tmp[k++] = 0x00
            // 居中打印
            tmp[k++] = 0x1B
            tmp[k++] = 0x61
            tmp[k++] = 1
            var j = 0
            while (j < bmp.height / 24f) {
                tmp[k++] = 0x1B
                tmp[k++] = 0x2A // 0x1B 2A 表示图片打印指令
                tmp[k++] = 33 // m=33时，选择24点密度打印
                tmp[k++] = (bmp.width % 256).toByte() // nL
                tmp[k++] = (bmp.width / 256).toByte() // nH
                for (i in 0 until bmp.width) {
                    for (m in 0..2) {
                        for (n in 0..7) {
                            val b = px2Byte(i, j * 24 + m * 8 + n, bmp)
                            tmp[k] = tmp[k].plus(tmp[k] + b).toByte()
                        }
                        k++
                    }
                }
                tmp[k++] = 10 // 换行
                j++
            }
            // 恢复默认行距
            tmp[k++] = 0x1B
            tmp[k++] = 0x32
            val result = ByteArray(k)
            System.arraycopy(tmp, 0, result, 0, k)
            return result
        }

        /**
         * 图片二值化，黑色是1，白色是0
         *
         * @param x   横坐标
         * @param y   纵坐标
         * @param bit 位图
         * @return
         */
        private fun px2Byte(x: Int, y: Int, bit: Bitmap?): Byte {
            if (x < bit!!.width && y < bit.height) {
                val b: Byte
                val pixel = bit.getPixel(x, y)
                val red = pixel and 0x00ff0000 shr 16 // 取高两位
                val green = pixel and 0x0000ff00 shr 8 // 取中两位
                val blue = pixel and 0x000000ff // 取低两位
                val gray = RGB2Gray(red, green, blue)
                b = if (gray < 128) {
                    1
                } else {
                    0
                }
                return b
            }
            return 0
        }

        /**
         * 图片灰度的转化
         */
        private fun RGB2Gray(r: Int, g: Int, b: Int): Int {
            return (0.29900 * r + 0.58700 * g + 0.11400 * b).toInt() // 灰度转化公式
        }

        /**
         * 对图片进行压缩（去除透明度）
         *
         * @param bitmapOrg
         */
        private fun compressPic(bitmapOrg: Bitmap): Bitmap {
            // 获取这个图片的宽和高
            val width = bitmapOrg.width
            val height = bitmapOrg.height
            // 定义预转换成的图片的宽度和高度
            val newWidth = IMAGE_SIZE
            val newHeight = IMAGE_SIZE
            val targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
            val targetCanvas = Canvas(targetBmp)
            targetCanvas.drawColor(-0x1)
            targetCanvas.drawBitmap(
                bitmapOrg,
                Rect(0, 0, width, height),
                Rect(0, 0, newWidth, newHeight),
                null
            )
            return targetBmp
        }

        private fun printYcPackageInfoByHYText(
            data: OriginalDataBean?,
            printListener: OnPrintListener?,
        ) {
            if (null == data || !Print.IsOpened() || "18" != isConnect) {

                //断开清掉记录
                if (TextUtils.isEmpty(isConnect)) currentPrintAddress = null
                printListener?.onFail(if (TextUtils.isEmpty(isConnect)) "打印机连接已断开！" else "打印机状态异常！")
                showConnectFailDialog()
                return
            }
            try {
                printPackageInfoByHY(data.printLineInfoBeanList)
                printListener?.onSuccess()
            } catch (e: Exception) {
                val msg = getExceptionMsg(e)
                printListener?.onFail("打印报错了：$msg")
            }
        }

        private fun printYcPackageInfoByAYText(
            data: OriginalDataBean?,
            printListener: OnPrintListener?,
        ) {
            val mPrinter = mPrinter
            if (null == data || null == mPrinter || mPrinter.printerStatus != 0) {

                //断开清掉记录
                if (16 == mPrinter!!.printerStatus) currentPrintAddress = null
                printListener?.onFail(if (16 == mPrinter.printerStatus) "打印机连接已断开！" else "打印机状态异常！")
                showConnectFailDialog()
                return
            }
            try {
                printPackageInfoByAY(data.printLineInfoBeanList, mPrinter)
                printListener?.onSuccess()
            } catch (e: Exception) {
                val msg = getExceptionMsg(e)
                printListener?.onFail("打印报错了：$msg")
            }
        }

        private fun printYcPackageInfoByFKText(
            data: OriginalDataBean?,
            printListener: OnPrintListener?,
        ) {
            //连接成功后，这里获取的状态稍微等一下，不然获取的状态很有可能还是没连接
            SystemClock.sleep(200)
            val pointer = pointer
            var status = 0
            if (null != pointer) status =
                AutoReplyPrint.INSTANCE.CP_Pos_QueryRTStatus(pointer, 10000)
            if (null == data || 303174162 != status) {

                //断开清掉记录
                if (0 == status) currentPrintAddress = null
                printListener?.onFail(if (0 == status) "打印机连接已断开！" else "打印机状态异常！")
                showConnectFailDialog()
                return
            }
            try {
                printPackageInfoByFK(data.printLineInfoBeanList, pointer)
                printListener?.onSuccess()
            } catch (e: Exception) {
                val msg = getExceptionMsg(e)
                printListener?.onFail("打印报错了：$msg")
            }
        }

        //解析查询错误日志
        private fun getExceptionMsg(e: Exception): String? {
            var msg = if (TextUtils.isEmpty(e.message)) e.localizedMessage else e.message
            if (TextUtils.isEmpty(msg)) {
                var cause = e.cause
                if (null != cause) {
                    msg = cause.message
                    while (null == msg && cause!!.cause != null) {
                        cause = cause.cause
                        if (!TextUtils.isEmpty(cause!!.message)) {
                            msg = cause.message
                        }
                    }
                }
            }
            return msg
        }

        private val isConnect: String
            private get() {
                var status = ""
                try {
                    val statusData = HPRTPrinterHelper.GetRealTimeStatus(
                        HPRTPrinterHelper.PRINTER_REAL_TIME_STATUS_ITEM_PRINTER.toByte()
                    )
                    for (statusDatum in statusData) {
                        status += statusDatum
                    }
                } catch (e: Exception) {
                    Log.d(
                        "HPRTSDKSample", StringBuilder("Activity_Status --> Refresh ")
                            .append(e.message).toString()
                    )
                }
                return status
            }

        /**
         * 生成简单二维码
         *
         * @param content                字符串内容
         * @param width                  二维码宽度
         * @param height                 二维码高度
         * @param character_set          编码方式（一般使用UTF-8）
         * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
         * @param margin                 空白边距（二维码与边框的空白区域）
         * @param color_black            黑色色块
         * @param color_white            白色色块
         * @return BitMap
         */
        @JvmStatic
        fun createQRCodeBitmap(
            content: String?, width: Int, height: Int,
            character_set: String?, error_correction_level: String?,
            margin: String?, color_black: Int, color_white: Int,
        ): Bitmap? {
            // 字符串内容判空
            if (TextUtils.isEmpty(content)) {
                return null
            }
            // 宽和高>=0
            return if (width < 0 || height < 0) {
                null
            } else try {
                /** 1.设置二维码相关配置  */
                val hints = Hashtable<EncodeHintType, String?>()
                // 字符转码格式设置
                if (!TextUtils.isEmpty(character_set)) {
                    hints.put(EncodeHintType.CHARACTER_SET, character_set)
                }
                // 容错率设置
                if (!TextUtils.isEmpty(error_correction_level)) {
                    hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level)
                }
                // 空白边距设置
                if (!TextUtils.isEmpty(margin)) {
                    hints.put(EncodeHintType.MARGIN, margin)
                }
                /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象  */
                val bitMatrix =
                    QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

                /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值  */
                val pixels = IntArray(width * height)
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                        if (bitMatrix[x, y]) {
                            pixels[y * width + x] = color_black //黑色色块像素设置
                        } else {
                            pixels[y * width + x] = color_white // 白色色块像素设置
                        }
                    }
                }
                /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象  */
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                bitmap
            } catch (e: WriterException) {
                e.printStackTrace()
                null
            }
        }

        private val barcodeFormat = BarcodeFormat.CODE_128
        private fun createBarcode(contents: String, desiredWidth: Int, desiredHeight: Int): Bitmap {
            val writer = MultiFormatWriter()
            var result: BitMatrix? = null
            try {
                result = writer.encode(
                    contents, barcodeFormat, desiredWidth,
                    desiredHeight
                )
            } catch (e: WriterException) {
                e.printStackTrace()
            }
            val width = result!!.width
            val height = result.height
            val pixels = IntArray(width * height)
            // All are 0, or black, by default
            for (y in 0 until height) {
                val offset: Int = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (result[x, y]) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        }

        private val opened_callback = CP_OnPortOpenedEvent_Callback { handle, name, private_data ->
            if (isCloseClass) {
                return@CP_OnPortOpenedEvent_Callback
            }
            //连接成功
            currentPrintType = 3
            currentPrintAddress = currentAddress
            try {
                printYcPackageInfos(mListData)
            } catch (e: Exception) {
                e.printStackTrace()
                hideWaitingDialog()
                if (null != listener) listener!!.onResult(false)
            }
        }
        private val openFailed_callback =
            CP_OnPortOpenFailedEvent_Callback { _, _, _ ->
                if (!isCloseClass) activity!!.runOnUiThread {
                    hideWaitingDialog()
                    showConnectFailDialog()
                }
            }
        private val closed_callback = CP_OnPortClosedEvent_Callback { h, _ -> //监听到连接关闭
            closeFKPrinter(h)
        }
        private val bHandler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (isCloseClass) {
                    return
                }
                when (msg.what) {
                    BluetoothPrinter.Handler_Connect_Connecting ->                     //正在连接打印机
                        showWaitingDialog("打印机连接中...")
                    BluetoothPrinter.Handler_Connect_Success -> {
                        //连接成功
                        currentPrintType = 2
                        currentPrintAddress = currentAddress
                        try {
                            hideWaitingDialog()
                            printYcPackageInfos(mListData)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (null != listener) listener!!.onResult(false)
                        }
                    }
                    BluetoothPrinter.Handler_Connect_Failed -> {
                        //连接失败
                        hideWaitingDialog()
                        activity!!.runOnUiThread { showConnectFailDialog() }
                    }
                    BluetoothPrinter.Handler_Message_Read, BluetoothPrinter.Handler_Connect_Closed -> {
                    }
                }
            }
        }

        private fun showConnectFailDialog() {
            if (!isCloseClass) activity!!.runOnUiThread(
                Runnable {
                    hideWaitingDialog()
                    if (isCloseClass) {
                        return@Runnable
                    }
                    CommonDialog.Builder(activity!!)
                        .setTitle("温馨提示：")
                        .setMessage("打印机状态异常或未开启,请确认开启后,重新打印")
                        .setOnConfirmClickListener("取消") {
                            connectAndPrint()
                        }
                        .setOnCancelClickListener("已开启") {
                            if (null != listener)
                                listener!!.onResult(false)
                        }
                        .setContentType(1)
                        .build()
                        .shown()
                })
        }

        private val isCloseClass: Boolean
            private get() = activity == null || activity!!.isDestroyed || activity!!.isFinishing

        private var waitingDialog: WaitingDialog? = null
        private fun showWaitingDialog(msg: String) {
            if (isCloseClass) {
                return
            }
            if (waitingDialog == null) {
                waitingDialog = WaitingDialog(activity)
            }
            activity!!.runOnUiThread {
                waitingDialog!!.show()
                waitingDialog!!.setMessage(msg)
            }
        }

        private fun hideWaitingDialog() {
            if (isCloseClass) {
                return
            }
            if (waitingDialog != null) {
                waitingDialog!!.dismiss()
            }
        }

        /**
         * 汉印小票打印
         *
         * @param list 打印信息集合
         */
        @kotlin.Throws(Exception::class)
        private fun printPackageInfoByHY(list: List<PrintLineInfoBean>?) {
            val print = ReceiptPrintUtil()
            for (infoBean in list!!) {
                print.setFontSizeCmd(infoBean.fontSize)
                print.setFontBoldCmd(infoBean.fontBold)
                print.setAlignCmd(infoBean.fontAlign)
                when (infoBean.contentType) {
                    PrintLineInfoBean.CONTENT_TXT -> if (infoBean.columnNum == PrintLineInfoBean.ONE_COLUMN) {
                        if (infoBean.fontAlign == PrintLineInfoBean.ALIGN_LEFT) {
                            infoBean.contentLeft?.let { print.printText(it) }
                        } else if (infoBean.fontAlign == PrintLineInfoBean.ALIGN_CENTER) {
                            infoBean.contentCenter?.let { print.printText(it) }
                        } else {
                            infoBean.contentRight?.let { print.printText(it) }
                        }
                    } else if (infoBean.columnNum == PrintLineInfoBean.TWO_COLUMN) {
                        print.printText(
                            print.printTwoColumn(
                                infoBean.contentLeft,
                                infoBean.contentRight
                            )
                        )
                    } else print.printText(
                        print.printThreeColumn(
                            infoBean.contentLeft,
                            infoBean.contentCenter,
                            infoBean.contentRight
                        )
                    )
                    PrintLineInfoBean.CONTENT_BITMAP, PrintLineInfoBean.CONTENT_BARCODE -> Print.PrintBitmap(
                        infoBean.hyBitmap,
                        0,
                        0
                    )
                    PrintLineInfoBean.CONTENT_DOTTED_LINE -> print.printDashLine()
                    PrintLineInfoBean.CONTENT_NEW_LINE -> print.printLine()
                    PrintLineInfoBean.CONTENT_BLANK_LINE -> Print.PrintAndFeedNLine(infoBean.printAndFeedNLine)
                }
            }
            print.printLine()
            Print.PrintAndFeed(100)
        }

        /**
         * 爱印小票打印
         *
         * @param list 打印信息集合
         */
        @kotlin.Throws(Exception::class)
        private fun printPackageInfoByAY(
            list: List<PrintLineInfoBean>?,
            mPrinter: BluetoothPrinter,
        ) {
            val print = ReceiptPrintUtil()
            mPrinter.init()
            for (infoBean in list!!) {
                print.setFontSizeCmd(infoBean.fontSize)
                mPrinter.setPrinter(BluetoothPrinter.COMM_ALIGN, infoBean.fontAlign)
                if (infoBean.ayFontBold == PrintLineInfoBean.FONT_BOLD) {
                    mPrinter.setCharacterMultiple(1, 1)
                } else {
                    mPrinter.setCharacterMultiple(0, 0)
                }
                when (infoBean.ayFontSize) {
                    PrintLineInfoBean.FONT_NORMAL -> {
                        val cmd1 = byteArrayOf(27, 128.toByte(), 0)
                        mPrinter.printByteData(cmd1)
                    }
                    PrintLineInfoBean.FONT_MIDDLE -> {
                        val cmd5 = byteArrayOf(27, 80, 1)
                        mPrinter.printByteData(cmd5)
                    }
                    PrintLineInfoBean.FONT_MIDDLE_ -> {
                        //16点阵
                        val cmd = byteArrayOf(27, 128.toByte(), 1)
                        mPrinter.printByteData(cmd)
                        //字体加大一倍，由16点阵变为32点阵 （字体大中小对应为 24点阵、32点阵、48点阵）
                        mPrinter.setCharacterMultiple(1, 1)
                        //加粗
                        val cmd4 = byteArrayOf(27, 69, 1)
                        mPrinter.printByteData(cmd4)
                    }
                    PrintLineInfoBean.FONT_MIDDLE_2 -> {
                        //16点阵
                        val cmd2 = byteArrayOf(27, 128.toByte(), 1)
                        mPrinter.printByteData(cmd2)
                        //字体加大一倍，由16点阵变为32点阵 （字体大中小对应为 24点阵、32点阵、48点阵）
                        mPrinter.setCharacterMultiple(1, 1)
                        //取消加粗
                        val cmd3 = byteArrayOf(27, 69, 0)
                        mPrinter.printByteData(cmd3)
                    }
                    PrintLineInfoBean.FONT_BIG -> {
                        val cmd6 = byteArrayOf(27, 60, 1)
                        mPrinter.printByteData(cmd6)
                    }
                }
                when (infoBean.contentType) {
                    PrintLineInfoBean.CONTENT_TXT -> when (infoBean.columnNum) {
                        PrintLineInfoBean.ONE_COLUMN -> {
                            when (infoBean.fontAlign) {
                                0 -> {
                                    mPrinter.printText(infoBean.contentLeft)
                                }
                                PrintLineInfoBean.ALIGN_CENTER -> {
                                    mPrinter.printText(infoBean.contentCenter)
                                }
                                else -> {
                                    mPrinter.printText(infoBean.contentRight)
                                }
                            }
                        }
                        PrintLineInfoBean.TWO_COLUMN -> {
                            mPrinter.printByteData(
                                print.printTwoColumn(
                                    infoBean.contentLeft,
                                    infoBean.contentRight
                                )
                            )
                        }
                        else -> mPrinter.printByteData(
                            print.printThreeColumn(
                                infoBean.contentLeft,
                                infoBean.contentCenter,
                                infoBean.contentRight
                            )
                        )
                    }
                    PrintLineInfoBean.CONTENT_BITMAP -> {
                        mPrinter.setPrinter(
                            BluetoothPrinter.COMM_ALIGN,
                            BluetoothPrinter.COMM_ALIGN_CENTER
                        )
                        val qrCode = Barcode(
                            BluetoothPrinter.BAR_CODE_TYPE_QRCODE,
                            infoBean.param1,
                            infoBean.param2,
                            infoBean.param3,
                            infoBean.ayCodeUrl
                        )
                        mPrinter.printBarCode(qrCode)
                    }
                    PrintLineInfoBean.CONTENT_BARCODE -> {
                        // 根据字符串生成条形码图片并显示在界面上
                        mPrinter.setPrinter(BluetoothPrinter.COMM_ALIGN,
                            BluetoothPrinter.COMM_ALIGN_CENTER)
                        val barcode = Barcode(BluetoothPrinter.BAR_CODE_TYPE_CODE128,
                            infoBean.param1,
                            infoBean.param2,
                            infoBean.param3,
                            infoBean.ayCodeUrl)
                        mPrinter.printBarCode(barcode)
                    }
                    PrintLineInfoBean.CONTENT_DOTTED_LINE -> {
                        mPrinter.setCharacterMultiple(0, 0)
                        mPrinter.printText("--------------------------------")
                    }
                    PrintLineInfoBean.CONTENT_NEW_LINE -> mPrinter.printText("\n")
                }
            }
            mPrinter.printText("\n")
            mPrinter.cutPaper()
        }

        /**
         * 复坤小票打印
         *
         * @param list 打印信息集合
         */
        @kotlin.Throws(Exception::class)
        private fun printPackageInfoByFK(list: List<PrintLineInfoBean>?, pointer: Pointer?) {
            val print = ReceiptPrintUtil()
            AutoReplyPrint.INSTANCE.CP_Pos_ResetPrinter(pointer)
            AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteMode(pointer)
            for (infoBean in list!!) {
                print.setFontSizeCmd(infoBean.fontSize)
                AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteEncoding(
                    pointer,
                    infoBean.multiByteEncoding
                )
                if (infoBean.fontBold == PrintLineInfoBean.FONT_BOLD) {
                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 1)
                } else {
                    AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 0)
                }
                when (infoBean.fontSize) {
                    PrintLineInfoBean.FONT_NORMAL -> AutoReplyPrint.INSTANCE.CP_Pos_SetTextScale(
                        pointer,
                        0,
                        0
                    )
                    PrintLineInfoBean.FONT_MIDDLE -> {
                        //中号字体
                        val cmd = byteArrayOf(0x1d.toByte(), 0x21.toByte(), 0x01.toByte())
                        AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, cmd, cmd.size, 10000)
                    }
                    PrintLineInfoBean.FONT_BIG -> AutoReplyPrint.INSTANCE.CP_Pos_SetTextScale(
                        pointer,
                        1,
                        1
                    )
                }
                when (infoBean.contentType) {
                    PrintLineInfoBean.CONTENT_TXT -> if (infoBean.columnNum == PrintLineInfoBean.ONE_COLUMN) {
                        if (infoBean.fontAlign == PrintLineInfoBean.ALIGN_LEFT) {
                            AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(
                                pointer,
                                AutoReplyPrint.CP_Pos_Alignment_Left
                            )
                            AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, infoBean.contentLeft)
                        } else if (infoBean.fontAlign == PrintLineInfoBean.ALIGN_CENTER) {
                            AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(
                                pointer,
                                AutoReplyPrint.CP_Pos_Alignment_HCenter
                            )
                            AutoReplyPrint.INSTANCE.CP_Pos_PrintText(
                                pointer,
                                infoBean.contentCenter
                            )
                        } else {
                            AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(
                                pointer,
                                AutoReplyPrint.CP_Pos_Alignment_Right
                            )
                            AutoReplyPrint.INSTANCE.CP_Pos_PrintText(pointer, infoBean.contentRight)
                        }
                    } else if (infoBean.columnNum == PrintLineInfoBean.TWO_COLUMN) {
                        val bytes4 =
                            print.printTwoColumn(infoBean.contentLeft, infoBean.contentRight)
                        AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes4, bytes4.size, 10000)
                    } else {
                        val bytes = print.printThreeColumn(
                            infoBean.contentLeft,
                            infoBean.contentCenter,
                            infoBean.contentRight
                        )
                        AutoReplyPrint.INSTANCE.CP_Port_Write(pointer, bytes, bytes.size, 10000)
                    }
                    PrintLineInfoBean.CONTENT_BITMAP -> {
                        AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 1)
                        AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(
                            pointer,
                            AutoReplyPrint.CP_Pos_Alignment_HCenter
                        )
                        val bitmap = infoBean.fkBitmap
                        AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
                            pointer,
                            bitmap!!.width,
                            bitmap.height,
                            bitmap,
                            AutoReplyPrint.CP_ImageBinarizationMethod_ErrorDiffusion,
                            AutoReplyPrint.CP_ImageCompressionMethod_Level1
                        )

                        AutoReplyPrint.INSTANCE.CP_Pos_SetTextScale(pointer, 0, 0)
                        AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 1)
                    }
                    PrintLineInfoBean.CONTENT_BARCODE -> {
                        AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 1)
                        AutoReplyPrint.INSTANCE.CP_Pos_SetAlignment(
                            pointer,
                            AutoReplyPrint.CP_Pos_Alignment_HCenter
                        )
                        val bitmap = infoBean.fkBitmap
                        AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
                            pointer,
                            bitmap!!.width,
                            bitmap.height,
                            bitmap,
                            AutoReplyPrint.CP_ImageBinarizationMethod_ErrorDiffusion,
                            AutoReplyPrint.CP_ImageCompressionMethod_Level1
                        )

                        AutoReplyPrint.INSTANCE.CP_Pos_SetTextScale(pointer, 0, 0)
                    }
                    PrintLineInfoBean.CONTENT_DOTTED_LINE -> {
                        AutoReplyPrint.INSTANCE.CP_Pos_SetTextBold(pointer, 0)
                        AutoReplyPrint.INSTANCE.CP_Pos_PrintText(
                            pointer,
                            "--------------------------------"
                        )
                    }
                    PrintLineInfoBean.CONTENT_NEW_LINE -> {
                        AutoReplyPrint.INSTANCE.CP_Pos_SetTextScale(pointer, 0, 0)
                        AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, infoBean.feedLine)
                    }
                }
            }
            AutoReplyPrint.INSTANCE.CP_Pos_FeedLine(pointer, 4)
        }

        /**
         * Drawable转换成一个Bitmap
         *
         * @param drawable drawable对象
         * @return
         */
        @JvmStatic
        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            return bitmap
        }

        @JvmStatic
        fun addBackgroundSynthesis(bg: Bitmap, icon: Bitmap): Bitmap? {
            val paint = Paint()
            val canvas = Canvas(bg)
            val b1h = bg.height
            val b2w = icon.width
            val b2h = icon.height
            val bx = (WIDTH_PIXEL - b2w) / 2
            val by = (b1h - b2h) / 2
            canvas.drawBitmap(icon, bx.toFloat(), by.toFloat(), paint)
            canvas.save()
            canvas.restore()
            return bg
        }
    }
}