package com.example.socketkotlin


data class ChatMessage(
    val sender: String,
    val content: String,
    val timestamp: String,
    val type: MessageType
)
