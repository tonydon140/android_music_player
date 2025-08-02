package com.tonydon.music_tangjian.adapter.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tonydon.music_tangjian.R
import com.tonydon.music_tangjian.db.entity.ConversationEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ConversationAdapter(
    private val onDelete: (Long) -> Unit, private val onSelected: (Long) -> Unit
) : ListAdapter<ConversationEntity, ConversationAdapter.ViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        // 将 XML 布局文件转换为 View 对象
        val chatView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false);
        return ViewHolder(chatView);
    }

    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) {
        val conversation = getItem(position)
        holder.title.text = conversation.title
        holder.time.text = formatTimestamp(conversation.createdAt)

        // 删除对话
        holder.deleteIV.setOnClickListener {
            onDelete(conversation.id)
        }
        // 选择对话
        holder.itemView.setOnClickListener {
            onSelected(conversation.id)
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val zoneId = ZoneId.systemDefault() // 可替换为 ZoneId.of("Asia/Shanghai")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return formatter.format(instant.atZone(zoneId))
    }


    class DiffCallback : DiffUtil.ItemCallback<ConversationEntity>() {
        override fun areItemsTheSame(
            oldItem: ConversationEntity, newItem: ConversationEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ConversationEntity, newItem: ConversationEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deleteIV: ImageView = itemView.findViewById(R.id.iv_conv_delete)
        val title: TextView = itemView.findViewById(R.id.tv_conv_title)
        val time: TextView = itemView.findViewById(R.id.tv_conv_time)
    }
}