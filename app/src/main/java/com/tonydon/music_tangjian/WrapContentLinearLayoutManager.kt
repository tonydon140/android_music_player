package com.tonydon.music_tangjian

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WrapContentLinearLayoutManager(context: Context) :
    LinearLayoutManager(context) {

    override fun canScrollVertically(): Boolean {
        return false // ✅ 不允许子 RecyclerView 滚动
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        val itemCount = state.itemCount
        var totalHeight = 0
        try {
            for (i in 0 until itemCount) {
                val view = recycler.getViewForPosition(i)
                measureChildWithMargins(view, 0, 0)
                val lp = view.layoutParams as RecyclerView.LayoutParams
                totalHeight += view.measuredHeight + lp.topMargin + lp.bottomMargin
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setMeasuredDimension(
            View.MeasureSpec.getSize(widthSpec),
            totalHeight + paddingTop + paddingBottom
        )
    }
}
