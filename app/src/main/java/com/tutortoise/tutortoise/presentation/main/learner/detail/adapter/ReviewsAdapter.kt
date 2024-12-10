package com.tutortoise.tutortoise.presentation.main.learner.detail.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ReviewResponse
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.isoToRelativeDate

class ReviewsAdapter(
    private val reviews: List<ReviewResponse>,
) : RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    inner class ReviewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.ivLearnerImage)
        val tvLearnerName: TextView = view.findViewById(R.id.tvLearnerName)
        val tvTutoringTime: TextView = view.findViewById(R.id.tvTutoringTime)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val tvLearnerReview: TextView = view.findViewById(R.id.tvLearnerReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_review_card, parent, false)
        return ReviewsViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val review = reviews[position]

        holder.apply {
            Glide.with(itemView.context)
                .load(Constants.getProfilePictureUrl(review.learnerId))
                .placeholder(R.drawable.default_profile_picture)
                .error(R.drawable.default_profile_picture)
                .circleCrop()
                .into(profileImage)

            ratingBar.rating = review.rating
            tvTutoringTime.text = isoToRelativeDate(review.createdAt)
            tvLearnerName.text = review.learnerName

            if (!review.message.isNullOrBlank()) {
                tvLearnerReview.visibility = View.VISIBLE
                tvLearnerReview.text = review.message
            } else {
                tvLearnerReview.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = reviews.size
}