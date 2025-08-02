package com.tonydon.music_tangjian.data

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    var stream: Boolean = false
)