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

        // Fetch subjects
        fetchSubjects()

        binding.btnBack.setOnClickListener {
            backToHome()
        }
    }

    private fun fetchSubjects() {
        val recyclerView: RecyclerView = findViewById(R.id.rvSubjects)
        recyclerView.layoutManager = GridLayoutManager(this, 3) //
        lifecycleScope.launch {
            val subjects = subjectRepository.fetchSubjects() // Replace with your API call
            recyclerView.adapter = subjects?.data?.let { SubjectsAdapter(it) }
        }
    }

    private fun backToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("startFragment", "home")
        startActivity(intent)
        finish()

    }
}