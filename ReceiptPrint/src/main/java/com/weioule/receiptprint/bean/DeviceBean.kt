package com.weioule.receiptprint.bean

/**
 * Author by weioule.
 * Date on 2022/09/10.
 */
class DeviceBean {
    var deviceName: String? = null
    var deviceAddress: String? = null
    var deviceStatus = 0

    constructor() {}
    constructor(deviceName: String?, deviceAddress: String?, deviceStatus: Int) {
        this.deviceName = deviceName
        this.deviceAddress = deviceAddress
        this.deviceStatus = deviceStatus
    }

    fun setDeviceConnectStatus(deviceStatus: Int) {
        this.deviceStatus = deviceStatus
    }
}