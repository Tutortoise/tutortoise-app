package com.tutortoise.tutortoise.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ChatRoom
import com.tutortoise.tutortoise.databinding.ItemChatRoomBinding
import com.tutortoise.tutortoise.domain.AuthManager
import com.tutortoise.tutortoise.utils.Constants
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ChatRoomAdapter(
    private val onRoomClick: (ChatRoom) -> Unit
) : ListAdapter<ChatRoom, ChatRoomAdapter.RoomViewHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemChatRoomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoomViewHolder(
        private val binding: ItemChatRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(room: ChatRoom) {
            binding.apply {
                val isLearner = AuthManager.getInstance()?.getUserRole() == "learner"

                // Set name based on role
                tvName.text = if (isLearner) {
                    room.tutorName
                } else {
                    room.learnerName
                }

                tvLastMessage.text = when {
                    room.lastMessage == null -> "No messages yet"
                    room.lastMessage.type == "image" -> root.context.getString(R.string.message_type_photo)
                    else -> room.lastMessage.content
                }

                tvTime.text = formatTime(room.lastMessageAt)

                val profileUserId = if (isLearner) {
                    room.tutorId
                } else {
                    room.learnerId
                }

                Glide.with(root.context)
                    .load(Constants.getProfilePictureUrl(profileUserId))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(profileImage)

                root.setOnClickListener { onRoomClick(room) }

                // Show unread count if > 0
                tvUnreadCount.visibility = if (room.unreadCount > 0) View.VISIBLE else View.GONE
                tvUnreadCount.text = room.unreadCount.toString()
            }
        }
    }
}

fun formatTime(isoTime: String): String {
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")

        val date = parser.parse(isoTime) ?: return "Unknown time"
        val timeMillis = date.time
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timeMillis

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        }
    } catch (e: Exception) {
        return "Unknown time"
    }
}

class RoomDiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom) =
        oldItem == newItem
}