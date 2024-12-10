package com.tutortoise.tutortoise.presentation.main.learner.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.data.model.TutoriesRecommendation
import com.tutortoise.tutortoise.databinding.ItemTutorCardBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator

class RecommendationAdapter(private val recommendations: List<TutoriesRecommendation>) :
    RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    // ViewHolder with View Binding
    inner class RecommendationViewHolder(private val binding: ItemTutorCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recommendation: TutoriesRecommendation) {
            binding.apply {
                tvTutoriesName.text = recommendation.name
                tvCategoryName.text = recommendation.category
                tvHourlyRate.text =
                    "Rp. ${recommendation.hourly_rate.formatWithThousandsSeparator()} / Hour"
                tvCity.text = recommendation.city

                tvOnsite.visibility = View.GONE
                tvOnline.visibility = View.GONE
                when (recommendation.type_lesson) {
                    "online" -> {
                        tvOnsite.visibility = View.GONE
                        tvOnline.visibility = View.VISIBLE
                    }

                    "offline" -> {
                        tvOnsite.visibility = View.VISIBLE
                        tvOnline.visibility = View.GONE
                    }

                    "both" -> {
                        tvOnsite.visibility = View.VISIBLE
                        tvOnline.visibility = View.VISIBLE
                    }
                }

                Glide.with(ivTutorImage.context)
                    .load(Constants.getProfilePictureUrl(recommendation.tutor_id))
                    .into(ivTutorImage)

                // TODO: rating
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding =
            ItemTutorCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    override fun getItemCount(): Int = recommendations.size
}
