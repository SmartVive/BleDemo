package com.mountains.bledemo.weiget

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import com.orhanobut.logger.Logger

class NoSlideSwitch : SwitchCompat {
    private var onWantCheckedChangeListener : OnWantCheckedChangeListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setChecked(checked: Boolean) {
        if (isPressed){
            onWantCheckedChangeListener?.onWantCheckedChanged(this,checked)
        }else{
            super.setChecked(checked)
        }
    }

    fun setOnWantCheckedChangeListener(listener: OnWantCheckedChangeListener){
        onWantCheckedChangeListener = listener
    }

    interface OnWantCheckedChangeListener{
        fun onWantCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)
    }
}