package com.tutortoise.tutortoise.presentation.main.tutor.tutories.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.data.pref.TutoriesServiceModel
import com.tutortoise.tutortoise.databinding.ItemTutoriesBinding

class TutoriesAdapter : RecyclerView.Adapter<TutoriesAdapter.TutoriesViewHolder>() {

    private val tutoriesList = mutableListOf<TutoriesServiceModel>()

    fun submitList(tutories: List<TutoriesServiceModel>) {
        tutoriesList.clear()
        tutoriesList.addAll(tutories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutoriesViewHolder {
        val binding = ItemTutoriesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TutoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutoriesViewHolder, position: Int) {
        holder.bind(tutoriesList[position])
    }

    override fun getItemCount(): Int = tutoriesList.size

    inner class TutoriesViewHolder(private val binding: ItemTutoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tutory: TutoriesServiceModel) {
            binding.tvSubject.text = tutory.subject
            binding.tvPrice.text = "${tutory.ratePerHour} per hour"
            binding.tvFaceToFace.visibility = if (tutory.isFaceToFace) View.VISIBLE else View.GONE
            binding.tvOnline.visibility = if (tutory.isOnline) View.VISIBLE else View.GONE

            // Handle item click
            binding.root.setOnClickListener {
                // Navigate to detailed view or perform other actions
            }
        }
    }
}