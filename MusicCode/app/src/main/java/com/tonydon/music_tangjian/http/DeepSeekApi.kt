package com.tonydon.music_tangjian.http

import com.tonydon.music_tangjian.data.ChatRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface DeepSeekApi {
    @Streaming
    @POST("chat/completions")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/event-stream",
        "Connection: Keep-Alive",
        "Cache-Control: no-cache",
        "Authorization: Bearer sk-f4781e5be87e4ac9a316a32a3f11537b"
    )
    suspend fun streamChatCompletions(
        @Body request: ChatRequest
    ): ResponseBody
}

