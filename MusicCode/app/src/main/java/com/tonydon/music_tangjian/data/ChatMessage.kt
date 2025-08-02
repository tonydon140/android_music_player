package com.tonydon.music_tangjian.data

data class ChatMessage(
    val role: String,   // "user" 或 "assistant"
    var content: String
)