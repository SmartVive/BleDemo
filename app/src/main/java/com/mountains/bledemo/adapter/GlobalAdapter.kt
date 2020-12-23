package com.mountains.bledemo.adapter

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout

import com.billy.android.loading.Gloading
import com.mountains.bledemo.util.DisplayUtil


class GlobalAdapter : Gloading.Adapter {
    override fun getView(holder: Gloading.Holder, convertView: View?, status: Int): View? {
        var loadingStatusView: GlobalLoadingStatusView? = null
        //reuse the old view, if possible
        if (convertView != null && convertView is GlobalLoadingStatusView) {
            loadingStatusView = convertView
        }
        if (loadingStatusView == null) {
            loadingStatusView = GlobalLoadingStatusView(holder.context, holder.retryTask)
        }
        loadingStatusView.setStatus(status)
        return loadingStatusView
    }

    internal class GlobalLoadingStatusView : RelativeLayout {

        /*override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            MeasureSpec.makeMeasureSpec(DisplayUtil.getScreenWidth(context),MeasureSpec.EXACTLY)
            MeasureSpec.makeMeasureSpec(DisplayUtil.getScreenHeight(context),MeasureSpec.EXACTLY)
            setMeasuredDimension(,)
        }*/

        constructor(context: Context?, retryTask: Runnable?):super(context){
            gravity = Gravity.CENTER
            isFocusable = true
            isClickable = true
            val progressBar = ProgressBar(context)
            addView(progressBar)
        }

        fun setStatus(status: Int) { //change ui by different status...
            if(status == Gloading.STATUS_LOADING){
                visibility = View.VISIBLE
            }else if (status == Gloading.STATUS_LOAD_SUCCESS){
                visibility = View.GONE
            }
        }
    }
}