package com.tonydon.music_tangjian.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tonydon.music_tangjian.db.dao.ConversationDao
import com.tonydon.music_tangjian.db.dao.MessageDao
import com.tonydon.music_tangjian.db.entity.ConversationEntity
import com.tonydon.music_tangjian.db.entity.MessageEntity

@Database(entities = [ConversationEntity::class, MessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_app_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
