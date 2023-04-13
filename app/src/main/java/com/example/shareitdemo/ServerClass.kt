package com.example.shareitdemo

import android.os.Handler
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

/**
 * Created by Luong Thuan on 21/03/2023.
 */
class ServerClass(var activity: MainActivity) {
    var serverSocket: ServerSocket? = null
    var socket: Socket? = null
    var sendReceive: SendReceive? = null
    val MESSAGE_READ = 1


    fun startServer(handler: Handler) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    serverSocket = ServerSocket(8888)
                    socket = serverSocket!!.accept()

                    sendReceive = SendReceive(socket!!, handler)
                    Log.e("wifidirectdemo", sendReceive.toString())
                    sendReceive!!.runSendReceive(activity)
                    //sendReceive!!.runSendReceive()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }
    }

    fun stopServer() {
        socket?.close()
        serverSocket?.close()
    }
}