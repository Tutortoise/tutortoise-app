package com.tutortoise.tutortoise.presentation.main.learner.session

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.databinding.ItemPendingLearnerBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.isoToReadableDate
import com.tutortoise.tutortoise.utils.isoToReadableTime

class OrdersAdapter(private val orders: List<OrderResponse>) :
    RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemPendingLearnerBinding.inflate(
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

    inner class OrderViewHolder(private val binding: ItemPendingLearnerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(order: OrderResponse) {
            binding.tvTutoriesName.text = order.tutorName
            binding.tvCategory.text = order.categoryName
            binding.tvTime.text =
                "${isoToReadableTime(order.sessionTime)} - ${isoToReadableTime(order.estimatedEndTime)}"
            binding.tvDate.text = isoToReadableDate(order.sessionTime)
            binding.tvPrice.text = order.price.toString()

            Glide.with(binding.root)
                .load(
                    Constants.getProfilePictureUrl(order.tutorId)
                )
                .into(binding.ivProfilePicture)
        }
    }


    override fun getItemCount(): Int = orders.size
}

