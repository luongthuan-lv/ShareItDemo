package com.example.shareitdemo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.WifiP2pManager.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shareitdemo.hotspot.APManager
import com.example.shareitdemo.hotspot.DefaultFailureListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.net.InetAddress


class MainActivity : AppCompatActivity(), ChannelListener, DeviceActionListener,
    APManager.OnSuccessListener {
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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.System.canWrite(applicationContext)) {
//                val intent = Intent(
//                    Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse(
//                        "package:$packageName"
//                    )
//                )
//                startActivityForResult(
//                    intent,
//                    100
//                )
//            }
//        }

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        if (!initP2p()) {
            finish()
        }

        wifi.setOnClickListener {
//            val ssid = "DIRECT-hs-My12345678"
//            val pass = "12345678"
//            val band = WifiP2pConfig.GROUP_OWNER_BAND_2GHZ
//
//            val config: WifiP2pConfig = WifiP2pConfig.Builder()
//                    .setNetworkName(ssid)
//                    .setPassphrase(pass)
//                    .enablePersistentMode(false)
//                    .setGroupOperatingBand(band)
//                    .build()
//
//
//            manager?.createGroup(channel!!, config, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    Toast.makeText(
//                        this@MainActivity, "Create Wifi", Toast.LENGTH_SHORT
//                    ).show()
//                    Log.e(TAG, "Create Group ${WifiP2pGroup().passphrase}")
//                }
//
//                override fun onFailure(p0: Int) {
//                }
//
//            })


//            manager?.createGroup(channel!!, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    Toast.makeText(
//                        this@MainActivity, "Create Group", Toast.LENGTH_SHORT
//                    ).show()
//                    Log.e(TAG, "Create Group ${WifiP2pGroup().passphrase}")
//                }
//
//                override fun onFailure(p0: Int) {
//                }
//
//            })


            val apManager: APManager = APManager.getApManager(this)
            apManager.turnOnHotspot(
                this, this, DefaultFailureListener(this)
            )

        }



        stop.setOnClickListener {


            manager?.requestGroupInfo(channel!!, object : WifiP2pManager.GroupInfoListener {
                override fun onGroupInfoAvailable(p0: WifiP2pGroup?) {
                    Log.e(TAG, "Create Group ${p0?.passphrase} ~~ ${p0?.networkName}")
                }

            })
//            manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    Toast.makeText(
//                        this@MainActivity, "Stop Wifi", Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                override fun onFailure(p0: Int) {
//                }
//
//            })
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
//            val config = WifiP2pConfig()
//            config.deviceAddress = deviceHost!!.deviceAddress
//            config.wps.setup = WpsInfo.PBC
//            Log.e(TAG, "config: $config")


//            val ssid = "DIRECT-hs-My12345678"
//            val pass = "12345678"
//            val band = WifiP2pConfig.GROUP_OWNER_BAND_2GHZ
//
//            val config: WifiP2pConfig = WifiP2pConfig.Builder()
//                .setNetworkName(ssid)
//                .setPassphrase(pass)
//                .enablePersistentMode(false)
//               // .setGroupOperatingBand(band)
//                .build()


//            manager!!.connect(channel, config, object : ActionListener {
//                override fun onSuccess() {
//                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
//                    Toast.makeText(
//                        this@MainActivity, "Connect Success!", Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                override fun onFailure(reason: Int) {
//                    Toast.makeText(
//                        this@MainActivity, "Connect failed. Retry.", Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })

            connectHotspot()
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
//                serverClass?.startSocket()
//            } else {
//                Log.e(TAG, "serverClass null")
//            }
//
//            if (clientClass != null) {
//                clientClass?.startClient(handler)
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
                    clientClass?.writeImage(uri!!)
                } else {
                    Log.e(TAG, "null client 1")
                }


                if (serverClass != null) {
                    serverClass?.writeImage(uri!!)
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
//                serverClass!!.startServerText(handler)
                serverClass!!.startServer(handler)

            } else if (info.groupFormed) {
                /*TODO CLIENT*/
                Log.e(TAG, "TODO CLIENT")
                clientClass = ClientClass(groupOwnerAddress, this@MainActivity)
                clientClass?.startClient(handler)

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
                Log.e("wifidirectdemo1", "RUN HANDLE")
                Toast.makeText(this, tempMsg, Toast.LENGTH_SHORT).show()
//                tvMessage.setText(tempMsg)
            }
        }
        true
    }

    override fun onSuccess(ssid: String, password: String) {
        Toast.makeText(this, "$ssid,$password", Toast.LENGTH_LONG).show()
        Log.e(TAG, "HOTSPOT: $ssid ~ $password")
    }

    fun connectHotspot(){
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "AndroidShare_1838"
        wifiConfig.preSharedKey = "eaf0a69bc53f"

        val networkId = wifiManager.addNetwork(wifiConfig)

        wifiManager.disconnect()
        wifiManager.enableNetwork(networkId, true)
        wifiManager.reconnect()
    }

}