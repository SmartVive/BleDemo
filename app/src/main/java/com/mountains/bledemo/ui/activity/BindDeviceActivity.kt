package com.mountains.bledemo.ui.activity

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.bar.OnTitleBarListener
import com.mountains.bledemo.R
import com.mountains.bledemo.adapter.BindDeviceAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.ble.BleManager
import com.mountains.bledemo.presenter.BindDevicePresenter
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.view.BindDeviceView
import kotlinx.android.synthetic.main.activity_bind_device.*



class BindDeviceActivity : BaseActivity<BindDevicePresenter>(),BindDeviceView{
    val scanDeviceList = mutableListOf<BluetoothDevice>()
    val scanDeviceAdapter by lazy { BindDeviceAdapter(R.layout.item_bind_device,scanDeviceList) }

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
    }

    private fun initView(){
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
            val intent = Intent(getContext(), DeviceConnectService::class.java)
            intent.putExtra(DeviceConnectService.DEVICE,scanDeviceList[position])
            startService(intent)
        }

        btnScan.setOnClickListener {
            scanDeviceList.clear()
            scanDeviceAdapter.notifyDataSetChanged()
            startScan()
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