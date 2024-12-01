package com.tutortoise.tutortoise.presentation.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ChatMessage
import com.tutortoise.tutortoise.databinding.ItemChatMessageBinding
import com.tutortoise.tutortoise.domain.AuthManager

class ChatMessageAdapter :
    ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.apply {
                when (message.type) {
                    "text" -> {
                        textMessage.isVisible = true
                        imageMessage.isVisible = false
                        textMessage.text = message.content
                    }

                    "image" -> {
                        textMessage.isVisible = false
                        imageMessage.isVisible = true
                        Glide.with(imageMessage)
                            .load(message.content)
                            .into(imageMessage)
                    }
                }

                // Align messages based on sender
                val isCurrentUser = message.senderId == AuthManager.getCurrentUserId()
                messageContainer.gravity = if (isCurrentUser) Gravity.END else Gravity.START

                // Apply different background colors/drawables for sender/receiver
                if (message.type == "text") {
                    textMessage.apply {
                        setBackgroundResource(
                            if (isCurrentUser) R.drawable.bg_sent_message
                            else R.drawable.bg_received_message
                        )
                        setTextColor(
                            if (isCurrentUser)
                                context.getColor(android.R.color.black)
                            else
                                context.getColor(android.R.color.white)
                        )
                    }
                }
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
        oldItem == newItem
}