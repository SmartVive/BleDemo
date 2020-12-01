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
import com.mountains.bledemo.event.BloodOxygenDetectionEvent
import com.mountains.bledemo.presenter.BloodOxygenDetectionPresenter
import com.mountains.bledemo.view.BloodOxygenDetectionView
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_blood_oxygen_detection.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BloodOxygenDetectionFragment : BaseFragment<BloodOxygenDetectionPresenter>(),BloodOxygenDetectionView{
    private var isDetecting = false
    private var count = 0
    private val handler = object :Handler(Looper.getMainLooper()){
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


    override fun createPresenter(): BloodOxygenDetectionPresenter {
        return BloodOxygenDetectionPresenter()
    }

    override fun setContentView(): Int {
        return R.layout.fragment_blood_oxygen_detection
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)


        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_blood_oxygen_detection)
            .addListener(object : RequestListener<GifDrawable>{
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
                    ivBoolOxygen.setImageDrawable(resource)
                    return true
                }

            })
            .into(ivBoolOxygen)



        btnDetection.setOnClickListener {
            btnDetection.isEnabled = false
            isDetecting = true
            count = 0
            presenter?.startBloodOxygenDetection()
            handler.sendEmptyMessageDelayed(DETECTION_TIME_OUT_MSG,70*1000)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (isDetecting){
            presenter?.stopBloodOxygenDetection()
        }
        EventBus.getDefault().unregister(this)
        handler.removeMessages(DETECTION_TIME_OUT_MSG)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event:BloodOxygenDetectionEvent){
        tvBloodOxygen.text = "${event.value}"
        presenter?.addBloodOxygenDetectionResult(event.value)
        count++
        if (count >= 10){
            count = 0
            presenter?.bloodOxygenDetectionFinish()
        }
    }

    private fun stopGif(){
        try {
            val drawable = ivBoolOxygen.drawable
            if (drawable != null && drawable is GifDrawable && drawable.isRunning){
                drawable.stop()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun startGif(){
        try {
            val drawable = ivBoolOxygen.drawable
            if (drawable != null && drawable is GifDrawable && !drawable.isRunning) {
                drawable.start()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onStartDetection() {
        isDetecting = true
        btnDetection.text = "正在检测血氧..."
        btnDetection.isEnabled = false
        startGif()
    }

    override fun onStopDetection() {
        isDetecting = false
        btnDetection.isEnabled = true
        btnDetection.text = "开始检测血氧"
        stopGif()
        handler.removeMessages(DETECTION_TIME_OUT_MSG)
    }

    override fun onBloodOxygenDetectionFinish(bloodOxygen: Int) {
        showToast("检测完成")
        tvBloodOxygen.text = "${bloodOxygen}"
    }
}