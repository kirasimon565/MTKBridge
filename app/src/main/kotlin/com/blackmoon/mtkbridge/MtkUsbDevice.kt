package com.blackmoon.mtkbridge

import android.hardware.usb.UsbDevice

class MtkUsbDevice(val usbDevice: UsbDevice) {
    val vendorId: Int
        get() = usbDevice.vendorId

    val productId: Int
        get() = usbDevice.productId

    val manufacturerName: String?
        get() = usbDevice.manufacturerName

    val productName: String?
        get() = usbDevice.productName

    override fun toString(): String {
        return "MtkUsbDevice(vendorId=$vendorId, productId=$productId, manufacturerName=$manufacturerName, productName=$productName)"
    }
}
