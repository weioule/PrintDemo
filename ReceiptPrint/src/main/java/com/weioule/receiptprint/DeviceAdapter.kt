package com.weioule.receiptprint

import android.bluetooth.BluetoothDevice
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.weioule.receiptprint.bean.DeviceBean

/**
 * Author by weioule.
 * Date on 2022/09/10.
 */
class DeviceAdapter(list: List<DeviceBean?>?) :
    BaseQuickAdapter<DeviceBean, BaseViewHolder>(R.layout.device_item, list) {
    override fun convert(helper: BaseViewHolder, item: DeviceBean) {
        helper.setText(R.id.tv_device_name, item.deviceName)
        helper.setText(R.id.tv_device_address, item.deviceAddress)
        if (item.deviceStatus == BluetoothDevice.BOND_NONE) {
            helper.setText(R.id.tv_device_status, "未配对")
        }
        if (item.deviceStatus == BluetoothDevice.BOND_BONDED) {
            helper.setText(R.id.tv_device_status, "已配对")
        }
        if (item.deviceStatus == 14) {
            helper.setText(R.id.tv_device_status, "已连接")
        }
    }
}