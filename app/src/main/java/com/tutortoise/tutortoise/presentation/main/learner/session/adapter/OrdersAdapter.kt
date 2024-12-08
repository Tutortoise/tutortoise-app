package com.tutortoise.tutortoise.presentation.main.learner.session.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.databinding.ItemDateHeaderBinding
import com.tutortoise.tutortoise.databinding.ItemSessionBinding
import com.tutortoise.tutortoise.presentation.item.SessionListItem
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator
import com.tutortoise.tutortoise.utils.isoToReadableDate
import com.tutortoise.tutortoise.utils.isoToReadableTime

class OrdersAdapter(
    private val items: List<SessionListItem>
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
                    ItemSessionBinding.inflate(
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

    inner class SessionViewHolder(
        private val binding: ItemSessionBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrderResponse) {
            binding.apply {
                val tutorName = order.tutorName
                val categoryName = order.categoryName
                val sessionTime = isoToReadableTime(order.sessionTime)
                val estimatedEndTime = isoToReadableTime(order.estimatedEndTime)
                val price = order.price.formatWithThousandsSeparator()

                // TODO: Not sure what to show here, tutories name or tutor name?
                tvTutoriesName.text = tutorName
                tvCategory.text = categoryName
                tvTime.text =
                    context.getString(R.string.session_time_range, sessionTime, estimatedEndTime)
                tvDate.text = isoToReadableDate(order.sessionTime)
                tvPrice.text = context.getString(R.string.formatted_price, price)

                Glide.with(root)
                    .load(
                        Constants.getProfilePictureUrl(order.tutorId)
                    )
                    .circleCrop()
                    .into(ivProfilePicture)

                when (order.status) {
                    "completed" -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_green)
                        tvStatus.text = context.getString(R.string.completed)
                    }

                    "pending" -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_yellow)
                        tvStatus.text = context.getString(R.string.awaiting_confirm)
                    }

                    "scheduled" -> {
                        when (order.typeLesson) {
                            "online" -> {
                                tvStatus.setBackgroundResource(R.drawable.bg_green)
                                tvStatus.text = context.getString(R.string.online)
                            }

                            "offline" -> {
                                tvStatus.setBackgroundResource(R.drawable.bg_dark_blue)
                                tvStatus.text = context.getString(R.string.onsite)
                            }
                        }
                    }

                    "declined" -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_red)
                        tvStatus.text = context.getString(R.string.rejected)
                    }

                    "canceled" -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_grey)
                        tvStatus.text = root.context.getString(R.string.canceled)
                    }
                }
            }
        }
    }


    override fun getItemCount(): Int = items.size
}