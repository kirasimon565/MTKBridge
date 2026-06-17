package com.blackmoon.mtkbridge

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, UsbBridgeService::class.java))
        finish()
    }
}
