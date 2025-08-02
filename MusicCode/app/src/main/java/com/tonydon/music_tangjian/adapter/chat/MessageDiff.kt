package com.tonydon.music_tangjian.adapter.chat

import androidx.recyclerview.widget.DiffUtil
import com.tonydon.music_tangjian.data.Message

class MessageDiff : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(
        oldItem: Message,
        newItem: Message
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: Message,
        newItem: Message
    ): Boolean {
        return oldItem == newItem
    }
}
