package com.mountains.bledemo

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.telecom.ConnectionService
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
import com.mountains.bledemo.event.SportEvent
import com.mountains.bledemo.helper.BaseUUID
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.helper.SportDataDecodeHelper
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.ui.activity.BindDeviceActivity
import com.mountains.bledemo.util.DisplayUtil
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode


class MainActivity : BaseActivity<MainPresenter>(), MainView {

    val itemDataList = mutableListOf<CardItemData>()
    val itemDataAdapter by lazy { ItemDataAdapter(R.layout.item_data,itemDataList) }


    override fun createPresenter(): MainPresenter {
        return MainPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)

        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(Color.TRANSPARENT)

        initView()
        initItemData()
    }

    override fun onDestroy() {
        super.onDestroy()
        DeviceConnectService.connectedDevice?.disconnect()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(sportEvent: SportEvent){
        val sportBean = sportEvent.sportBean
        val bigDecimal = BigDecimal(sportBean.mileage.toDouble()/1000)
        val mileage = bigDecimal.setScale(2, RoundingMode.HALF_UP).toString()
        tvCalorie.text = "${sportBean.calorie}"
        tvMileage.text = mileage
        stepsView.setCurrentSteps(sportBean.steps)
    }

    private  fun initView(){
        recyclerView.apply {
            layoutManager = GridLayoutManager(context,2)
            addItemDecoration(ItemDataDecoration(DisplayUtil.dp2px(context,12f)))
            adapter = itemDataAdapter
        }
        itemDataAdapter.setOnItemClickListener { adapter, view, position ->
            when(itemDataList[position].itemType){
                CardItemData.DEVICE_TYPE->{
                    val intent = Intent(this@MainActivity, BindDeviceActivity::class.java)
                    startActivity(intent)
                }
            }

        }

        swipeRefreshLayout.setOnRefreshListener {
            DeviceConnectService.connectedDevice?.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE,CommHelper.getDeviceInfo(),object : CommCallBack{
                override fun onSuccess(byteArray: ByteArray?) {
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun onFail(exception: BleException) {
                    swipeRefreshLayout.isRefreshing = false
                }
            })
        }
    }


    private fun initItemData(){
        val deviceCard = CardItemData(CardItemData.DEVICE_TYPE, R.drawable.ic_card_device, null, null, "设备")
        val heartCard = CardItemData(CardItemData.HEART_TYPE, R.drawable.ic_card_heart, "0 - 0bpm", "暂无数据", "心率记录")
        val bloodPressureCard = CardItemData(CardItemData.BLOOD_PRESSURE_TYPE, R.drawable.ic_card_blood_pressure, "0 / 0mmHg", "暂无数据", "血压记录")
        val bloodOxygenCard = CardItemData(CardItemData.BLOOD_OXYGEN_TYPE, R.drawable.ic_card_blood_oxygen, "0 - 0%", "暂无数据", "血氧记录")
        val sleepCard = CardItemData(CardItemData.SLEEP_TYPE, R.drawable.ic_card_sleep, "0h 0min", "暂无数据", "睡眠记录")
        itemDataList.add(deviceCard)
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
