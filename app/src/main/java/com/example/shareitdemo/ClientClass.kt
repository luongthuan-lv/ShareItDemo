package com.example.shareitdemo

import android.net.Uri
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
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
                    while (true) {
                        Log.e("wifidirectdemo1", "startClient")
                        clientSocket = Socket()
                        clientSocket?.bind(null)
                        clientSocket?.connect(InetSocketAddress(hostAdd, 9090), 10000)



                        sendReceive = SendReceive(clientSocket!!, handler)
                        sendReceive!!.runSendReceiveNew(activity)
                        //sendReceive!!.runSendReceive(activity)
                        //sendReceive!!.runSendReceive()

                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }
    }


    fun startClient(uri: Uri, handler: Handler) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    // clientSocket = Socket(InetAddress.getByName(hostAdd), 8888)
//                    clientSocket = Socket()
//                    clientSocket?.bind(null)
//                    clientSocket?.connect(InetSocketAddress(hostAdd, 8888), 10000)
//
                    val cr = activity.contentResolver
                    var `is`: InputStream? = null
                    try {
                        `is` = cr.openInputStream(uri)
                    } catch (e: FileNotFoundException) {
                        Log.d("wifidirectdemo1", "ERROR")
                        Log.d("wifidirectdemo1", e.toString())
                    }


                    copyFile(
                        `is`!!,
                        clientSocket?.getOutputStream()!!
                    )

                    //clientSocket!!.close()

                    sendReceive = SendReceive(clientSocket!!, handler)
                    //sendReceive!!.runSendReceive(activity)

                } catch (e: IOException) {
                    Log.e("wifidirectdemo1", "CRASH 1 $e")
                    e.printStackTrace()
                } finally {
                    if (clientSocket != null) {
                        if (clientSocket!!.isConnected()) {
                            try {
                                Log.e("wifidirectdemo1", "CLOSE SOCKET")
                                clientSocket!!.close()
                            } catch (e: IOException) {
                                // Give up
                                Log.e("wifidirectdemo1", "CRASH 2")
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }
    }

    fun writeImage(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                var outputStream: OutputStream? = null
                try {
                    outputStream = clientSocket?.getOutputStream()
                    if (outputStream == null) {
                        Log.e("wifidirectdemo1", "Output stream is null.")
                        return@withContext
                    }

                    copyFile(
                        activity.contentResolver.openInputStream(uri)!!,
                        outputStream
                    )
                } catch (e: IOException) {
                    Log.e("wifidirectdemo1", "CRASH 1 $e")
                    e.printStackTrace()
                } finally {
                    if (clientSocket != null) {
                        if (clientSocket!!.isConnected()) {
                            try {
                                Log.e("wifidirectdemo1", "CLOSE SOCKET")
                                outputStream?.flush()
                                outputStream?.close()
                            } catch (e: IOException) {
                                // Give up
                                Log.e("wifidirectdemo1", "CRASH 2")
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }
    }

    fun copyFile(inputStream: InputStream, out: OutputStream): Boolean {

        val buf = ByteArray(1024)
        var len: Int
        try {
            Log.e("wifidirectdemo1", "SEND FILE 00 ")
            while (inputStream.read(buf).also { len = it } != -1) {
                out.write(buf, 0, len)
            }

            Log.e("wifidirectdemo1", "SEND FILE 22 ")


        } catch (e: IOException) {
            Log.e("wifidirectdemo1", "SEND FILE 33 ")
            Log.d("wifidirectdemo1", e.toString())
            return false
        } finally {
            inputStream.close()
        }
        return true
    }
}