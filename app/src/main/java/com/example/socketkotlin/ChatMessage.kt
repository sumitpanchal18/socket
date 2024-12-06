package com.example.socketkotlin


import com.google.gson.Gson

data class ChatMessage(
    val id: String,
    val message: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): String = Gson().toJson(this)
}
