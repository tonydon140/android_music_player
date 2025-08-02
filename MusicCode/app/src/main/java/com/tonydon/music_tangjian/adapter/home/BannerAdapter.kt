package com.tonydon.music_tangjian.adapter.home

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.data.MusicInfo
import com.tonydon.music_tangjian.service.PlayerManager
import com.youth.banner.adapter.BannerAdapter

class BannerAdapter(
    val mData: List<MusicInfo>?,
    val onPlay: (Int) -> Unit
) : BannerAdapter<MusicInfo, QuickViewHolder>(mData) {

    override fun onCreateHolder(
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder? {
        return QuickViewHolder(R.layout.item_card, parent)
    }

    override fun onBindView(
        holder: QuickViewHolder,
        data: MusicInfo?,
        position: Int,
        size: Int
    ) {
        val iv = holder.getView<ImageView>(R.id.iv_card)
        Glide.with(iv)
            .load(data?.coverUrl)
            .transform(CenterCrop(), RoundedCorners(40))
            .into(iv)
        holder.getView<TextView>(R.id.tv_author).text = data?.author
        holder.getView<TextView>(R.id.tv_music_name).text = data?.musicName
        // 点击 + 号，添加音乐到播放列表
        holder.getView<ImageButton>(R.id.ib_play).setOnClickListener {
            PlayerManager.addOne(data!!)
        }
        // 点击图片，播放音乐
        iv.setOnClickListener {
            onPlay(position)
        }
    }


    fun pxToDp(px: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (px * density).toInt()
    }
}