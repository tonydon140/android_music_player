package com.tonydon.music_tangjian.adapter.home

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.io.MusicInfo
import com.tonydon.music_tangjian.service.PlayerManager

/**
 * BaseQuickAdapter<T : Any, VH : RecyclerView.ViewHolder>
 * 内部数据类型默认是 List<T>
 */
class CardAdapter(
    val ivHeight: Int = -1,
    val onPlay: (Int) -> Unit
) : BaseQuickAdapter<MusicInfo, QuickViewHolder>() {
    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: MusicInfo?
    ) {
        val iv = holder.getView<ImageView>(R.id.iv_card)
        Glide.with(iv.context)
            .load(item?.coverUrl)
            .transform(CenterCrop(), RoundedCorners(40))
            .into(iv)

        holder.getView<TextView>(R.id.tv_author).text = item?.author
        holder.getView<TextView>(R.id.tv_music_name).text = item?.musicName
        // 点击 + 号，添加音乐到播放列表
        holder.getView<ImageButton>(R.id.ib_play).setOnClickListener {
            PlayerManager.addOne(item!!)
        }

        holder.itemView.setOnClickListener {
            onPlay(position)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        val holder = QuickViewHolder(R.layout.item_card, parent)
        if (ivHeight != -1) {
            val layout = holder.getView<ConstraintLayout>(R.id.card_root)
            layout.setPadding(
                layout.paddingLeft,
                layout.paddingTop,
                layout.paddingRight,
                pxToDp(10, holder.itemView.context)  // 设置底部 padding
            )
            // 设置 Card 的高度
            val lp = layout.layoutParams
            lp.height = pxToDp(ivHeight, holder.itemView.context)
            layout.layoutParams = lp
        }
        return holder
    }

    fun pxToDp(px: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (px * density).toInt()
    }
}