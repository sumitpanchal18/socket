package com.example.socketkotlin


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI

class MainActivity : AppCompatActivity() {
    private lateinit var webSocketClient: ChatWebSocketClient
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    companion object {
        private const val TAG = "MainActivity"
        private const val WEB_SOCKET_URL = "ws://192.168.1.249:8080"


        private const val CURRENT_USER_ID = "user123"
        private const val RECEIVER_USER_ID = "user456"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupWebSocket()
    }

    private fun initializeViews() {
        messageInput = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        messageRecyclerView = findViewById(R.id.messageRecyclerView)

        messageAdapter = MessageAdapter(CURRENT_USER_ID)
        messageRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupWebSocket() {
        try {
            val uri = URI(WEB_SOCKET_URL)
            webSocketClient = ChatWebSocketClient(
                uri,
                onMessageReceived = { message ->
                    runOnUiThread {
                        messageAdapter.addMessage(message)
                        messageRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                },
                onConnectionOpen = {
                    runOnUiThread {
                        Toast.makeText(this, "Connected to chat", Toast.LENGTH_SHORT).show()
                        sendButton.isEnabled = true
                    }
                },
                onConnectionClose = {
                    runOnUiThread {
                        Toast.makeText(this, "Disconnected from chat", Toast.LENGTH_SHORT).show()
                        sendButton.isEnabled = false
                        reconnectWebSocket()
                    }
                }
            )

            lifecycleScope.launch {
                connectWebSocket()
            }
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket setup error", e)
            Toast.makeText(this, "Error setting up chat", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun connectWebSocket() = withContext(Dispatchers.IO) {
        try {
            webSocketClient.connectBlocking()
        } catch (e: InterruptedException) {
            Log.e(TAG, "WebSocket connection interrupted", e)
        }
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        if (messageText.isNotEmpty()) {
            val message = ChatMessage(
                senderId = CURRENT_USER_ID,
                receiverId = RECEIVER_USER_ID,
                message = messageText
            )

            try {
                webSocketClient.sendChatMessage(message)
                messageInput.setText("")
                messageAdapter.addMessage(message)
                messageRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reconnectWebSocket() {
        lifecycleScope.launch {
            webSocketClient.reconnectWebSocket()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}