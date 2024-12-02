package com.tutortoise.tutortoise.presentation.main.learner.categories.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.CategoryResponse

class CategoriesAdapter(
    private val categories: List<CategoryResponse>,
    private val onCategoryClick: ((CategoryResponse) -> Unit)? = null
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val ivCategoryIcon: ImageView = view.findViewById(R.id.ivCategoryIcon)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClick?.invoke(categories[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategoryName.text = category.name
        Glide.with(holder.ivCategoryIcon.context)
            .load(category.iconUrl)
            .into(holder.ivCategoryIcon)
    }

    override fun getItemCount(): Int = categories.size
}

