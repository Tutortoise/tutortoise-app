package com.tutortoise.tutortoise.presentation.main.tutor.home.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.databinding.ItemHomeScheduledSessionBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.isoToReadableDate
import com.tutortoise.tutortoise.utils.isoToReadableTime

class HomeScheduledSessionsAdapter(
    private var sessions: List<OrderResponse>,
    private val onChatClick: (String) -> Unit
) : RecyclerView.Adapter<HomeScheduledSessionsAdapter.ViewHolder>() {

    class ViewHolder(
        private val binding: ItemHomeScheduledSessionBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderResponse, onChatClick: (String) -> Unit) {
            binding.apply {
                tvLearnerName.text = order.learnerName
                tvCategory.text = order.categoryName
                tvTime.text = context.getString(
                    R.string.session_time_range,
                    isoToReadableTime(order.sessionTime),
                    isoToReadableTime(order.estimatedEndTime)
                )
                tvDate.text = isoToReadableDate(order.sessionTime)

                when (order.typeLesson) {
                    "online" -> {
                        tvTypeLesson.setBackgroundResource(R.drawable.bg_green)
                        tvTypeLesson.text = context.getString(R.string.online)
                    }

                    "offline" -> {
                        tvTypeLesson.setBackgroundResource(R.drawable.bg_dark_blue)
                        tvTypeLesson.text = context.getString(R.string.onsite)
                    }
                }

                Glide.with(root)
                    .load(Constants.getProfilePictureUrl(order.learnerId))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivProfilePicture)

                btnChat.setOnClickListener { onChatClick(order.learnerId) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeScheduledSessionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sessions[position], onChatClick)
    }

    override fun getItemCount(): Int = sessions.size

    fun updateSessions(newSessions: List<OrderResponse>) {
        sessions = newSessions.sortedBy { it.sessionTime }
        notifyDataSetChanged()
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }
}