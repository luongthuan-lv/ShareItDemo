package com.example.shareitdemo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.WifiP2pManager.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.net.InetAddress


class MainActivity : AppCompatActivity(), ChannelListener, DeviceActionListener {
    val TAG = "wifidirectdemo"
    private var manager: WifiP2pManager? = null
    private var isWifiP2pEnabled = false
    private var retryChannel = false

    private val intentFilter = IntentFilter()
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null

    private var device: WifiP2pDevice? = null
    private var deviceHost: WifiP2pDevice? = null
    private var peers: ArrayList<WifiP2pDevice>? = null

    private var peersAdapter: PeersAdapter? = null
    private val CHOOSE_FILE_RESULT_CODE = 20

    private var isStart = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        if (!initP2p()) {
            finish()
        }

        wifi.setOnClickListener {
            if (serverClass != null) {
                Log.e(
                    TAG,
                    "start serverClass: serverSocket:" + serverClass!!.serverSocket.toString()
                )
                Log.e(TAG, "start serverClass: socket" + serverClass!!.socket.toString())
            } else {
                Log.e(TAG, "serverClass null")
            }

            if (clientClass != null) {
                Log.e(
                    TAG,
                    "start clientClass: clientSocket:" + clientClass!!.clientSocket.toString()
                )
                Log.e(TAG, "start clientClass: hostAdd" + clientClass!!.hostAdd.toString())
            } else {
                Log.e(TAG, "clientClass null")
            }
        }

