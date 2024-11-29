package com.tutortoise.tutortoise.presentation.main.learner.explore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ExploreTutoriesResponse
import com.tutortoise.tutortoise.data.model.LessonType
import com.tutortoise.tutortoise.utils.Constants

class ExploreAdapter(private var tutories: List<ExploreTutoriesResponse>) :
    RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

    inner class ExploreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTutorName: TextView = view.findViewById(R.id.tvTutorName)
        private val tvSubjectName: TextView = view.findViewById(R.id.tvTutorSubject)
        private val tvRating: TextView = view.findViewById(R.id.tvRating)
        private val tvHourlyRate: TextView = view.findViewById(R.id.tvHourlyRate)
        private val tvCity: TextView = view.findViewById(R.id.tvCity)
        private val tvOnsite: TextView = view.findViewById(R.id.tvOnsite)
        private val tvOnline: TextView = view.findViewById(R.id.tvOnline)
        private val ivTutorImage: ImageView = view.findViewById(R.id.ivTutorImage)

        fun bind(tutoriesItem: ExploreTutoriesResponse) {
            tvTutorName.text = tutoriesItem.tutorName
            tvSubjectName.text = tutoriesItem.subjectName
            tvRating.text = tutoriesItem.avgRating.toString()
            tvHourlyRate.text = "Rp. ${tutoriesItem.hourlyRate} / Hour"
            tvCity.text = tutoriesItem.city

            tvOnsite.visibility = View.GONE
            tvOnline.visibility = View.GONE
            when (tutoriesItem.typeLesson) {
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

