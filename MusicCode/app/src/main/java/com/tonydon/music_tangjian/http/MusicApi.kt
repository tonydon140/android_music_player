package com.tonydon.music_tangjian.http

import com.tonydon.music_tangjian.data.MusicRes
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApi {
    @GET("music/homePage")
    suspend fun query(
        @Query("current") current: Int,
        @Query("size") size: Int
    ): MusicRes
}