        discover.setOnClickListener {
            Log.e(TAG, "1111: ${Utils.getMACAddress("wlan0")}")
            Log.e(TAG, "2222: ${Utils.getMACAddress("eth0")}")
            Log.e(TAG, "3333: ${Utils.getIPAddress(true)}")
            Log.e(TAG, "4444: ${Utils.getIPAddress(false)}")

            progress.visibility = View.VISIBLE

            if (!isWifiP2pEnabled) {
                Toast.makeText(this, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show()
            }

            manager?.discoverPeers(channel, object : ActionListener {
                override fun onSuccess() {
                    Toast.makeText(this@MainActivity, "Discovery Initiated", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onFailure(p0: Int) {
                    Toast.makeText(
                        this@MainActivity, "Discovery Failed : $p0", Toast.LENGTH_SHORT
                    ).show()
                    progress.visibility = View.GONE
                }

            })
        }

        btnConnect.setOnClickListener {
            val config = WifiP2pConfig()
            config.deviceAddress = deviceHost!!.deviceAddress
            config.wps.setup = WpsInfo.PBC
            Log.e(TAG, "config: $config")
            manager!!.connect(channel, config, object : ActionListener {
                override fun onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(
                        this@MainActivity, "Connect failed. Retry.", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        btnDisConnect.setOnClickListener {
            manager!!.removeGroup(channel, object : ActionListener {
                override fun onFailure(reasonCode: Int) {
                    Log.d(
                        TAG, "Disconnect failed. Reason :$reasonCode"
                    )
                }

                override fun onSuccess() {
                    llConnect.visibility = View.GONE
                }
            })
        }

        btnGallery.setOnClickListener {


//            if (serverClass != null) {
//                if (serverClass?.sendReceive?.isRunning == false) {
//                    serverClass?.startServer(handler)
//                    Log.e(TAG, "start serverClass: " + serverClass!!.socket.toString())
//                } else {
//                    Log.e(TAG, "serverClass isRunning null")
//                }
//            } else {
//                Log.e(TAG, "serverClass null")
//            }
//
//            if (clientClass != null) {
//                if (clientClass?.sendReceive?.isRunning == false) {
//                    clientClass?.startClient(handler)
//                    Log.e(TAG, "start clientClass: " + clientClass!!.clientSocket.toString())
//                } else {
//                    Log.e(TAG, "clientClass isRunning null")
//                }
//            } else {
//                Log.e(TAG, "clientClass null")
//            }


            // Allow user to pick an image from Gallery or other
            // registered apps
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(
                intent, CHOOSE_FILE_RESULT_CODE
            )
        }

        btnSend.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val content = editData.text.toString()

                if (clientClass != null) {
                    if (clientClass!!.sendReceive != null) {
                        clientClass!!.sendReceive!!.write(content.toByteArray())
                    } else {
                        Log.e(TAG, "null client 2")
                    }

                } else {
                    Log.e(TAG, "null client 1")
                }

                if (serverClass != null) {
                    if (serverClass!!.sendReceive != null) {
                        serverClass!!.sendReceive!!.write(content.toByteArray())
                    } else {
                        Log.e(TAG, "null server 2")
                    }

                } else {
                    Log.e(TAG, "null server 1")
                }
            }
        }

        peersAdapter = PeersAdapter(this@MainActivity)
        peersAdapter?.onClickDevice = object : DeviceActionListener {
            override fun showDetails(device: WifiP2pDevice?) {
                llConnect.visibility = View.VISIBLE
                deviceHost = device
            }
        }
        rvList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = peersAdapter
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        if (data == null) return
        val uri = data.data

        // Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show()
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (clientClass != null) {
                    if (clientClass?.clientSocket == null) {
                        Log.e(TAG, "KHỞI TẠO")
                        clientClass?.startClient(uri!!)
                    } else {
                        clientClass?.startClientNew(uri!!)
                        Log.e(TAG, "KHÔNG KHỞI TẠO")
                    }

//                    if (clientClass!!.sendReceive != null) {
//                        Log.e(
//                            "wifidirectdemo",
//                            "CHECK CLIENT: ${contentResolver.openInputStream(uri!!)!!} ~~~~~ ${clientClass!!.sendReceive!!.outputStream}   "
//                        )
//                        clientClass!!.sendReceive!!.copyFile(
//                            contentResolver.openInputStream(uri!!)!!,
//                            clientClass!!.sendReceive!!.outputStream
//                        )
//                    } else {
//                        Log.e(TAG, "null client 2")
//                    }
                } else {
                    Log.e(TAG, "null client 1")
                }


                if (serverClass != null) {
                    if (serverClass!!.sendReceive != null) {

                        Log.e(
                            "wifidirectdemo",
                            "CHECK SERVER: ${contentResolver.openInputStream(uri!!)!!} ~~~~~ ${serverClass!!.sendReceive!!.outputStream}   "
                        )

                        serverClass!!.sendReceive!!.copyFile(
                            contentResolver.openInputStream(uri!!)!!,
                            serverClass!!.sendReceive!!.outputStream
                        )
                    } else {
                        Log.e(TAG, "null server 2")
                    }
                } else {
                    Log.e(TAG, "null server 1")
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "CRASH HERE")
        }

    }

    /** register the BroadcastReceiver with the intent values to be matched  */
    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(manager!!, channel!!, this)
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }


    fun setIsWifiP2pEnabled(isWifiP2pEnabled: Boolean) {
        this.isWifiP2pEnabled = isWifiP2pEnabled
    }

    private fun initP2p(): Boolean {
        // Device capability definition check
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(
                TAG, "Wi-Fi Direct is not supported by this device."
            )
            return false
        }

        // Hardware capability check
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (!wifiManager.isP2pSupported) {
            Log.e(
                TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off."
            )
            return false
        }
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        if (manager == null) {
            Log.e(
                TAG, "Cannot get Wi-Fi Direct system service."
            )
            return false
        }
        channel = manager!!.initialize(this, mainLooper, null)
        if (channel == null) {
            Log.e(
                TAG, "Cannot initialize Wi-Fi Direct."
            )
            return false
        }
        return true
    }

    override fun onChannelDisconnected() {
        // we will try once more

        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show()
            resetData()
            retryChannel = true
            manager!!.initialize(this, mainLooper, this)
        } else {
            Toast.makeText(
                this,
                "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun resetData() {
//        val fragmentList = fragmentManager
//            .findFragmentById(R.id.frag_list) as DeviceListFragment
//        val fragmentDetails: DeviceDetailFragment = fragmentManager
//            .findFragmentById(R.id.frag_detail) as DeviceDetailFragment
//        fragmentList?.clearPeers()
//        if (fragmentDetails != null) {
//            fragmentDetails.resetViews()
//        }
    }


    override fun showDetails(device: WifiP2pDevice?) {
    }

    override fun cancelDisconnect() {


    }

    override fun connect(config: WifiP2pConfig?) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }


        manager!!.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(
                    this@MainActivity, "Connect failed. Retry.", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun disconnect() {
    }

    fun updateThisDevice(device: WifiP2pDevice) {
        this.device = device
        my_name.text = device.deviceName + " ~~ " + device.deviceAddress
        my_status.text = getDeviceStatus(device.status)
    }

    fun getDeviceStatus(deviceStatus: Int): String {
        Log.d(TAG, "Peer status :$deviceStatus")
        return when (deviceStatus) {
            WifiP2pDevice.AVAILABLE -> "Available"
            WifiP2pDevice.INVITED -> "Invited"
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
    }

    var peerListListener: PeerListListener = object : PeerListListener {
        override fun onPeersAvailable(peerList: WifiP2pDeviceList) {
            progress.visibility = View.GONE
            peers = arrayListOf()
            peers?.let {
                it.clear()
                it.addAll(peerList.deviceList)
                peersAdapter?.setData(it)
                Log.d(TAG, "SIZE DEVICE: ${peerList.deviceList?.size} ~~~ ${it.size}")
                if (it.size == 0) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.GONE
                }
            }

        }

    }

    var serverClass: ServerClass? = null
    var clientClass: ClientClass? = null

    //    var sendReceive: SendReceive? = null
    var connectionInfoListener: ConnectionInfoListener = object : ConnectionInfoListener {
        override fun onConnectionInfoAvailable(info: WifiP2pInfo) {
            val groupOwnerAddress: InetAddress = info.groupOwnerAddress

            if (info.groupFormed && info.isGroupOwner) {
                /*TODO SERVER*/
                Log.e(TAG, "TODO SERVER")
                serverClass = ServerClass(this@MainActivity)
                serverClass?.startServer(handler)

            } else if (info.groupFormed) {
                /*TODO CLIENT*/
                Log.e(TAG, "TODO CLIENT")
                clientClass = ClientClass(groupOwnerAddress, this@MainActivity)
                // clientClass?.startClient(handler)

            }

            btnConnect.visibility = View.GONE
        }
    }


    val MESSAGE_READ = 1
    var handler = Handler { msg ->
        when (msg.what) {
            MESSAGE_READ -> {
                val readBuff = msg.obj as ByteArray
                val tempMsg = String(readBuff, 0, msg.arg1)
                Log.e(TAG, "RUN HANDLE")
                Toast.makeText(this, tempMsg, Toast.LENGTH_SHORT).show()
//                tvMessage.setText(tempMsg)
            }
        }
        true
    }

}