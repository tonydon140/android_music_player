package com.tonydon.music_tangjian.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 对话类
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // 用户可自定义会话标题
    val createdAt: Long = System.currentTimeMillis()
)
