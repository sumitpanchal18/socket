package com.example.socketkotlin

import android.util.Log
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.locks.ReentrantLock

class ChatWebSocketClient(
    private val serverUri: URI,
    private val onMessageReceived: (ChatMessage) -> Unit,
    private val onConnectionOpen: () -> Unit,
    private val onConnectionClose: () -> Unit
) : WebSocketClient(serverUri) {

    companion object {
        private const val TAG = "ChatWebSocketClient"
    }

    private val connectionLock = ReentrantLock()
    private val gson = Gson()

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d(TAG, "WebSocket connection opened")
        onConnectionOpen()
    }

    override fun onMessage(message: String?) {
        message?.let {
            try {
                val chatMessage = gson.fromJson(it, ChatMessage::class.java)
                onMessageReceived(chatMessage)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing message: ${e.message}")
            }
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d(TAG, "WebSocket connection closed. Code: $code, Reason: $reason")
        onConnectionClose()
    }

    override fun onError(ex: Exception?) {
        Log.e(TAG, "WebSocket error", ex)
    }

    fun sendChatMessage(message: ChatMessage) {
        if (isOpen) {
            try {
                val jsonMessage = gson.toJson(message)
                send(jsonMessage)
                Log.d(TAG, "Message sent: $jsonMessage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message", e)
            }
        } else {
            Log.e(TAG, "Cannot send message. Connection is not open.")
            reconnect()
        }
    }

    fun reconnectWebSocket() {
        if (!isOpen) {
            Log.d(TAG, "Attempting to reconnect...")
            reconnect()
        }
    }
}