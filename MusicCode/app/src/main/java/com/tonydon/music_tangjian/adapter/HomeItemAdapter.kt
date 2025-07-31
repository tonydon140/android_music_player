package com.tonydon.music_tangjian.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.adapter.home.BannerAdapter
import com.tonydon.music_tangjian.adapter.home.CardAdapter
import com.tonydon.music_tangjian.io.MusicInfo
import com.tonydon.music_tangjian.io.MusicRecord
import com.youth.banner.Banner
import com.youth.banner.indicator.CircleIndicator


class HomeItemAdapter(
    val onPlay: (List<MusicInfo>, Int) -> Unit
) : BaseQuickAdapter<MusicRecord, QuickViewHolder>() {

    lateinit var bannerAdapter: BannerAdapter

    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: MusicRecord?
    ) {
        when (holder.itemViewType) {
            // Banner
            1 -> {
                val banner = holder.getView<Banner<MusicInfo, BannerAdapter>>(R.id.banner)
                bannerAdapter = BannerAdapter(item?.musicInfoList) { pos ->
                    onPlay(item!!.musicInfoList, pos)
                }
                banner.setAdapter(bannerAdapter)
                    .setIndicator(CircleIndicator(context)) // 默认圆点指示器
                    .setLoopTime(3000) // 自动轮播间隔
                    .isAutoLoop(true)
                    .start()
            }
            // 横滑大卡
            2 -> {
                val viewPager2 = holder.getView<ViewPager2>(R.id.viewPager2)
                val adapter = CardAdapter { pos ->
                    onPlay(item!!.musicInfoList, pos)
                }
                viewPager2.adapter = adapter
                adapter.submitList(item?.musicInfoList)
                viewPager2.apply {
                    offscreenPageLimit = 3  // 预加载两边页面
                    clipToPadding = false
                    clipChildren = false
                }
                viewPager2.setCurrentItem(1, false)
            }

            // 一行一列
            3 -> {
                val recyclerView = holder.getView<RecyclerView>(R.id.s3_recycler_view)
                val adapter = CardAdapter(155) { pos ->
                    onPlay(item!!.musicInfoList, pos)
                }
                recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
                recyclerView.adapter = adapter
                adapter.submitList(item?.musicInfoList)
            }

            // 一行两列
            4 -> {
                val recyclerView = holder.getView<RecyclerView>(R.id.s3_recycler_view)
                val adapter = CardAdapter(109) { pos ->
                    onPlay(item!!.musicInfoList, pos)
                }
                recyclerView.layoutManager = GridLayoutManager(context, 2)
                recyclerView.adapter = adapter
                adapter.submitList(item?.musicInfoList)
            }
        }
    }

    /**
     * 根据不同的 viewType 创建不同的视图
     */
    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return when (viewType) {
            1 -> QuickViewHolder(R.layout.item_style_1, parent)
            2 -> QuickViewHolder(R.layout.item_style_2, parent)
            3 -> QuickViewHolder(R.layout.item_style_3, parent)
            4 -> QuickViewHolder(R.layout.item_style_4, parent)
            else -> QuickViewHolder(R.layout.item_style_1, parent)
        }
    }

    /**
     * 不同的 style 对应不同的 viewType
     */
    override fun getItemViewType(position: Int, list: List<MusicRecord>): Int {
        return list[position].style
    }
}