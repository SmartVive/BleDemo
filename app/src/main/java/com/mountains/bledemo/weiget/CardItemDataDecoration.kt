package com.mountains.bledemo.weiget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CardItemDataDecoration(val margin:Int) : RecyclerView.ItemDecoration() {

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