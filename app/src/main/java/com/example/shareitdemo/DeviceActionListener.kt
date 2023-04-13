package com.example.shareitdemo

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice

/**
 * Created by Luong Thuan on 08/03/2023.
 */
interface DeviceActionListener {
    fun showDetails(device: WifiP2pDevice?){}

    fun cancelDisconnect(){}

    fun connect(config: WifiP2pConfig?){}

    fun disconnect(){}
}