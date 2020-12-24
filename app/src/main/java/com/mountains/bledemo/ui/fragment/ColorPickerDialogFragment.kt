package com.mountains.bledemo.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.mountains.bledemo.R
import com.mountains.bledemo.util.DisplayUtil
import kotlinx.android.synthetic.main.fragment_color_picker.*
import top.defaults.colorpicker.ColorObserver


class ColorPickerDialogFragment : DialogFragment() {
    private var pickColorListener: OnPickColorListener? = null
    private var defaultColor = -1


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val screenWidth = DisplayUtil.getScreenWidth(context)
        val window = dialog!!.window!!
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout((screenWidth*0.8).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_color_picker,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorPickerView.setInitialColor(defaultColor)
        colorPickerView.subscribe(colorObserver)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnConfirm.setOnClickListener {
            pickColorListener?.onPickColor(colorPickerView.color)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        colorPickerView.unsubscribe(colorObserver)
        pickColorListener = null
    }

    private val colorObserver = ColorObserver { color, fromUser, shouldPropagate ->
        colorView.setBackgroundColor(color)
        tvColor.text = toHexEncoding(color)
    }

    private fun toHexEncoding(color: Int): String? {
        val sb = StringBuffer()
        var R: String = Integer.toHexString(Color.red(color))
        var G: String = Integer.toHexString(Color.green(color))
        var B: String = Integer.toHexString(Color.blue(color))
        //判断获取到的R,G,B值的长度 如果长度等于1 给R,G,B值的前边添0
        R = if (R.length == 1) "0$R" else R
        G = if (G.length == 1) "0$G" else G
        B = if (B.length == 1) "0$B" else B
        sb.append("#")
        sb.append(R)
        sb.append(G)
        sb.append(B)
        return sb.toString()
    }

    fun show(fragmentManager: FragmentManager, tag:String, defaultColor: Int, listener: OnPickColorListener){
        super.show(fragmentManager,tag)
        pickColorListener = listener
        this.defaultColor = defaultColor
    }

    interface OnPickColorListener{
        fun onPickColor(color:Int)
    }
}