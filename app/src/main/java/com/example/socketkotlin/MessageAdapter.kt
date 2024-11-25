package com.example.socketkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Message(val text: String)

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.text
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    // Inside MessageAdapter
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)  // Notify RecyclerView that a new message has been added
    }


    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageTextView: TextView = view.findViewById(android.R.id.text1)
    }
}
