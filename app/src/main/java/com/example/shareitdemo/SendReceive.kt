package com.example.shareitdemo

import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket

/**
 * Created by Luong Thuan on 21/03/2023.
 */
class SendReceive(var socket: Socket, var handler: Handler) {
    val inputStream: InputStream = socket.getInputStream()
    val outputStream: OutputStream = socket.getOutputStream()
    val MESSAGE_READ = 1
    var isRunning = true

    fun runSendReceive() {
        CoroutineScope(Dispatchers.IO).launch {
            while (socket != null) {
                try {
                    Log.e("wifidirectdemo", "RECEIVE TEXT")
                    /*send text*/
                    val buffer = ByteArray(1024)
                    val bytes: Int = inputStream.read(buffer)
                    Log.e("wifidirectdemo", "try")
                    if (bytes > 0) {
                        Log.e("wifidirectdemo", "RECEIVE: $handler")
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget()
                    }

                } catch (e: IOException) {
                    Log.e("wifidirectdemo", "catch")
                    e.printStackTrace()
                }
            }
        }
    }

    fun runSendReceive(activity: MainActivity) {
        CoroutineScope(Dispatchers.IO).launch {
            //while (socket != null) {
            Log.e("wifidirectdemo", "RECEIVE FILE")
            val f = File(
                activity.getExternalFilesDir("received"),
                "wifip2pshared-" + System.currentTimeMillis() + ".jpg"
            )

            val dirs = File(f.parent!!)
            if (!dirs.exists()) dirs.mkdirs()
            f.createNewFile()
            Log.e("wifidirectdemo", "CHECK  COPY ")
            copyFile(inputStream, FileOutputStream(f))

            if (isRunning) {
                Log.e("wifidirectdemo", "CHECK  OPEN ")
                val fileUri = FileProvider.getUriForFile(
                    activity,
                    "com.example.android.shareit.fileprovider",
                    f
                )

                Log.e("wifidirectdemo", "uri: ~~~  $fileUri")

                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(fileUri, "image/*")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                activity.startActivity(intent)

            }


//                break
            //}
        }
    }

    fun write(bytes: ByteArray?) {
        try {
            Log.e("wifidirectdemo", "SEND")
            outputStream.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
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


            out.close()
//            inputStream.close()
            Log.e("wifidirectdemo", "close input and output + check")


        } catch (e: IOException) {
            isRunning = false
            Log.e("wifidirectdemo", "SEND FILE 33 ")
            Log.d("wifidirectdemo", e.toString())
            return false
        }
        return true
    }
}