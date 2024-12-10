package com.tutortoise.tutortoise.presentation.chat.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
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
                val isCurrentUser = message.senderId == AuthManager.getCurrentUserId()
                val timestamp = formatTime(message.sentAt)

                // Hide all containers first
                sentMessageContainer.isVisible = false
                receivedMessageContainer.isVisible = false
                imageMessageContainer.isVisible = false

                when (message.type) {
                    "text" -> {
                        if (isCurrentUser) {
                            sentMessageContainer.isVisible = true
                            textMessage.text = message.content
                            sentTimeStamp.text = timestamp
                        } else {
                            receivedMessageContainer.isVisible = true
                            textReceivedMessage.text = message.content
                            receivedTimeStamp.text = timestamp
                        }
                    }

                    "image" -> {
                        imageMessageContainer.isVisible = true
                        imageMessage.isVisible = true // Make sure image view is visible

                        // Set container gravity based on sender
                        imageMessageContainer.layoutParams =
                            (imageMessageContainer.layoutParams as LinearLayout.LayoutParams).apply {
                                gravity = if (isCurrentUser) Gravity.END else Gravity.START
                            }

                        // Load image
                        Glide.with(itemView.context)
                            .load(message.content)
                            .placeholder(R.color.shimmer_color) // Add a placeholder
                            .error(R.drawable.ic_image_placeholder) // Add an error image
                            .into(imageMessage)

                        imageTimeStamp.text = timestamp
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