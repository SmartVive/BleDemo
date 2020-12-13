package com.mountains.bledemo.ui.activity

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.bar.OnTitleBarListener
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.BindDeviceAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.base.Const
import com.mountains.bledemo.ble.BleDevice
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.BleManager
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.helper.DeviceStorage
import com.mountains.bledemo.presenter.BindDevicePresenter
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.util.SharedUtil
import com.mountains.bledemo.view.BindDeviceView
import kotlinx.android.synthetic.main.activity_bind_device.*
import java.lang.Exception


class BindDeviceActivity : BaseActivity<BindDevicePresenter>(),BindDeviceView{
    val scanDeviceList = mutableListOf<BluetoothDevice>()
    val scanDeviceAdapter by lazy { BindDeviceAdapter(R.layout.item_bind_device,scanDeviceList) }
    var deviceConnectService : DeviceConnectService? = null
    var connectingDialog:AlertDialog? = null


    private val serviceConnection = object : ServiceConnection{

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            deviceConnectService = (service as DeviceConnectService.MyBinder).getService()

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            deviceConnectService = null
        }

    }

    override fun createPresenter(): BindDevicePresenter {
        return BindDevicePresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_device)
        initView()
        startScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopScan()
        try {
            unbindService(serviceConnection)
        }catch (e:Exception){}

    }

    private fun initView(){
        val intent = Intent(getContext(), DeviceConnectService::class.java)
        startService(intent)
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE)

        titleBar.setOnTitleBarListener(object :OnTitleBarListener{
            override fun onLeftClick(v: View?) {
                finish()
            }

            override fun onRightClick(v: View?) {

            }

            override fun onTitleClick(v: View?) {

            }

        })


        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BindDeviceActivity)
            addItemDecoration(DividerItemDecoration(this@BindDeviceActivity,DividerItemDecoration.VERTICAL))
            adapter = scanDeviceAdapter
        }

        scanDeviceAdapter.setOnItemClickListener { adapter, view, position ->
            deviceConnectService?.let {
               connectDevice(scanDeviceList[position])
            }

        }

        btnScan.setOnClickListener {
            scanDeviceList.clear()
            scanDeviceAdapter.notifyDataSetChanged()
            startScan()
        }

    }

    private fun connectDevice(device: BluetoothDevice){
        deviceConnectService?.connectDevice(device,object : ConnectCallback{
            override fun connectSuccess(bleDevice: BleDevice) {
                val deviceStorage = DeviceStorage.getInstance()
                deviceStorage.mac = bleDevice.getMac()
                deviceStorage.name = bleDevice.getName()
                deviceStorage.save()
                //SharedUtil.save(Const.BIND_DEVICE_MAC,bleDevice.getMac())
                hideConnectingDialog()
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun connectFail(exception: BleException) {
                hideConnectingDialog()
            }

            override fun disconnect() {
                hideConnectingDialog()
            }

        })
        showConnectingDialog()
    }

    private fun showConnectingDialog(){
        try {
            connectingDialog = AlertDialog.Builder(this)
                .setTitle("连接设备")
                .setMessage("正在连接设备...")
                .setCancelable(false)
                .show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun hideConnectingDialog(){
        try {
            connectingDialog?.dismiss()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    /**
     * 扫描
     */
    private fun startScan(){
        if (BleManager.getInstance().isBlueToothEnable()){
            presenter.startScan(this)
        }else{
            presenter.enableBlueTooth(this)
        }
    }

    override fun onStartScan() {
        btnScan.text = "扫描中..."
        btnScan.isEnabled = false
    }

    override fun onScanComplete() {
        btnScan.text = "扫描"
        btnScan.isEnabled = true
    }

    override fun onEnableBleSuccess() {
        presenter.startScan(this)
    }

    override fun onScanDeviceResult(result: ScanResult) {
        for (scanDevice in scanDeviceList){
            if(scanDevice.address == result.device.address){
                return
            }
        }
        scanDeviceList.add(result.device)
        scanDeviceAdapter.notifyItemInserted(scanDeviceList.size-1)
    }

}