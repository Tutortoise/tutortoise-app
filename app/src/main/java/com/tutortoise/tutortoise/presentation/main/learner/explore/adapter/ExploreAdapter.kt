package com.tutortoise.tutortoise.presentation.main.learner.explore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ExploreTutoriesResponse
import com.tutortoise.tutortoise.data.model.LessonType
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator

class ExploreAdapter(
    private var tutories: List<ExploreTutoriesResponse>,
    private val onItemClicked: (ExploreTutoriesResponse) -> Unit
) :
    RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

    inner class ExploreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTutoriesName: TextView = view.findViewById(R.id.tvTutoriesName)
        private val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        private val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        private val tvRating: TextView = view.findViewById(R.id.tvRating)
        private val tvTotalReviews: TextView = view.findViewById(R.id.tvTotalReviews)
        private val tvHourlyRate: TextView = view.findViewById(R.id.tvHourlyRate)
        private val tvCity: TextView = view.findViewById(R.id.tvCity)
        private val tvOnsite: TextView = view.findViewById(R.id.tvOnsite)
        private val tvOnline: TextView = view.findViewById(R.id.tvOnline)
        private val ivTutorImage: ImageView = view.findViewById(R.id.ivTutorImage)

        fun bind(tutoriesItem: ExploreTutoriesResponse) {
            tvTutoriesName.text = tutoriesItem.name
            tvCategoryName.text = tutoriesItem.categoryName

            // Handle if rating is 0
            if (tutoriesItem.avgRating == 0f) {
                ratingBar.visibility = View.GONE
                tvRating.text = "No rating"
                tvTotalReviews.visibility = View.GONE
            } else {
                ratingBar.visibility = View.VISIBLE
                ratingBar.rating = tutoriesItem.avgRating
                tvRating.text = tutoriesItem.avgRating.toString()
                tvTotalReviews.text =
                    itemView.context.resources.getQuantityString(
                        R.plurals.total_reviews,
                        tutoriesItem.totalReviews,
                        tutoriesItem.totalReviews
                    )
            }

            tvHourlyRate.text =
                itemView.context.resources.getString(
                    R.string.hourly_rate,
                    tutoriesItem.hourlyRate.formatWithThousandsSeparator()
                )
            tvCity.text = tutoriesItem.city

            tvOnsite.visibility = View.GONE
            tvOnline.visibility = View.GONE
            when (LessonType.fromString(tutoriesItem.typeLesson)) {
                LessonType.BOTH -> {
                    tvOnsite.visibility = View.VISIBLE
                    tvOnline.visibility = View.VISIBLE
                }

                LessonType.ONLINE -> tvOnline.visibility = View.VISIBLE
                LessonType.OFFLINE -> tvOnsite.visibility = View.VISIBLE
            }

            Glide.with(ivTutorImage.context)
                .load(Constants.getProfilePictureUrl(tutoriesItem.tutorId))
                .into(ivTutorImage)

            itemView.setOnClickListener { onItemClicked(tutoriesItem) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tutor_card, parent, false)
        return ExploreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        holder.bind(tutories[position])
    }

    override fun getItemCount(): Int = tutories.size

    fun updateItems(newItems: List<ExploreTutoriesResponse>) {
        tutories = newItems
        notifyDataSetChanged() // For simple implementation. Consider using DiffUtil for better performance
    }
}

