package com.mountains.bledemo

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mountains.bledemo.adapter.CardAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.ble.*
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.bean.CardItemData
import com.mountains.bledemo.event.DataUpdateEvent
import com.mountains.bledemo.event.HeartRateEvent
import com.mountains.bledemo.event.SportEvent
import com.mountains.bledemo.helper.BaseUUID
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.presenter.MainPresenter
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.ui.activity.BindDeviceActivity
import com.mountains.bledemo.ui.activity.HeartRateDetailsActivity
import com.mountains.bledemo.ui.activity.StepDetailsActivity
import com.mountains.bledemo.util.DisplayUtil
import com.mountains.bledemo.view.MainView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode


class MainActivity : BaseActivity<MainPresenter>(), MainView {

    val itemDataList = mutableListOf<CardItemData>()
    val itemDataAdapter by lazy { CardAdapter(R.layout.item_card,itemDataList) }


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
        initCard()
        initData()

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataUpdateEvent(dataUpdateEvent: DataUpdateEvent){
        if(dataUpdateEvent.type == DataUpdateEvent.HEART_RATE_UPDATE_TYPE){
            presenter.getHeartRateData()
        }

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
                    val intent = Intent(getContext(), BindDeviceActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.HEART_RATE_TYPE->{
                    val intent = Intent(getContext(), HeartRateDetailsActivity::class.java)
                    startActivity(intent)
                }
            }

        }

        swipeRefreshLayout.setOnRefreshListener {

            DeviceConnectService.connectedDevice?.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE,CommHelper.checkHeartRate(1),object : CommCallback{
                override fun onSuccess(byteArray: ByteArray?) {
                    swipeRefreshLayout.isRefreshing = false
                    Logger.d("commOnSuccess")
                }

                override fun onFail(exception: BleException) {
                    swipeRefreshLayout.isRefreshing = false
                    Logger.e("commOnFail:${exception.message}")
                }
            })
        }

        layoutStep.setOnClickListener {
            val intent = Intent(this, StepDetailsActivity::class.java)
            startActivity(intent)
        }


    }


    private fun initCard(){
        val deviceCard = CardItemData(CardItemData.DEVICE_TYPE, R.drawable.ic_card_device, "", "", "设备")
        val heartCard = CardItemData(CardItemData.HEART_RATE_TYPE, R.drawable.ic_card_heart, "0 - 0bpm", "最后一次:暂无数据", "心率记录")
        val bloodPressureCard = CardItemData(CardItemData.BLOOD_PRESSURE_TYPE, R.drawable.ic_card_blood_pressure, "0 / 0mmHg", "最后一次:暂无数据", "血压记录")
        val bloodOxygenCard = CardItemData(CardItemData.BLOOD_OXYGEN_TYPE, R.drawable.ic_card_blood_oxygen, "0 - 0%", "最后一次:暂无数据", "血氧记录")
        val sleepCard = CardItemData(CardItemData.SLEEP_TYPE, R.drawable.ic_card_sleep, "0h 0min", "最后一次:暂无数据", "睡眠记录")
        itemDataList.add(deviceCard)
        itemDataList.add(heartCard)
        itemDataList.add(bloodPressureCard)
        itemDataList.add(bloodOxygenCard)
        itemDataList.add(sleepCard)
        itemDataAdapter.notifyDataSetChanged()
    }

    private fun initData(){
        presenter.getHeartRateData()
    }

    override fun onHeartRateData(valueContent: String, timeContent: String) {
        itemDataList.filter { it.itemType == CardItemData.HEART_RATE_TYPE }.forEach {
            it.value = valueContent
            it.time = timeContent
        }
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
