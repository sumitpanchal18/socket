package com.example.socketkotlin


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var statusText: TextView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var webSocket: WebSocket
    private val messagesList = mutableListOf<ChatMessage>()
    private val TAG = "WebSocketDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        statusText = findViewById(R.id.statusText)

        // Setup RecyclerView
        chatAdapter = ChatAdapter(messagesList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        // Initialize WebSocket connection
        initializeWebSocket()

        // Setup send button click listener
        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageInput.setText("")
            }
        }
    }

    private fun initializeWebSocket() {
        val client = OkHttpClient.Builder()
            .build()

        val serverUrl =
            "ws://10.0.2.2:8888"

        Log.d(TAG, "Attempting to connect to: $serverUrl")

        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected Successfully")
                runOnUiThread {
                    statusText.text = "Connected"
                    Toast.makeText(
                        applicationContext,
                        "Connected to chat server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                try {
                    val jsonMessage = JSONObject(text)
                    Log.d(TAG, "Parsed JSON: $jsonMessage")

                    val chatMessage = ChatMessage(
                        jsonMessage.getString("sender"),
                        jsonMessage.getString("content"),
                        jsonMessage.getString("timestamp"),
                        MessageType.RECEIVED
                    )

                    runOnUiThread {
                        messagesList.add(chatMessage)
                        chatAdapter.notifyItemInserted(messagesList.size - 1)
                        recyclerView.smoothScrollToPosition(messagesList.size - 1)
                        Log.d(TAG, "UI updated with new message")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing received message: ${e.message}")
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closing: $code / $reason")
                runOnUiThread {
                    statusText.text = "Disconnecting..."
                    Toast.makeText(applicationContext, "Disconnecting: $reason", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Error: ${t.message}")
                t.printStackTrace()
                runOnUiThread {
                    statusText.text = "Connection Failed"
                    Toast.makeText(
                        applicationContext,
                        "Connection error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun sendMessage(message: String) {
        try {
            val jsonMessage = JSONObject().apply {
                put("sender", "User") // Replace with actual user ID
                put("content", message)
                put("timestamp", System.currentTimeMillis().toString())
            }

            val messageString = jsonMessage.toString()
            Log.d(TAG, "Sending message: $messageString")

            val sent = webSocket.send(messageString)
            Log.d(TAG, "Message sent successfully: $sent")

            val chatMessage = ChatMessage(
                "User",
                message,
                System.currentTimeMillis().toString(),
                MessageType.SENT
            )
            messagesList.add(chatMessage)
            chatAdapter.notifyItemInserted(messagesList.size - 1)
            recyclerView.smoothScrollToPosition(messagesList.size - 1)

        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket.close(1000, "App Closed")
    }
}