package com.tonydon.music_tangjian.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.tonydon.music_tangjian.db.entity.ConversationEntity

@Dao
interface ConversationDao {
    @Insert
    suspend fun insert(conversationEntity: ConversationEntity): Long

    @Query("SELECT * FROM conversations ORDER BY createdAt DESC")
    fun getAll(): List<ConversationEntity>

    @Delete
    suspend fun deleteConversation(conversationEntity: ConversationEntity)
}
