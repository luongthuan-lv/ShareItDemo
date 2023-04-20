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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(9090)
                while (true) {
                    Log.e("wifidirectdemo1", "HEREEEE11")
                    socket = serverSocket!!.accept()
                    Log.e("wifidirectdemo1", "HEREEEE22")
                    sendReceive = SendReceive(socket!!, handler)
                    sendReceive!!.runSendReceiveNew(activity)
                    //sendReceive!!.runSendReceive(activity)
                    Log.e("wifidirectdemo1", "HEREEEE33")
                    //sendReceive!!.runSendReceive()


                    Log.e("wifidirectdemo1", "HEREEEE44")
                }

            } catch (e: Exception) {
                Log.e("wifidirectdemo1", "HEREEEE2 $e")
                e.printStackTrace()
            }


        }
    }

    fun startSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
//                if (socket!!.isClosed) {
//                    Log.e("wifidirectdemo1", "accept SERVER SOCKET $socket")
//                    Log.e("wifidirectdemo1", "accept SERVER SOCKET1 $serverSocket")
//                    socket = serverSocket!!.accept()
//                    Log.e("wifidirectdemo1", "accept SERVER SOCKET2 $socket")
//                }
            } catch (e: Exception) {
                Log.e("wifidirectdemo1", "HEREEEE1 $e")
                e.printStackTrace()
            }
        }

    }

    fun startServerText(handler: Handler) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(8888)
                socket = serverSocket!!.accept()
                sendReceive = SendReceive(socket!!, handler)
                //sendReceive!!.runSendReceive(activity)
                //sendReceive!!.runSendReceiveNew(activity)
                sendReceive!!.runSendReceive()

            } catch (e: Exception) {
                Log.e("wifidirectdemo1", "HEREEEE2 $e")
                e.printStackTrace()
            }


        }
    }

    fun stopServer() {
        socket?.close()
        serverSocket?.close()
    }

    fun writeImage(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                var outputStream: OutputStream? = null
                try {
                    outputStream = socket?.getOutputStream()
                    Log.e("wifidirectdemo1", "Check write  file")
                    if (outputStream == null) {
                        Log.e("wifidirectdemo1", "Output stream is null.")
                        return@withContext
                    }

                    copyFile(
                        activity.contentResolver.openInputStream(uri)!!, outputStream
                    )
                } catch (e: IOException) {
                    Log.e("wifidirectdemo1", "CRASH 1 $e")
                    e.printStackTrace()
                } finally {
                    if (socket != null) {
                        if (socket!!.isConnected()) {
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