package com.blackmoon.mtkbridge

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log

class AndroidUsbConnection(private val context: Context) {
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private var usbDevice: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var endpointIn: UsbEndpoint? = null
    private var endpointOut: UsbEndpoint? = null

    companion object {
        const val MTK_VENDOR_ID = 0x0E8D
        private const val ACTION_USB_PERMISSION = "com.blackmoon.mtkbridge.USB_PERMISSION"
        private const val TAG = "AndroidUsbConnection"
    }

    fun discoverDevice(): MtkUsbDevice? {
        val deviceList = usbManager.deviceList
        for (device in deviceList.values) {
            if (device.vendorId == MTK_VENDOR_ID) {
                usbDevice = device
                return MtkUsbDevice(device)
            }
        }
        return null
    }

    fun hasPermission(): Boolean {
        return usbDevice?.let { usbManager.hasPermission(it) } ?: false
    }

    fun requestPermission() {
        usbDevice?.let {
            if (!usbManager.hasPermission(it)) {
                val permissionIntent = PendingIntent.getBroadcast(
                    context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE
                )
                usbManager.requestPermission(it, permissionIntent)
            }
        }
    }

    fun open(): Boolean {
        val device = usbDevice ?: return false
        if (!usbManager.hasPermission(device)) return false

        connection = usbManager.openDevice(device) ?: return false

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            connection?.claimInterface(iface, true)

            var epIn: UsbEndpoint? = null
            var epOut: UsbEndpoint? = null

            for (j in 0 until iface.endpointCount) {
                val endpoint = iface.getEndpoint(j)
                if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (endpoint.direction == UsbConstants.USB_DIR_IN) {
                        epIn = endpoint
                    } else if (endpoint.direction == UsbConstants.USB_DIR_OUT) {
                        epOut = endpoint
                    }
                }
            }

            if (epIn != null && epOut != null) {
                usbInterface = iface
                endpointIn = epIn
                endpointOut = epOut
                return true
            } else {
                connection?.releaseInterface(iface)
            }
        }

        close()
        return false
    }

    fun write(data: ByteArray, timeout: Int = 1000): Int {
        val conn = connection ?: return -1
        val epOut = endpointOut ?: return -1
        return conn.bulkTransfer(epOut, data, data.size, timeout)
    }

    fun read(length: Int, timeout: Int = 1000): ByteArray? {
        val conn = connection ?: return null
        val epIn = endpointIn ?: return null
        val buffer = ByteArray(length)
        val bytesRead = conn.bulkTransfer(epIn, buffer, buffer.size, timeout)

        if (bytesRead > 0) {
            return buffer.copyOf(bytesRead)
        }
        return null
    }

    fun close() {
        usbInterface?.let {
            connection?.releaseInterface(it)
        }
        connection?.close()
        connection = null
        usbInterface = null
        endpointIn = null
        endpointOut = null
    }
}
