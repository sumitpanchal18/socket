package com.example.socketkotlin


import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.UUID

data class ChatMessage(
    @SerializedName("id") val id: String = UUID.randomUUID().toString(),
    @SerializedName("senderId") val senderId: String,
    @SerializedName("receiverId") val receiverId: String,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
) : Serializable