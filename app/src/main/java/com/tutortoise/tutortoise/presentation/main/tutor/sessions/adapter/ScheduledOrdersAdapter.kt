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
import com.tutortoise.tutortoise.databinding.ItemScheduledTutorBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator
import com.tutortoise.tutortoise.utils.isoToReadableTime

class ScheduledOrdersAdapter(
    private val orders: List<OrderResponse>,
    private val onCancelled: (String) -> Unit,
) : RecyclerView.Adapter<ScheduledOrdersAdapter.ScheduledOrderViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduledOrderViewHolder {
        val binding = ItemScheduledTutorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduledOrderViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ScheduledOrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    inner class ScheduledOrderViewHolder(private val binding: ItemScheduledTutorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(order: OrderResponse) {
            binding.apply {
                tvLearnerName.text = order.learnerName
                tvCategory.text = order.categoryName
                tvTime.text =
                    "${isoToReadableTime(order.sessionTime)} - ${isoToReadableTime(order.estimatedEndTime)}"
                tvTotal.text = "Rp. ${order.price.formatWithThousandsSeparator()},-"
                tvNote.text = order.notes?.takeIf { it.isNotEmpty() } ?: "-"

                Glide.with(root)
                    .load(Constants.getProfilePictureUrl(order.learnerId))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivProfilePicture)

                when (order.typeLesson) {
                    "online" -> {
                        tvTypeLesson.setBackgroundResource(R.drawable.bg_green)
                        tvTypeLesson.text = "Online"
                    }

                    "offline" -> {
                        tvTypeLesson.setBackgroundResource(R.drawable.bg_dark_blue)
                        tvTypeLesson.text = "On-site"
                    }
                }

                ivExpandCollapse.setOnClickListener {
                    if (expandableCardView.visibility == View.GONE) {
                        expandableCardView.visibility = View.VISIBLE
                        expandableContent.visibility = View.VISIBLE
                        // TODO: Change the icon to icon up
                        ivExpandCollapse.setImageResource(R.drawable.ic_back)
                    } else {
                        expandableCardView.visibility = View.GONE
                        expandableContent.visibility = View.GONE
                        ivExpandCollapse.setImageResource(R.drawable.ic_dropdown)
                    }
                }

                btnCancel.setOnClickListener {
                    onCancelled(order.id)
                }

            }
        }
    }

    override fun getItemCount(): Int = orders.size


}