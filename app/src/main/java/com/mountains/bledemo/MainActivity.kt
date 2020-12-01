package com.mountains.bledemo

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mountains.bledemo.adapter.CardAdapter
import com.mountains.bledemo.base.BaseActivity
import com.mountains.bledemo.bean.CardItemData
import com.mountains.bledemo.ble.BleException
import com.mountains.bledemo.ble.callback.CommCallback
import com.mountains.bledemo.event.DataUpdateEvent
import com.mountains.bledemo.event.SportEvent
import com.mountains.bledemo.helper.BaseUUID
import com.mountains.bledemo.helper.CommHelper
import com.mountains.bledemo.presenter.MainPresenter
import com.mountains.bledemo.service.DeviceConnectService
import com.mountains.bledemo.ui.activity.*
import com.mountains.bledemo.ui.fragment.CalendarDialogFragment
import com.mountains.bledemo.util.DisplayUtil
import com.mountains.bledemo.view.MainView
import com.mountains.bledemo.weiget.CardItemDataDecoration
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode


class MainActivity : BaseActivity<MainPresenter>(), MainView {

    val cardItemList = mutableListOf<CardItemData>()
    val cardItemAdapter by lazy { CardAdapter(R.layout.item_card,cardItemList) }


    override fun createPresenter(): MainPresenter {
        return MainPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)

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
        when(dataUpdateEvent.type){
            DataUpdateEvent.HEART_RATE_UPDATE_TYPE->{
                presenter.getHeartRateData()
            }
            DataUpdateEvent.BLOOD_OXYGEN_UPDATE_TYPE->{
                presenter.getBloodOxygenData()
            }
            DataUpdateEvent.BLOOD_PRESSURE_UPDATE_TYPE->{
                presenter.getBloodPressureData()
            }
        }

    }

    private  fun initView(){
        recyclerView.apply {
            layoutManager = GridLayoutManager(context,2)
            addItemDecoration(CardItemDataDecoration(DisplayUtil.dp2px(context,12f)))
            adapter = cardItemAdapter
        }
        cardItemAdapter.setOnItemClickListener { adapter, view, position ->
            when(cardItemList[position].itemType){
                CardItemData.DEVICE_TYPE->{
                    val intent = Intent(getContext(), BindDeviceActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.HEART_RATE_TYPE->{
                    val intent = Intent(getContext(), HeartRateDetailsActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.SLEEP_TYPE->{
                    val intent = Intent(getContext(), SleepDetailsActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.DETECTION_TYPE->{
                    val intent = Intent(getContext(), HealthDetectionActivity::class.java)
                    startActivity(intent)
                }
                CardItemData.BLOOD_OXYGEN_TYPE->{
                    val intent = Intent(getContext(), BloodOxygenDetailsActivity::class.java)
                    startActivity(intent)
                }
            }

        }

        swipeRefreshLayout.setOnRefreshListener {

            DeviceConnectService.connectedDevice?.writeCharacteristic(BaseUUID.SERVICE,BaseUUID.WRITE,CommHelper.heartRateDetection(1),object : CommCallback {
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

        stepsView.setMaxSteps(8000)
        tvGoal.text = "8000"

    }


    private fun initCard(){
        val deviceCard = CardItemData(CardItemData.DEVICE_TYPE, R.drawable.ic_card_device, "", "", "设备")
        val healthDetection = CardItemData(CardItemData.DETECTION_TYPE, R.drawable.ic_card_detection, "", "", "健康体检")
        val heartCard = CardItemData(CardItemData.HEART_RATE_TYPE, R.drawable.ic_card_heart, "0 - 0bpm", "最后一次:暂无数据", "心率记录")
        val bloodPressureCard = CardItemData(CardItemData.BLOOD_PRESSURE_TYPE, R.drawable.ic_card_blood_pressure, "0 / 0mmHg", "最后一次:暂无数据", "血压记录")
        val bloodOxygenCard = CardItemData(CardItemData.BLOOD_OXYGEN_TYPE, R.drawable.ic_card_blood_oxygen, "0 - 0%", "最后一次:暂无数据", "血氧记录")
        val sleepCard = CardItemData(CardItemData.SLEEP_TYPE, R.drawable.ic_card_sleep, "0h 0min", "最后一次:暂无数据", "睡眠记录")
        cardItemList.add(deviceCard)
        cardItemList.add(healthDetection)
        cardItemList.add(heartCard)
        cardItemList.add(bloodPressureCard)
        cardItemList.add(bloodOxygenCard)
        cardItemList.add(sleepCard)
        cardItemAdapter.notifyDataSetChanged()
    }

    private fun initData(){
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

    override fun onBloodPressureData(valueContent: String, timeContent: String) {
        cardItemList.filter { it.itemType == CardItemData.BLOOD_PRESSURE_TYPE }.forEach {
            it.value = valueContent
            it.time = timeContent
        }
        cardItemAdapter.notifyDataSetChanged()
    }
}
