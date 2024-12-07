package com.tutortoise.tutortoise.presentation.main.tutor.sessions.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.databinding.ItemPendingTutorBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator
import com.tutortoise.tutortoise.utils.isoToReadableDate
import com.tutortoise.tutortoise.utils.isoToReadableTime

class PendingOrdersAdapter(
    private val orders: List<OrderResponse>,
    private val onAcceptClick: (String) -> Unit = {},
    private val onRejectClick: (String) -> Unit = {}
) :
    RecyclerView.Adapter<PendingOrdersAdapter.PendingOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingOrderViewHolder {
        val binding = ItemPendingTutorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PendingOrderViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PendingOrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    inner class PendingOrderViewHolder(private val binding: ItemPendingTutorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(order: OrderResponse) {
            binding.apply {
                tvLearnerName.text = order.learnerName
                tvCategory.text = order.categoryName
                tvTime.text =
                    "${isoToReadableTime(order.sessionTime)} - ${isoToReadableTime(order.estimatedEndTime)}"
                tvDate.text = isoToReadableDate(order.sessionTime)
                tvPrice.text = "Rp. ${order.price.formatWithThousandsSeparator()},-"
                tvNote.text = order.notes?.takeIf { it.isNotEmpty() } ?: "-"

                Glide.with(root)
                    .load(Constants.getProfilePictureUrl(order.learnerId))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivProfilePicture)

                when (order.typeLesson) {
                    "offline" -> {
                        tvTypeLesson.setBackgroundResource(R.drawable.bg_dark_blue)
                        tvTypeLesson.text = "On-site"
                    }

                    "online" -> {
                        tvTypeLesson.setBackgroundResource(R.drawable.bg_green)
                        tvTypeLesson.text = "Online"
                    }

                }

                ivDropdown.setOnClickListener {
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

    override fun getItemCount(): Int = orders.size
}