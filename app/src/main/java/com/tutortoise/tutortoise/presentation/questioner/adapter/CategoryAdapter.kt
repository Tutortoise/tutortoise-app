package com.tutortoise.tutortoise.presentation.questioner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val maxSelections: Int = 3,
    private val onSelectionChanged: (List<String>) -> Unit
) : ListAdapter<CategoryResponse, CategoryAdapter.ViewHolder>(CategoryDiffCallback()) {

    private val selectedCategories = mutableSetOf<String>()

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryResponse) {
            binding.apply {
                tvCategoryName.text = category.name

                Glide.with(root.context)
                    .load(category.iconUrl)
                    .into(ivCategoryIcon)

                root.isSelected = selectedCategories.contains(category.id)
                root.setBackgroundResource(R.drawable.category_item_background)

                root.setOnClickListener {
                    if (selectedCategories.contains(category.id)) {
                        selectedCategories.remove(category.id)
                    } else if (selectedCategories.size < maxSelections) {
                        selectedCategories.add(category.id)
                    }
                    root.isSelected = selectedCategories.contains(category.id)
                    onSelectionChanged(selectedCategories.toList())
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryResponse>() {
        override fun areItemsTheSame(oldItem: CategoryResponse, newItem: CategoryResponse) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CategoryResponse, newItem: CategoryResponse) =
            oldItem == newItem
    }
}