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

class CompletedOrdersAdapter(
    private val orders: List<OrderResponse>,
) : RecyclerView.Adapter<CompletedOrdersAdapter.CompletedOrderViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedOrderViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CompletedOrderViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CompletedOrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    inner class CompletedOrderViewHolder(private val binding: ItemSessionBinding) :
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

                    "declined" -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_red)
                        tvStatus.text = "Rejected"
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = orders.size


}