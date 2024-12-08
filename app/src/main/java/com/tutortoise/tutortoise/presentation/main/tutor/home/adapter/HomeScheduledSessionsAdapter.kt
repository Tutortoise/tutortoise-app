package com.tutortoise.tutortoise.presentation.main.tutor.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.databinding.ItemDateHeaderBinding
import com.tutortoise.tutortoise.databinding.ItemHomeScheduledSessionBinding
import com.tutortoise.tutortoise.presentation.item.SessionListItem
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.isoToReadableDate
import com.tutortoise.tutortoise.utils.isoToReadableTime

class HomeScheduledSessionsAdapter(
    private var items: List<SessionListItem>,
    private val onChatClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var lastAnimatedPosition = -1
    private val animationDelay = 50L

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_SESSION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SessionListItem.DateHeader -> TYPE_DATE
            is SessionListItem.SessionItem -> TYPE_SESSION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE -> {
                DateViewHolder(
                    ItemDateHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            TYPE_SESSION -> {
                SessionViewHolder(
                    ItemHomeScheduledSessionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    parent.context
                )
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val adapterPosition = holder.adapterPosition
        if (adapterPosition == RecyclerView.NO_POSITION) return

        if (adapterPosition > lastAnimatedPosition) {
            val animation = android.view.animation.AnimationUtils.loadAnimation(
                holder.itemView.context,
                R.anim.item_fade_in
            )
            animation.startOffset = adapterPosition * animationDelay
            holder.itemView.startAnimation(animation)
            lastAnimatedPosition = adapterPosition
        }

        when (val item = items[adapterPosition]) {
            is SessionListItem.DateHeader -> (holder as DateViewHolder).bind(item)
            is SessionListItem.SessionItem -> (holder as SessionViewHolder).bind(item.order)
        }
    }

    inner class DateViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SessionListItem.DateHeader) {
            binding.tvDate.text = item.date
        }
    }

    inner class SessionViewHolder(
        private val binding: ItemHomeScheduledSessionBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrderResponse) {
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

    override fun getItemCount(): Int = items.size

    fun updateSessions(newItems: List<SessionListItem>) {
        items = newItems
        lastAnimatedPosition = -1
        notifyDataSetChanged()
    }
}