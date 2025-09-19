package com.tonydon.music_tangjian.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.tonydon.music_tangjian.data.*
import org.json.JSONObject

object RetrofitClient {
    val deepSeekApi: DeepSeekApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekApi::class.java)
    }

    val musicApi: MusicApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://hotfix-service-prod.g.mi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicApi::class.java)
    }

    /**
     * DeepSeek 流式响应
     */
    suspend fun streamResponse(
        content: String,
        onResponse: (String) -> Unit,
    ) = withContext(Dispatchers.IO) {
        // 构建请求体 (Data Class)
        val chatRequest = ChatRequest(
            model = "deepseek-chat",
            messages = listOf(ChatMessage("user", content)),
            stream = true
        )

        // 调用 Retrofit 接口并发起请求
        try {
            val responseBody = deepSeekApi.streamChatCompletions(chatRequest)

            // 3. 处理流式响应 (这部分逻辑与你之前完全相同)
            responseBody.use { body ->
                val source = body.source()
                while (!source.exhausted()) {
                    val line = source.readUtf8LineStrict()
                    if (!line.startsWith("data:")) continue

                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break

                    val responseContent = JSONObject(data)
                        .optJSONArray("choices")
                        ?.optJSONObject(0)
                        ?.optJSONObject("delta")
                        ?.optString("content", "")

                    if (!responseContent.isNullOrEmpty()) {
                        onResponse(responseContent)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}