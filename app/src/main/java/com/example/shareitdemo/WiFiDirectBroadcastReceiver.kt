package com.example.shareitdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Parcelable
import android.util.Log

class WiFiDirectBroadcastReceiver(
    var manager: WifiP2pManager,
    var channel: WifiP2pManager.Channel,
    var activity: MainActivity
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {

            // UI update to indicate wifi p2p status.
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true)
            } else {
                activity.setIsWifiP2pEnabled(false)
                activity.resetData()
            }
            Log.d(activity.TAG, "P2P state changed - $state")
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                manager.requestPeers(channel, activity.peerListListener)
                Log.d(activity.TAG, "P2P peers changed")
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            if (manager == null) {
                return
            }

            val networkInfo =
                intent.getParcelableExtra<Parcelable>(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo?

            if (networkInfo!!.isConnected) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, activity.connectionInfoListener)
                Log.d(activity.TAG, "P2P connection changed0")
            } else {
                // It's a disconnect
                activity.resetData()
                Log.d(activity.TAG, "P2P connection changed1")
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            activity.updateThisDevice(intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)!!)
            Log.d(activity.TAG, "P2P device changed")
        }
    }
}