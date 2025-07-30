package com.tonydon.music_tangjian.utils

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * 流式缓存：用户在听歌曲时，边播放边往本地磁盘写缓存，下次播放同一 URL 时直接走本地缓存，减少网络请求。
 */
@UnstableApi
object CacheManager {
    private const val MAX_CACHE_BYTES = 100L * 1024 * 1024  // 100MB
    private const val CACHE_DIR_NAME = "exo_media_cache"

    // 只保留最近最常用的缓存，超过容量即按 LRU 策略清理
    private val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES)
    private lateinit var simpleCache: SimpleCache

    /**
     * 初始化缓存管理器。建议在 Application.onCreate() 或 Service.onCreate() 中调用一次。
     */
    fun init(context: Context) {
        if (::simpleCache.isInitialized) return

        // 使用 StandaloneDatabaseProvider 代替已废弃的 ExoDatabaseProvider
        val databaseProvider: DatabaseProvider = StandaloneDatabaseProvider(context)
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)

        simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
    }

    /**
     * 获取全局唯一的 SimpleCache 实例
     */
    fun getCache(): SimpleCache {
        if (!::simpleCache.isInitialized) {
            throw IllegalStateException("CacheManager is not initialized. Call init(context) first.")
        }
        return simpleCache
    }

    fun buildCacheDataSourceFactory(context: Context): DataSource.Factory {
        // 上游：网络 / 本地
        val upstreamFactory = DefaultDataSource.Factory(context)
        // 写缓存：2MB 分片
        val cacheWriteSinkFactory = CacheDataSink.Factory()
            .setCache(getCache())
            .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)

        return CacheDataSource.Factory()
            .setCache(getCache())
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(cacheWriteSinkFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

}