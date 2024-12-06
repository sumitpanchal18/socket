package com.example.socketkotlin

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class ChatWebSocketClient(
    uri: URI,
    private val onMessageReceived: (String) -> Unit,
    private val onConnectionOpen: () -> Unit,
    private val onConnectionClose: () -> Unit
) : WebSocketClient(uri) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        onConnectionOpen()
    }

    override fun onMessage(message: String?) {
        message?.let { onMessageReceived(it) }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        onConnectionClose()
    }

    override fun onError(ex: Exception?) {
        ex?.printStackTrace()
    }

    fun sendChatMessage(chatMessage: ChatMessage) {
        send(chatMessage.toJson())
    }
}
