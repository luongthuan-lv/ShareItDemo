package com.example.shareitdemo

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_devices.view.*

/**
 * Created by Luong Thuan on 20/03/2023.
 */
class PeersAdapter(var mContext: Context) : RecyclerView.Adapter<PeersAdapter.ViewHolder>() {
    private var listDevices: ArrayList<WifiP2pDevice> = arrayListOf()
    var onClickDevice: DeviceActionListener? = null

    fun setData(listDevices: ArrayList<WifiP2pDevice>) {
        this.listDevices.clear()
        this.listDevices.addAll(listDevices)
        Log.e("wifidirectdemo", "setData: ${this.listDevices.size}")
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.device_name
        var tvStatus: TextView = itemView.device_details
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_devices, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Log.e("wifidirectdemo", "onBind: ${listDevices[position].deviceName}")
        holder.tvName.text = listDevices[position].deviceName  +" ~~ "+ listDevices[position].deviceAddress
        holder.tvStatus.text =
            (mContext as MainActivity).getDeviceStatus(listDevices[position].status)
        holder.itemView.setOnClickListener {
            onClickDevice?.showDetails(listDevices[position])
        }

    }

    override fun getItemCount(): Int {
        return listDevices.size
    }
}