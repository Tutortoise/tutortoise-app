package com.tutortoise.tutortoise.presentation.main.tutor.tutories.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.GetMyTutoriesResponse
import com.tutortoise.tutortoise.databinding.ItemTutoriesBinding
import com.tutortoise.tutortoise.utils.Constants


class TutoriesAdapter : RecyclerView.Adapter<TutoriesAdapter.TutoriesViewHolder>() {

    private val tutoriesList = mutableListOf<GetMyTutoriesResponse>()

    fun submitList(tutories: List<GetMyTutoriesResponse>) {
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

        fun bind(tutories: GetMyTutoriesResponse) {
            binding.tvSubject.text = tutories.subjectName
            binding.tvPrice.text = "Rp. ${tutories.hourlyRate} per hour"

            // Initially hide both type lessons
            binding.tvOnsite.visibility = View.GONE
            binding.tvOnline.visibility = View.GONE

            when (tutories.typeLesson) {
                "both" -> {
                    binding.tvOnsite.visibility = View.VISIBLE
                    binding.tvOnline.visibility = View.VISIBLE
                }

                "online" -> {
                    binding.tvOnline.visibility = View.VISIBLE
                }

                "offline" -> {
                    binding.tvOnsite.visibility = View.VISIBLE
                }
            }

            // Show subject image
            Glide.with(this@TutoriesViewHolder.itemView.context)
                .load(Constants.getSubjectIconUrl(tutories.subjectName))
                .into(binding.ivSubject)

            // Handle item click
            binding.root.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("tutoriesId", tutories.id)
                }
                itemView.findNavController().navigate(
                    R.id.action_tutories_to_editTutories,
                    bundle
                )
            }
        }
    }
}