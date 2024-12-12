package com.tutortoise.tutortoise.presentation.main.learner.payment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.databinding.ActivityPaymentDetailsBinding
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.capitalizeFirst
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator
import com.tutortoise.tutortoise.utils.isoToReadableTimeRange
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentDetailsBinding
    private val viewModel: PaymentViewModel by viewModels {
        PaymentViewModel.provideFactory(
            OrderRepository(this)
        )
    }

    private var tutoriesId = ""
    private var tutoriesName = ""
    private var tutorId = ""
    private var tutorName = ""
    private var categoryName = ""
    private var hourlyRate = 0
    private var typeLesson = ""
    private var totalHours = 0
    private var datetime = ""
    private var note = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tutoriesId = intent.getStringExtra("TUTORIES_ID") ?: ""
        tutorId = intent.getStringExtra("TUTOR_ID") ?: ""
        tutorName = intent.getStringExtra("TUTOR_NAME") ?: ""
        tutoriesName = intent.getStringExtra("TUTORIES_NAME") ?: ""
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        datetime = intent.getStringExtra("DATETIME") ?: ""
        hourlyRate = intent.getIntExtra("HOURLY_RATE", 0)
        typeLesson = intent.getStringExtra("TYPE_LESSON") ?: ""
        totalHours = intent.getIntExtra("TOTAL_HOURS", 0)
        note = intent.getStringExtra("NOTE") ?: ""

        setupUI()
    }


    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.apply {
            tvTutoriesName.text = tutoriesName
            tvTutorName.text = tutorName
            tvCategoryName.text = categoryName

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault()
            val date = inputFormat.parse(datetime)
            tvTutoringDate.text = outputFormat.format(date!!).capitalizeFirst()

            tvTutoringTime.text = isoToReadableTimeRange(datetime, totalHours)

            tvTotalHours.text = resources.getQuantityString(
                R.plurals.total_hours,
                totalHours,
                totalHours
            ) + " lesson"

            tvTutoringPrice.text = resources.getString(
                R.string.formatted_price,
                (hourlyRate.times(totalHours)).formatWithThousandsSeparator()
            )

            tvTotalPrice.text = resources.getString(
                R.string.formatted_price,
                (hourlyRate.times(totalHours) + 5000).formatWithThousandsSeparator()
            )

            tvOnlineStatus.visibility = View.GONE
            tvOnsiteStatus.visibility = View.GONE

            when (typeLesson) {
                "online" -> {
                    tvOnlineStatus.apply {
                        background =
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_indicator_active_online
                            )
                        findViewById<TextView>(R.id.tvOnline).setTextColor(Color.WHITE)
                        findViewById<ImageView>(R.id.ivOnline).setColorFilter(Color.WHITE)
                        visibility = View.VISIBLE
                    }
                }

                "offline" -> {
                    tvOnsiteStatus.apply {
                        background =
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_indicator_active_onsite
                            )
                        findViewById<TextView>(R.id.tvOnsite).setTextColor(Color.WHITE)
                        findViewById<ImageView>(R.id.ivOnsite).setColorFilter(Color.WHITE)
                        visibility = View.VISIBLE
                    }
                }
            }

            tvNote.text = note.takeIf { it.isNotEmpty() } ?: "-"

            Glide.with(this@PaymentActivity)
                .load(Constants.getProfilePictureUrl(tutorId))
                .into(binding.ivTutorImage)

            btnConfirm.setOnClickListener {
                viewModel.reserveOrder(
                    tutoriesId,
                    typeLesson,
                    datetime,
                    totalHours,
                    note
                )

                Toast.makeText(
                    this@PaymentActivity,
                    "You have successfully reserved a lesson!",
                    Toast.LENGTH_SHORT
                )
                    .show()

                finish()
            }
        }
    }
}