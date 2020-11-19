package com.mountains.bledemo.adapter

import android.bluetooth.BluetoothDevice
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mountains.bledemo.R
import kotlinx.android.synthetic.main.item_bind_device.view.*

class BindDeviceAdapter(layoutResId:Int,data:MutableList<BluetoothDevice>) : BaseQuickAdapter<BluetoothDevice,BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: BluetoothDevice) {
        holder.setText(R.id.tvDeviceName,item.name)
        holder.setText(R.id.tvDeviceAddress,item.address)
    }
}