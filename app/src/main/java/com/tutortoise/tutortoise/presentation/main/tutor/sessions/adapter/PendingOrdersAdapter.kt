package com.tutortoise.tutortoise.presentation.main.tutor.sessions.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.databinding.ItemDateHeaderBinding
import com.tutortoise.tutortoise.databinding.ItemPendingTutorBinding
import com.tutortoise.tutortoise.domain.ChatManager
import com.tutortoise.tutortoise.presentation.item.SessionListItem
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator
import com.tutortoise.tutortoise.utils.isoToReadableDate
import com.tutortoise.tutortoise.utils.isoToReadableTime

class PendingOrdersAdapter(
    private val items: List<SessionListItem>,
    private val onAcceptClick: (String) -> Unit = {},
    private val onRejectClick: (String) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                    ItemPendingTutorBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
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

    inner class SessionViewHolder(private val binding: ItemPendingTutorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrderResponse) {
            val learnerName = order.learnerName
            val categoryName = order.categoryName
            val sessionTime = isoToReadableTime(order.sessionTime)
            val date = isoToReadableDate(order.sessionTime)
            val estimatedEndTime = isoToReadableTime(order.estimatedEndTime)
            val price = order.price.formatWithThousandsSeparator()
            val notes = order.notes?.takeIf { it.isNotEmpty() } ?: "-"

            binding.apply {
                tvLearnerName.text = learnerName
                tvCategory.text = categoryName
                tvTime.text =
                    root.context.getString(
                        R.string.session_time_range,
                        sessionTime,
                        estimatedEndTime
                    )
                tvDate.text = date
                tvPrice.text = root.context.getString(R.string.formatted_price, price)
                tvNote.text = notes

                Glide.with(root)
                    .load(Constants.getProfilePictureUrl(order.learnerId))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivProfilePicture)

                btnChat.setOnClickListener {
                    ChatManager.navigateToChat(
                        context = root.context,
                        tutorId = order.tutorId,
                        tutorName = order.tutorName
                    )
                }

                when (order.typeLesson) {
                    "online" -> {
                        tvTypeLesson.setBackgroundResource(R.drawable.bg_green)
                        tvTypeLesson.text = root.context.getString(R.string.online)
                    }

                    "offline" -> {
                        tvTypeLesson.setBackgroundResource(R.drawable.bg_dark_blue)
                        tvTypeLesson.text = root.context.getString(R.string.onsite)
                    }

                }

                mainCardView.setOnClickListener {
                    if (expandableCardView.visibility == View.GONE) {
                        expandableCardView.visibility = View.VISIBLE
                        expandableContent.visibility = View.VISIBLE
                        ivDropdown.setImageResource(R.drawable.ic_up)
                    } else {
                        expandableCardView.visibility = View.GONE
                        expandableContent.visibility = View.GONE
                        ivDropdown.setImageResource(R.drawable.ic_dropdown)
                    }
                }

                btnAccept.setOnClickListener {
                    onAcceptClick(order.id)
                }

                btnReject.setOnClickListener {
                    onRejectClick(order.id)
                }

            }
        }
    }

    override fun getItemCount(): Int = items.size
}