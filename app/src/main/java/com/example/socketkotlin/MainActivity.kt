package com.example.socketkotlin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var webSocketClient: ChatWebSocketClient
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    companion object {
        private const val WEB_SOCKET_URL =
            "ws://192.168.1.249:8080"
    }

    private var CURRENT_USER_ID: String = ""
    private var RECEIVER_USER_ID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageInput = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        messageRecyclerView = findViewById(R.id.messageRecyclerView)

        messageAdapter = MessageAdapter()
        messageRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        CURRENT_USER_ID = UUID.randomUUID().toString()
        RECEIVER_USER_ID =
            if (CURRENT_USER_ID == "user123") "user456" else "user123"

        setupWebSocket()
        sendButton.setOnClickListener { sendMessage() }
    }

    private fun setupWebSocket() {
        webSocketClient = ChatWebSocketClient(
            URI(WEB_SOCKET_URL),
            onMessageReceived = { messageJson ->
                val message = Gson().fromJson(messageJson, ChatMessage::class.java)
                runOnUiThread {
                    messageAdapter.addMessage(message)
                }
            },
            onConnectionOpen = {
                runOnUiThread {
                    Toast.makeText(this, "Connected to chat", Toast.LENGTH_SHORT).show()
                }
            },
            onConnectionClose = {
                runOnUiThread {
                    Toast.makeText(this, "Disconnected from chat", Toast.LENGTH_SHORT).show()
                }
            }
        )

        lifecycleScope.launch(Dispatchers.IO) {
            webSocketClient.connectBlocking()
        }
    }

    private fun sendMessage() {
        val text = messageInput.text.toString()
        if (text.isNotEmpty()) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                message = text,
                senderId = CURRENT_USER_ID,
                receiverId = RECEIVER_USER_ID
            )
            webSocketClient.sendChatMessage(message)
            runOnUiThread { messageInput.setText("") }
            messageAdapter.addMessage(message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}
