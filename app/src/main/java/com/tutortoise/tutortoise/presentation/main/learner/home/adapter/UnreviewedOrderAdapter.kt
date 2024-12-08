package com.tutortoise.tutortoise.presentation.main.learner.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.databinding.ItemRatingTutorCardBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.isoToReadableDate

class UnreviewedOrderAdapter(
    private val onClosed: (String, Int) -> Unit,
    private val onCardClicked: (String) -> Unit,
    private val onRatingClicked: (String, Float) -> Unit
) : RecyclerView.Adapter<UnreviewedOrderAdapter.UnreviewedOrderViewHolder>() {

    val items = mutableListOf<OrderResponse>()

    fun submitList(list: List<OrderResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun removeItem(orderId: String) {
        val position = items.indexOfFirst { it.id == orderId }
        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnreviewedOrderViewHolder {
        val binding = ItemRatingTutorCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UnreviewedOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnreviewedOrderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class UnreviewedOrderViewHolder(private val binding: ItemRatingTutorCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrderResponse) {
            binding.apply {
                tvTutoriesName.text = order.tutorName
                tvCategoryName.text = order.categoryName
                tvDate.text = isoToReadableDate(order.sessionTime)

                Glide.with(root)
                    .load(Constants.getProfilePictureUrl(order.tutorId))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivTutorImage)

                ivClose.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        items.removeAt(position)
                        notifyItemRemoved(position)
                        onClosed(order.id, items.size)
                    }
                }

                cardRating.setOnClickListener {
                    onCardClicked(order.id)
                }

                ratingBar.setOnRatingBarChangeListener { _, star, _ ->
                    onRatingClicked(order.id, star)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}