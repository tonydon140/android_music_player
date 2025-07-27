package com.tonydon.music_tangjian.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.tonydon.music_tangjian.io.MusicInfo
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder

class ImageAdapter(val mData: List<MusicInfo>?) : BannerImageAdapter<MusicInfo>(mData) {
    override fun onBindView(
        holder: BannerImageHolder,
        musicInfo: MusicInfo,
        position: Int,
        size: Int
    ) {
        Glide.with(holder.imageView)
            .load(musicInfo.coverUrl)
            .transform(CenterCrop(), RoundedCorners(40))
            .into(holder.imageView)
    }
}