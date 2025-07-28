package com.tonydon.music_tangjian.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.io.MusicInfo

class MusicListAdapter(
    private val onPlayClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : BaseDifferAdapter<MusicInfo, QuickViewHolder>(object : DiffUtil.ItemCallback<MusicInfo>() {
    override fun areItemsTheSame(old: MusicInfo, new: MusicInfo) = old.id == new.id
    override fun areContentsTheSame(old: MusicInfo, new: MusicInfo) = old == new
}) {

    private var playingId: Int? = null

    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: MusicInfo?
    ) {
        val music = getItem(position) ?: return

        val nameTV = holder.getView<TextView>(R.id.tv_list_name)
        val authorTV = holder.getView<TextView>(R.id.tv_list_author)
        val deleteIV = holder.getView<ImageView>(R.id.iv_list_remove)

        nameTV.text = music.musicName
        authorTV.text = music.author

        // 高亮当前播放的音乐
        if (playingId != null && music.id == playingId) {
            nameTV.setTextColor("#3325CD".toColorInt())
            authorTV.setTextColor("#993325CD".toColorInt())
        } else {
            authorTV.text = music.author
        }
        holder.itemView.setOnClickListener { onPlayClick(position) }
        deleteIV.setOnClickListener { onDeleteClick(position) }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_music_list, parent)
    }

    /** 外部调用，更新高亮 */
    fun setPlayingId(id: Int) {
        val old = playingId
        playingId = id
        // 刷新旧／新两条目
        old?.let { notifyItemChanged(items.indexOfFirst { it.id == old }) }
        notifyItemChanged(items.indexOfFirst { it.id == id })
    }
}