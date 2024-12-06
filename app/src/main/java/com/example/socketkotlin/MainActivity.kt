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
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var webSocketClient: WebSocketClient
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    private val serverUrl = "ws://192.168.1.249:8080" // Replace with your server's IP
    private val senderId = "user123" // Sender's ID (could be dynamic, unique for each user)
    private val receiverId = "user456" // Receiver's ID (can be set dynamically based on the user you want to chat with)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageInput = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        messageRecyclerView = findViewById(R.id.messageRecyclerView)

        messageAdapter = MessageAdapter()
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = messageAdapter

        setupWebSocket()

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupWebSocket() {
        val uri = URI(serverUrl)

        webSocketClient = object : WebSocketClient(uri, Draft_6455()) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Toast.makeText(this@MainActivity, "Connected to chat server", Toast.LENGTH_SHORT).show()
            }

            override fun onMessage(message: String?) {
                message?.let {
                    val chatMessage = Gson().fromJson(it, ChatMessage::class.java)
                    runOnUiThread {
                        messageAdapter.addMessage(chatMessage)
                    }
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Disconnected from chat server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(ex: Exception?) {
                ex?.printStackTrace()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            webSocketClient.connectBlocking()
        }
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString()
        if (messageText.isNotEmpty()) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                message = messageText,
                senderId = senderId,
                receiverId = receiverId,
                timestamp = System.currentTimeMillis()
            )

            webSocketClient.send(Gson().toJson(message))
            runOnUiThread {
                messageInput.setText("")
                messageAdapter.addMessage(message)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}
