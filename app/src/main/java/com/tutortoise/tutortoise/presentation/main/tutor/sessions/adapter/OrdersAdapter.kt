package com.tutortoise.tutortoise.presentation.main.tutor.sessions.adapter

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
        return OrderViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    inner class OrderViewHolder(private val binding: ItemSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(order: OrderResponse) {
            binding.apply {
                tvTutoriesName.text = order.learnerName
                tvCategory.text = order.categoryName
                tvTime.text =
                    "${isoToReadableTime(order.sessionTime)} - ${isoToReadableTime(order.estimatedEndTime)}"
                tvDate.text = isoToReadableDate(order.sessionTime)
                tvPrice.text = "Rp. ${order.price.formatWithThousandsSeparator()},-"

                Glide.with(root)
                    .load(Constants.getProfilePictureUrl(order.learnerId))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivProfilePicture)

                when (order.status) {
                    "completed" -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_green)
                        tvStatus.text = "Completed"
                    }

                    "pending" -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_yellow)
                        tvStatus.text = "Awaiting Confirmation"
                    }

                    "scheduled" -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_dark_green)
                        tvStatus.text = order.typeLesson
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = orders.size
}