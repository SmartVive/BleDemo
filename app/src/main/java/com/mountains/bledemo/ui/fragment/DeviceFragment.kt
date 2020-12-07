package com.mountains.bledemo.ui.fragment

import android.os.Bundle
import android.view.View
import com.mountains.bledemo.R
import com.mountains.bledemo.base.BaseFragment
import com.mountains.bledemo.presenter.DevicePresenter
import com.mountains.bledemo.view.DeviceView

class DeviceFragment : BaseFragment<DevicePresenter>(),DeviceView {
    override fun createPresenter(): DevicePresenter {
        return DevicePresenter()
    }

    override fun setContentView(): Int {
        return R.layout.fragment_device
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}