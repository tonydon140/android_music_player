package com.tonydon.music_tangjian.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.tonydon.music_tangjian.db.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface ConversationDao {
    @Insert
    suspend fun insert(conversationEntity: ConversationEntity): Long

    @Query("SELECT * FROM conversations ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<ConversationEntity>>

    @Delete
    suspend fun deleteConversation(conversationEntity: ConversationEntity)
}
