package com.mountains.bledemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.mountains.bledemo.adapter.CardAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.CardItemData
import com.mountains.bledemo.event.DataUpdateEvent
import com.mountains.bledemo.event.DeviceStateEvent
import com.mountains.bledemo.event.DisconnectAllDeviceEvent
import com.mountains.bledemo.event.SportEvent
import com.mountains.bledemo.presenter.MainPresenter
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.ui.activity.*
import com.mountains.bledemo.util.DisplayUtil
import com.mountains.bledemo.view.MainView
import com.mountains.bledemo.weiget.CardItemDataDecoration
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode


class MainActivity : BaseActivity<MainPresenter>(), MainView {

    val cardItemList = mutableListOf<CardItemData>()
    val cardItemAdapter by lazy { CardAdapter(R.layout.item_card, cardItemList) }


    override fun createPresenter(): MainPresenter {
        return MainPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)

        // 启动服务的地方
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, DeviceConnectService::class.java)
            startForegroundService(intent)
        } else {
            val intent = Intent(this, DeviceConnectService::class.java)
            startService(intent)
        }

        initView()
        initCard()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().post(DisconnectAllDeviceEvent())
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(sportEvent: SportEvent) {
        val sportBean = sportEvent.sportBean
        val bigDecimal = BigDecimal(sportBean.mileage.toDouble() / 1000)
        val mileage = bigDecimal.setScale(2, RoundingMode.HALF_UP).toString()
        tvCalorie.text = "${sportBean.calorie}"
        tvMileage.text = mileage
        stepsView.setCurrentSteps(sportBean.steps)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataUpdateEvent(dataUpdateEvent: DataUpdateEvent) {
        when (dataUpdateEvent.type) {
            DataUpdateEvent.HEART_RATE_UPDATE_TYPE -> {
                presenter.getHeartRateData()
            }
            DataUpdateEvent.BLOOD_OXYGEN_UPDATE_TYPE -> {
                presenter.getBloodOxygenData()
            }
            DataUpdateEvent.BLOOD_PRESSURE_UPDATE_TYPE -> {
                presenter.getBloodPressureData()
            }
            DataUpdateEvent.SLEEP_UPDATE_TYPE->{
                presenter.getSleepData()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeviceConnectState(deviceStateEvent: DeviceStateEvent){
        val deviceName = deviceStateEvent.deviceName
        when(deviceStateEvent.type){
            DeviceStateEvent.CONNECTED_TYPE->{
                setDeviceState(deviceName,"已连接")
            }
            DeviceStateEvent.CONNECT_FAIL_TYPE->{
                setDeviceState(deviceName,"连接失败")
            }
            DeviceStateEvent.DISCONNECT_TYPE->{
                setDeviceState(deviceName,"断开连接")
            }
            DeviceStateEvent.CONNECTING_TYPE->{
                setDeviceState(deviceName,"正在连接...")
            }
        }
    }

    private fun initView() {
        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            addItemDecoration(CardItemDataDecoration(DisplayUtil.dp2px(context, 12f)))
            adapter = cardItemAdapter
        }
        cardItemAdapter.setOnItemClickListener { adapter, view, position ->
            when (cardItemList[position].itemType) {
                CardItemData.DEVICE_TYPE -> {
                    val intent = Intent(getContext(), BindDeviceActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.HEART_RATE_TYPE -> {
                    val intent = Intent(getContext(), HeartRateDetailsActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.SLEEP_TYPE -> {
                    val intent = Intent(getContext(), SleepDetailsActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.DETECTION_TYPE -> {
                    val intent = Intent(getContext(), HealthDetectionActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.BLOOD_OXYGEN_TYPE -> {
                    val intent = Intent(getContext(), BloodOxygenDetailsActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.BLOOD_PRESSURE_TYPE -> {
                    val intent = Intent(getContext(), BloodPressureDetailsActivity::class.java)
                    startActivity(intent)
                }
            }

        }

        swipeRefreshLayout.setOnRefreshListener {
            initData()
            swipeRefreshLayout.isRefreshing = false
        }

        layoutStep.setOnClickListener {
            val intent = Intent(this, StepDetailsActivity::class.java)
            startActivity(intent)
        }

        stepsView.setMaxSteps(8000)
        tvGoal.text = "8000"

    }


    private fun initCard() {
        val deviceCard = CardItemData(CardItemData.DEVICE_TYPE, R.drawable.ic_card_device, "", "", "设备")
        val healthDetection = CardItemData(CardItemData.DETECTION_TYPE, R.drawable.ic_card_detection, "", "", "健康体检")
        val heartCard =
            CardItemData(CardItemData.HEART_RATE_TYPE, R.drawable.ic_card_heart, "0 - 0 bpm", "最后一次:暂无数据", "心率记录")
        val bloodPressureCard = CardItemData(
            CardItemData.BLOOD_PRESSURE_TYPE,
            R.drawable.ic_card_blood_pressure,
            "0 / 0 mmHg",
            "最后一次:暂无数据",
            "血压记录"
        )
        val bloodOxygenCard = CardItemData(
            CardItemData.BLOOD_OXYGEN_TYPE,
            R.drawable.ic_card_blood_oxygen,
            "0 - 0 %",
            "最后一次:暂无数据",
            "血氧记录"
        )
        val sleepCard = CardItemData(CardItemData.SLEEP_TYPE, R.drawable.ic_card_sleep, "0h 0min", "最后一次:暂无数据", "睡眠记录")
        cardItemList.add(deviceCard)
        cardItemList.add(healthDetection)
        cardItemList.add(heartCard)
        cardItemList.add(bloodPressureCard)
        cardItemList.add(bloodOxygenCard)
        cardItemList.add(sleepCard)
        cardItemAdapter.notifyDataSetChanged()
    }

    private fun initData() {
        presenter.getHeartRateData()
        presenter.getBloodOxygenData()
        presenter.getSleepData()
        presenter.getBloodPressureData()
    }

    /**
     * 心率记录
     */
    override fun onHeartRateData(valueContent: String, timeContent: String) {
        cardItemList.filter { it.itemType == CardItemData.HEART_RATE_TYPE }.forEach {
            it.value = valueContent
            it.time = timeContent
        }
        cardItemAdapter.notifyDataSetChanged()
    }

    /**
     * 血氧记录
     */
    override fun onBloodOxygenData(valueContent: String, timeContent: String) {
        cardItemList.filter { it.itemType == CardItemData.BLOOD_OXYGEN_TYPE }.forEach {
            it.value = valueContent
            it.time = timeContent
        }
        cardItemAdapter.notifyDataSetChanged()
    }

    /**
     * 睡眠记录
     */
    override fun onSleepData(valueContent: String, timeContent: String) {
        cardItemList.filter { it.itemType == CardItemData.SLEEP_TYPE }.forEach {
            it.value = valueContent
            it.time = timeContent
        }
        cardItemAdapter.notifyDataSetChanged()
    }

    /**
     * 血压记录
     */
    override fun onBloodPressureData(valueContent: String, timeContent: String) {
        cardItemList.filter { it.itemType == CardItemData.BLOOD_PRESSURE_TYPE }.forEach {
            it.value = valueContent
            it.time = timeContent
        }
        cardItemAdapter.notifyDataSetChanged()
    }

    fun setDeviceState(name:String,state:String){
        cardItemList.filter { it.itemType == CardItemData.DEVICE_TYPE }.forEach {
            it.value = name
            it.time = state
        }
        cardItemAdapter.notifyDataSetChanged()
    }
}
