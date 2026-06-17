package com.blackmoon.mtkbridge

import android.content.Context

class UsbBridgeConnection(context: Context) {
    val androidUsbConnection = AndroidUsbConnection(context)

    fun discoverDevice(): MtkUsbDevice? {
        return androidUsbConnection.discoverDevice()
    }

    fun hasPermission(): Boolean {
        return androidUsbConnection.hasPermission()
    }

    fun requestPermission() {
        androidUsbConnection.requestPermission()
    }

    fun open(): Boolean {
        return androidUsbConnection.open()
    }

    fun close() {
        androidUsbConnection.close()
    }

    fun write(data: ByteArray): Int {
        return androidUsbConnection.write(data)
    }

    fun read(length: Int): ByteArray? {
        return androidUsbConnection.read(length)
    }
}
