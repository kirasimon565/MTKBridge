package com.blackmoon.mtkbridge

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var logText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        startButton = findViewById(R.id.startButton)
        logText = findViewById(R.id.logText)

        startButton.setOnClickListener {
            startBridgeService()
        }
    }

    private fun startBridgeService() {
        try {
            log("Attempting to start UsbBridgeService...")
            val serviceIntent = Intent(this, UsbBridgeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            statusText.text = "Server Status: Started"
            log("Service started successfully.")
        } catch (e: Exception) {
            statusText.text = "Server Status: Error starting"
            log("Error starting service: ${e.message}")
            log(getStackTrace(e))
        }
    }

    private fun log(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val currentLog = logText.text.toString()
        val newLog = "[$time] $message\n"
        logText.text = currentLog + newLog
    }

    private fun getStackTrace(e: Exception): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        return sw.toString()
    }
}
