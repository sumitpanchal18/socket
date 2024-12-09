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
import android.provider.Settings

import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var webSocketClient: ChatWebSocketClient
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    companion object {
//        private const val WEB_SOCKET_URL = "ws://152.58.62.254:8080"
        private const val WEB_SOCKET_URL = "ws://192.168.1.249:8080"
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

        // Generate unique IDs for the current user
        CURRENT_USER_ID = getUniqueUserId()

        // Initialize receiver ID dynamically (set later when assigned)
        RECEIVER_USER_ID = ""

        setupWebSocket()
        sendButton.setOnClickListener { sendMessage() }
    }
    private fun getUniqueUserId(): String {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        return UUID.nameUUIDFromBytes(androidId.toByteArray()).toString()
    }
    private fun setupWebSocket() {
        webSocketClient = ChatWebSocketClient(
            URI(WEB_SOCKET_URL),
            onMessageReceived = { messageJson ->
                val message = Gson().fromJson(messageJson, ChatMessage::class.java)

                // Handle dynamic receiver ID assignment
                if (message.senderId == "server") {
                    RECEIVER_USER_ID = message.message // Server sends the receiver ID
                } else {
                    runOnUiThread {
                        messageAdapter.addMessage(message)
                    }
                }
            },
            onConnectionOpen = {
                // Notify server of the current user's ID
                val userIdMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    message = CURRENT_USER_ID,
                    senderId = "server",
                    receiverId = "server"
                )
                webSocketClient.sendChatMessage(userIdMessage)

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
