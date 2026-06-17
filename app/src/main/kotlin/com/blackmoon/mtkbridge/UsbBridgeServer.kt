package com.blackmoon.mtkbridge

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class UsbBridgeServer(private val context: Context) {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var serverJob: Job? = null
    private val usbConnection = UsbBridgeConnection(context)

    companion object {
        private const val TAG = "UsbBridgeServer"
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        serverJob = GlobalScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(SocketProtocol.PORT)
                Log.d(TAG, "Server started on port ${SocketProtocol.PORT}")
                while (isActive && isRunning) {
                    val client = serverSocket?.accept()
                    client?.let {
                        handleClient(it)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server error", e)
            } finally {
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
        serverJob?.cancel()
        serverSocket?.close()
        usbConnection.close()
    }

    private suspend fun handleClient(client: Socket) {
        withContext(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val writer = PrintWriter(client.getOutputStream(), true)

                while (isActive && isRunning && !client.isClosed) {
                    val line = reader.readLine() ?: break
                    processCommand(line, writer)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Client error", e)
            } finally {
                client.close()
            }
        }
    }

    private fun processCommand(commandLine: String, writer: PrintWriter) {
        val parts = commandLine.trim().split("\\s+".toRegex())
        if (parts.isEmpty()) return

        val cmd = parts[0].uppercase()
        when (cmd) {
            SocketProtocol.CMD_LIST -> {
                val device = usbConnection.discoverDevice()
                if (device != null) {
                    writer.println("OK MTK_DEVICE")
                } else {
                    writer.println("ERROR NO_DEVICE")
                }
            }
            SocketProtocol.CMD_OPEN -> {
                val device = usbConnection.discoverDevice()
                if (device == null) {
                    writer.println("ERROR NO_DEVICE")
                    return
                }
                if (!usbConnection.hasPermission()) {
                    writer.println("ERROR NO_PERMISSION")
                    return
                }
                val success = usbConnection.open()
                if (success) {
                    writer.println("OK")
                } else {
                    writer.println("ERROR OPEN_FAILED")
                }
            }
            SocketProtocol.CMD_CLOSE -> {
                usbConnection.close()
                writer.println("OK")
            }
            SocketProtocol.CMD_WRITE -> {
                if (parts.size < 2) {
                    writer.println("ERROR INVALID_ARGUMENT")
                    return
                }
                val hexString = parts[1]
                val bytes = try {
                    hexStringToByteArray(hexString)
                } catch (e: Exception) {
                    writer.println("ERROR INVALID_HEX")
                    return
                }
                val written = usbConnection.write(bytes)
                if (written >= 0) {
                    writer.println("OK $written")
                } else {
                    writer.println("ERROR WRITE_FAILED")
                }
            }
            SocketProtocol.CMD_READ -> {
                if (parts.size < 2) {
                    writer.println("ERROR INVALID_ARGUMENT")
                    return
                }
                val length = try {
                    parts[1].toInt()
                } catch (e: Exception) {
                    writer.println("ERROR INVALID_LENGTH")
                    return
                }
                val readData = usbConnection.read(length)
                if (readData != null) {
                    val hexStr = byteArrayToHexString(readData)
                    writer.println(hexStr)
                } else {
                    writer.println("ERROR READ_FAILED")
                }
            }
            else -> {
                writer.println("ERROR UNKNOWN_COMMAND")
            }
        }
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        require(len % 2 == 0) { "Hex string must have an even length" }
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }

    private fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = java.lang.StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b.toInt() and 0xFF))
        }
        return sb.toString()
    }
}
