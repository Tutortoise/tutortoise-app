package com.tutortoise.tutortoise.presentation.main.learner.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.AlsoTeachesResponse

class AlsoTeachAdapter(private val alsoTeachList: List<AlsoTeachesResponse>) :
    RecyclerView.Adapter<AlsoTeachAdapter.AlsoTeachViewHolder>() {

    inner class AlsoTeachViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubjectName: TextView = view.findViewById(R.id.tvSubjectName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlsoTeachViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_also_teach, parent, false)
        return AlsoTeachViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlsoTeachViewHolder, position: Int) {
        val alsoTeachItem = alsoTeachList[position]
        holder.tvSubjectName.text = alsoTeachItem.subjectName
    }

    override fun getItemCount(): Int = alsoTeachList.size
}

