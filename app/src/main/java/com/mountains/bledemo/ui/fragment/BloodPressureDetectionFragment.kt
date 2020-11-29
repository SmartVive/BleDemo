package com.mountains.bledemo.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseFragment
import com.mountains.bledemo.event.BloodPressureDetectionEvent
import com.mountains.bledemo.presenter.BloodPressureDetectionPresenter
import com.mountains.bledemo.view.BloodPressureDetectionView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_blood_pressure_detection.*
import kotlinx.android.synthetic.main.fragment_blood_pressure_detection.btnDetection
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BloodPressureDetectionFragment : BaseFragment<BloodPressureDetectionPresenter>(),BloodPressureDetectionView {
    private var isDetecting = false
    private var count = 0
    private val handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                DETECTION_TIME_OUT_MSG->{
                    Logger.w("检测超时")
                    showToast("请检查手环是否正确佩戴")
                    onStopDetection()
                }
            }
        }
    }
    companion object{
        const val DETECTION_TIME_OUT_MSG = 100
    }

    override fun createPresenter(): BloodPressureDetectionPresenter {
        return BloodPressureDetectionPresenter()
    }

    override fun setContentView(): Int {
        return R.layout.fragment_blood_pressure_detection
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_blood_pressure_detection)
            .addListener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    ivBoolPressure.setImageDrawable(resource)
                    return true
                }

            })
            .into(ivBoolPressure)
        stopGif()
        btnDetection.setOnClickListener {
            btnDetection.isEnabled = false
            count = 0
            presenter?.startBloodPressureDetection()
            handler.sendEmptyMessageDelayed(DETECTION_TIME_OUT_MSG,70*1000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isDetecting){
            presenter?.stopBloodPressureDetection()
        }
        EventBus.getDefault().unregister(this)
        handler.removeMessages(DETECTION_TIME_OUT_MSG)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event:BloodPressureDetectionEvent){
        tvBloodPressure.text = "${event.bloodDiastolic} / ${event.bloodSystolic}"
        if (count >= 1){
            count = 0
            presenter?.stopBloodPressureDetection()
        }else{
            count++
        }
    }

    private fun stopGif(){
        try {
            val drawable = ivBoolPressure.drawable
            if (drawable != null && drawable is GifDrawable && drawable.isRunning){
                drawable.stop()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun startGif(){
        try {
            val drawable = ivBoolPressure.drawable
            if (drawable != null && drawable is GifDrawable && !drawable.isRunning){
                drawable.start()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onStartDetection() {
        isDetecting = true
        btnDetection.text = "正在检测血压..."
        btnDetection.isEnabled = false
        startGif()
    }

    override fun onStopDetection() {
        isDetecting = false
        btnDetection.isEnabled = true
        btnDetection.text = "开始检测血压"
        stopGif()
        handler.removeMessages(DETECTION_TIME_OUT_MSG)
    }
}