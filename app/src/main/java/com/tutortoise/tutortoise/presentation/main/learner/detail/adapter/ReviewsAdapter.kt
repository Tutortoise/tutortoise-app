package com.tutortoise.tutortoise.presentation.main.learner.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ReviewResponse

class ReviewsAdapter(
    private val reviews: List<ReviewResponse>,
) : RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    inner class ReviewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLearnerName: TextView = view.findViewById(R.id.tvLearnerName)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvLearnerReview: TextView = view.findViewById(R.id.tvLearnerReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_review_card, parent, false)
        return ReviewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val review = reviews[position]

        holder.apply {
            tvLearnerName.text = review.learnerName
            tvRating.text = review.rating.toString()
            tvLearnerReview.text = review.message
        }
    }

    override fun getItemCount(): Int = reviews.size
}

