package com.tonydon.music_tangjian.utils

import com.google.gson.Gson
import com.tonydon.music_tangjian.data.ChatMessage
import com.tonydon.music_tangjian.data.ChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object HttpUtils {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mediaType: MediaType = "application/json".toMediaType()
    private val requestBody = ChatRequest(
        "deepseek-chat",
        listOf(ChatMessage("user", ""))
    )

    /**
     * suspend 函数，只能执行中协程中
     * = withContext(Dispatchers.IO) 表示在 IO 线程池中执行
     */
    suspend fun streamResponse(
        content: String,
        onResponse: (String) -> Unit,
    ) = withContext(Dispatchers.IO) {
        // 获取 api 请求密钥
//         val apiKey = "sk-f4781e5be87e4ac9a316a32a3f11537b"
         val apiKey = "f4781e5be87e4ac9a316a32a3f11537b"
//        val apiKey = ApiKeyCryptoUtil.getDecryptedApiKey()
//        Log.d("mychatboot", "$apiKey")

        // 构建请求体
        requestBody.stream = true
        requestBody.messages[0].content = content
        val body = gson.toJson(requestBody)

        // 构建请求
        val request = Request.Builder()
            .url("https://api.deepseek.com/chat/completions")
            .header("Authorization", "Bearer sk-$apiKey")
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream") // 指示我们想要 SSE
            .header("Connection", "Keep-Alive")
            .header("Cache-Control", "no-cache")
            .post(body.toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            // 请求失败，抛出异常
            if (!response.isSuccessful) {
                throw okio.IOException("网络错误 $response")
            }

            // 获取数据源，为 null 直接返回
            val source = response.body?.source() ?: return@withContext

            // 判断输入流是否已经读取完毕
            while (!source.exhausted()) {
                val line = source.readUtf8LineStrict()
                if (!line.startsWith("data:")) continue

                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break

                // 解析内容
                val content = JSONObject(data)
                    .optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("delta")
                    ?.optString("content", "")

                if (!content.isNullOrEmpty()) {
                    onResponse(content)
                }
            }
        }
    }
}