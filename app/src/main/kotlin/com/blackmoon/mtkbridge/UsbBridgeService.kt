package com.blackmoon.mtkbridge

import android.app.Service
import android.content.Intent
import android.os.IBinder

class UsbBridgeService : Service() {

    private lateinit var usbBridgeServer: UsbBridgeServer

    override fun onCreate() {
        super.onCreate()
        usbBridgeServer = UsbBridgeServer(this)
        usbBridgeServer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        usbBridgeServer.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
