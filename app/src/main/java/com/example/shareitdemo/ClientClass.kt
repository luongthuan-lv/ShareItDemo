package com.example.shareitdemo

import android.os.Handler
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Created by Luong Thuan on 21/03/2023.
 */
class ClientClass(hostAddress: InetAddress, var activity: MainActivity) {
    val hostAdd: String = hostAddress.hostAddress as String
    var clientSocket: Socket? = null
    var sendReceive: SendReceive? = null

    fun startClient(handler: Handler) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    clientSocket = Socket()
                    clientSocket?.connect(InetSocketAddress(hostAdd, 8888), 10000)

                    sendReceive = SendReceive(clientSocket!!, handler)
                    Log.e("wifidirectdemo", sendReceive.toString())
                    sendReceive!!.runSendReceive(activity)
                    //sendReceive!!.runSendReceive()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }
    }
}