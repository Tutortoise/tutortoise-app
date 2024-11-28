package com.tutortoise.tutortoise.presentation.subjects

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.SubjectRepository
import com.tutortoise.tutortoise.databinding.ActivitySubjectsBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.presentation.subjects.adapter.SubjectsAdapter
import kotlinx.coroutines.launch

class SubjectsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubjectsBinding
    private lateinit var subjectRepository: SubjectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectRepository = SubjectRepository(this)

        // Fetch popular subjects
        fetchPopularSubjects()

        binding.btnBack.setOnClickListener {
            backToHome()
        }
    }

    private fun fetchPopularSubjects() {
        val recyclerView: RecyclerView = findViewById(R.id.rvSubjects)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        lifecycleScope.launch {
            // Try to fetch popular subjects first
            val popularSubjects = subjectRepository.fetchPopularSubjects()

            if (popularSubjects?.data != null) {
                recyclerView.adapter = SubjectsAdapter(popularSubjects.data)
            } else {
                // Fallback to regular subjects if popular subjects fetch fails
                val subjects = subjectRepository.fetchSubjects()
                recyclerView.adapter = subjects?.data?.let { SubjectsAdapter(it) }
            }
        }
    }

    private fun backToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("startFragment", "home")
        startActivity(intent)
        finish()
    }
}