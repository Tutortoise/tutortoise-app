package com.tutortoise.tutortoise.presentation.main.learner.subjects.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.SubjectResponse

class SubjectsAdapter(
    private val subjects: List<SubjectResponse>,
    private val onSubjectClick: ((SubjectResponse) -> Unit)? = null
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    inner class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectName: TextView = view.findViewById(R.id.subjectName)
        val subjectImage: ImageView = view.findViewById(R.id.subjectImage)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSubjectClick?.invoke(subjects[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.subjectName.text = subject.name
        Glide.with(holder.subjectImage.context)
            .load(subject.iconUrl)
            .into(holder.subjectImage)
    }

    override fun getItemCount(): Int = subjects.size
}

