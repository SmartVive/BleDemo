package com.mountains.bledemo

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mountains.bledemo.adapter.ItemDataAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.ble.*
import com.mountains.bledemo.ble.callback.CommCallBack
import com.mountains.bledemo.ble.callback.ConnectCallback
import com.mountains.bledemo.bean.CardItemData
import com.mountains.bledemo.ble.BleManager
import com.mountains.bledemo.util.DisplayUtil
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity<MainPresenter>(), MainView {
    val bleManager by lazy { BleManager.getInstance() }
    private var bleDevice : BleDevice? = null

    val itemDataList = mutableListOf<CardItemData>()
    val itemDataAdapter by lazy { ItemDataAdapter(R.layout.item_data,itemDataList) }


    override fun createPresenter(): MainPresenter {
        return MainPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(Color.TRANSPARENT)

        initView()
        initItemData()
    }

    private  fun initView(){
        recyclerView.apply {
            layoutManager = GridLayoutManager(context,2)
            addItemDecoration(ItemDataDecoration(DisplayUtil.dp2px(context,12f)))
            adapter = itemDataAdapter.apply {
                setOnItemClickListener { adapter, view, position ->

                }
            }
        }


        btnScan.setOnClickListener {
            bleManager.enableBlueTooth(this,object :BleManager.BlueToothEnableListener{
                override fun onEnableSuccess() {
                    scanDevice()
                }

                override fun onEnableFail() {
                    showToast("打开蓝牙失败")
                }

                override fun notSupportBle() {
                    showToast("不支持当前设备")
                }
            })

        }

        btnRead.setOnClickListener {

            bleDevice?.readCharacteristic("00001800-0000-1000-8000-00805f9b34fb","00002a00-0000-1000-8000-00805f9b34fb",object :CommCallBack{

                override fun onSuccess(byteArray: ByteArray?) {
                    Logger.d("readCharacteristicSuccess")
                }

                override fun onFail(exception: BleException) {
                    Logger.d("readCharacteristicFail:${exception.message}")
                }

            })

            bleDevice?.readCharacteristic("00001800-0000-1000-8000-00805f9b34fb","00002a00-0000-1000-8000-00805f9b34fb",object :CommCallBack{

                override fun onSuccess(byteArray: ByteArray?) {
                    Logger.d("readCharacteristicSuccess")
                }

                override fun onFail(exception: BleException) {
                    Logger.d("readCharacteristicFail")
                }

            })
        }



    }

    private fun scanDevice(){
        bleManager.startScan(this,object :BleManager.ScanResultListener{
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                Logger.d(result.device?.name)
                if (result.device?.name == "GIONEE F106"){
                    connect(result.device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Logger.e("onScanFailed:$errorCode")
            }

            override fun onScanComplete() {
                Logger.d("onScanComplete")
            }

        })
    }


    private fun connect(device:BluetoothDevice){

        bleManager.connectDevice(device,object :ConnectCallback{
            override fun connectSuccess(bleDevice: BleDevice) {
                showToast("连接成功:${bleDevice.device.name}")
                this@MainActivity.bleDevice = bleDevice
            }

            override fun connectFail(exception: BleException) {
                showToast("连接失败：${exception.message}")
            }

            override fun disconnect() {
                showToast("断开连接")
            }

        })
    }

    private fun initItemData(){
        val heartCard = CardItemData(CardItemData.HEART_TYPE, R.drawable.ic_card_heart, "0 - 0bpm", "暂无数据", "心率记录")
        val bloodPressureCard = CardItemData(CardItemData.BLOOD_PRESSURE_TYPE, R.drawable.ic_card_blood_pressure, "0 / 0mmHg", "暂无数据", "血压记录")
        val bloodOxygenCard = CardItemData(CardItemData.BLOOD_OXYGEN_TYPE, R.drawable.ic_card_blood_oxygen, "0 - 0%", "暂无数据", "血氧记录")
        val sleepCard = CardItemData(CardItemData.SLEEP_TYPE, R.drawable.ic_card_sleep, "0h 0min", "暂无数据", "睡眠记录")
        itemDataList.add(heartCard)
        itemDataList.add(bloodPressureCard)
        itemDataList.add(bloodOxygenCard)
        itemDataList.add(sleepCard)
        itemDataAdapter.notifyDataSetChanged()
    }

    //边距
    class ItemDataDecoration(val margin:Int) : RecyclerView.ItemDecoration() {


        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val layoutManager = parent.layoutManager
            if (layoutManager is GridLayoutManager) {
                val position = parent.getChildAdapterPosition(view)
                val spanCount = layoutManager.spanCount
                val childCount = parent.adapter?.itemCount ?: return
                //当前item在多少行
                val currentRow = Math.floor(position.toDouble() / spanCount).toInt()
                //一共有多少行
                val maxRow = Math.ceil(childCount.toDouble() / spanCount).toInt() - 1

                if (position % spanCount == 0) {
                    //第一列item
                    outRect.left = margin
                    outRect.right = margin/2
                } else if (position % spanCount == spanCount - 1) {
                    //最后一列item
                    outRect.left = margin/2
                    outRect.right = margin
                } else {
                    //中间列item
                    outRect.left = margin / 2
                    outRect.right = margin / 2
                }


                if (currentRow == 0) {
                    //第一行item
                    outRect.top = margin
                } else if (currentRow == maxRow) {
                    //最后一行item
                    outRect.top = margin
                    outRect.bottom = margin
                } else {
                    //中间行item
                    outRect.top = margin
                }
            }

        }
    }
}
