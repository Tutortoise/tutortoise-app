package com.tutortoise.tutortoise.presentation.main.learner.categories

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.data.repository.CategoryRepository
import com.tutortoise.tutortoise.databinding.ActivityCategoriesBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.presentation.main.learner.categories.adapter.CategoriesAdapter
import kotlinx.coroutines.launch

class CategoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var categoryRepository: CategoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryRepository = CategoryRepository(this)

        // Fetch popular categories
        fetchPopularCategories()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchPopularCategories() {
        val recyclerView: RecyclerView = findViewById(R.id.rvCategories)
        recyclerView.layoutManager = FlexboxLayoutManager(this)

        lifecycleScope.launch {
            // Try to fetch popular categories first
            val popularCategories = categoryRepository.fetchPopularCategories()

            if (popularCategories?.data != null) {
                recyclerView.adapter =
                    CategoriesAdapter(popularCategories.data) { clickedCategory ->
                        navigateToExploreWithCategory(clickedCategory)
                    }
            } else {
                val categories = categoryRepository.fetchCategories()
                recyclerView.adapter = categories?.data?.let {
                    CategoriesAdapter(it) { clickedCategory ->
                        navigateToExploreWithCategory(clickedCategory)
                    }
                }
            }
        }
    }

    private fun navigateToExploreWithCategory(category: CategoryResponse) {
        // Using NavController to navigate if CategoriesActivity is hosting fragments
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("startFragment", "explore")
        intent.putExtra("categoryId", category.id)
        intent.putExtra("categoryName", category.name)
        startActivity(intent)
    }
}