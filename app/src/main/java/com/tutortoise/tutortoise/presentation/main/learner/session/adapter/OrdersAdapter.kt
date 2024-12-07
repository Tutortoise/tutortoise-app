package com.tutortoise.tutortoise.presentation.main.learner.session.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.databinding.ItemSessionBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator
import com.tutortoise.tutortoise.utils.isoToReadableDate
import com.tutortoise.tutortoise.utils.isoToReadableTime

class OrdersAdapter(private val orders: List<OrderResponse>) :
    RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding, parent.context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    inner class OrderViewHolder(
        private val binding: ItemSessionBinding,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
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
                }
            }
        }
    }


    override fun getItemCount(): Int = orders.size
}

