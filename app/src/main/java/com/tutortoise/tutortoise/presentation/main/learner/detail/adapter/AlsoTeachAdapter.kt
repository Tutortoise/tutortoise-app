package com.tutortoise.tutortoise.presentation.main.learner.detail.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.AlsoTeachesResponse
import com.tutortoise.tutortoise.presentation.main.learner.detail.DetailTutorActivity

class AlsoTeachAdapter(
    private val alsoTeachList: List<AlsoTeachesResponse>,
) : RecyclerView.Adapter<AlsoTeachAdapter.AlsoTeachViewHolder>() {

    inner class AlsoTeachViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val context = itemView.context

                    val intent = Intent(context, DetailTutorActivity::class.java).apply {
                        putExtra("TUTORIES_ID", alsoTeachList[position].id)
                    }

                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlsoTeachViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_also_teach, parent, false)
        return AlsoTeachViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlsoTeachViewHolder, position: Int) {
        val alsoTeachItem = alsoTeachList[position]
        holder.tvCategoryName.text = alsoTeachItem.categoryName
    }

    override fun getItemCount(): Int = alsoTeachList.size
}

