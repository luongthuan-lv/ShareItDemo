package com.example.shareitdemo

import android.net.Uri
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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

                    if (clientSocket == null){
                        clientSocket = Socket()
                        clientSocket?.bind(null)
                        clientSocket?.connect(InetSocketAddress(hostAdd, 8888), 10000)
                        Log.e("wifidirectdemo", "KHỞI TẠO")
                    }else{
                        Log.e("wifidirectdemo", "KHÔNG KHỞI TẠO")
                    }


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

    fun startClient(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    clientSocket = Socket()
                    clientSocket?.bind(null)
                    clientSocket?.connect(InetSocketAddress(hostAdd, 8888), 10000)

                    copyFile(
                        activity.contentResolver.openInputStream(uri)!!,
                        clientSocket?.getOutputStream()!!
                    )

                    //clientSocket!!.close()

//                    sendReceive = SendReceive(clientSocket!!, handler)
//                    Log.e("wifidirectdemo", sendReceive.toString())
//                    sendReceive!!.runSendReceive(activity)
                    //sendReceive!!.runSendReceive()
                } catch (e: IOException) {
                    Log.e("wifidirectdemo", "CRASH 1 $e")
                    e.printStackTrace()
                } finally {
                    if (clientSocket != null) {
                        if (clientSocket!!.isConnected()) {
                            try {
                                clientSocket!!.close();
                            } catch (e: IOException) {
                                // Give up
                                Log.e("wifidirectdemo", "CRASH 2")
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }
    }

    fun startClientNew(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
//                try {
//                    clientSocket = Socket()
//                    clientSocket?.bind(null)
                    clientSocket?.connect(InetSocketAddress(hostAdd, 8888), 10000)

                    copyFile(
                        activity.contentResolver.openInputStream(uri)!!,
                        clientSocket?.getOutputStream()!!
                    )

                    //clientSocket!!.close()

//                    sendReceive = SendReceive(clientSocket!!, handler)
//                    Log.e("wifidirectdemo", sendReceive.toString())
//                    sendReceive!!.runSendReceive(activity)
                    //sendReceive!!.runSendReceive()
//                } catch (e: IOException) {
//                    Log.e("wifidirectdemo", "CRASH 1 $e")
//                    e.printStackTrace()
//                } finally {
//                    if (clientSocket != null) {
//                        if (clientSocket!!.isConnected()) {
//                            try {
//                                clientSocket!!.close();
//                            } catch (e: IOException) {
//                                // Give up
//                                Log.e("wifidirectdemo", "CRASH 2")
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
            }

        }
    }

    fun copyFile(inputStream: InputStream, out: OutputStream): Boolean {

        val buf = ByteArray(1024)
        var len: Int
        try {
            Log.e("wifidirectdemo", "SEND FILE 00 ")
            while (inputStream.read(buf).also { len = it } != -1) {
                out.write(buf, 0, len)
            }

            Log.e("wifidirectdemo", "SEND FILE 22 ")

//            out.close()
//            inputStream.close()
//            Log.e("wifidirectdemo", "close input and output + check")


        } catch (e: IOException) {
            Log.e("wifidirectdemo", "SEND FILE 33 ")
            Log.d("wifidirectdemo", e.toString())
            return false
        }
        return true
    }
}