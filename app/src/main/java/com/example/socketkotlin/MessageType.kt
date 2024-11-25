package com.example.socketkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

enum class MessageType {
    SENT,
    RECEIVED
}


class ChatAdapter(private val messages: List<ChatMessage>) : 
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
        val senderText: TextView = view.findViewById(R.id.senderText)
        val timeText: TextView = view.findViewById(R.id.timeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutRes = if (viewType == MessageType.SENT.ordinal) {
            R.layout.item_message
        } else {
            R.layout.item_message_received
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.content
        holder.senderText.text = message.sender
        // Convert timestamp to readable format
        val formattedTime = android.text.format.DateFormat.format("HH:mm", message.timestamp.toLong())
        holder.timeText.text = formattedTime
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return messages[position].type.ordinal
    }
}