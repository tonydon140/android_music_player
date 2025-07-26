package com.tonydon.music_tangjian.adapter

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.io.MusicInfo
import com.tonydon.music_tangjian.io.MusicRecord

class StyleAdapter4 : BaseQuickAdapter<MusicInfo, QuickViewHolder>(){
    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: MusicInfo?
    ) {
        val iv = holder.getView<ImageView>(R.id.iv_card)
        Log.d("music_StyleAdapter3", item.toString())
        Glide.with(iv.context)
            .load(item?.coverUrl)
            .transform(CenterCrop(), RoundedCorners(40))
            .into(iv)

        holder.getView<TextView>(R.id.tv_author).text = item?.author
        holder.getView<TextView>(R.id.tv_music_name).text = item?.musicName
        holder.getView<ImageButton>(R.id.ib_play).setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                item?.musicName, Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_card_4, parent)
    }
}