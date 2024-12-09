package com.tutortoise.tutortoise.presentation.main.learner.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.pref.ApiException
import com.tutortoise.tutortoise.data.repository.ReviewRepository
import com.tutortoise.tutortoise.databinding.FragmentDialogRatingTutorBinding
import kotlinx.coroutines.launch

class RatingTutorBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentDialogRatingTutorBinding? = null
    private val binding get() = _binding!!

    private var orderId: String = ""
    private var initialRating: Float = 0f
    private var onRatingSubmitted: ((String) -> Unit)? = null

    companion object {
        fun newInstance(
            orderId: String,
            initialRating: Float = 0f,
            onRatingSubmitted: (String) -> Unit
        ): RatingTutorBottomSheetFragment {
            return RatingTutorBottomSheetFragment().apply {
                this.orderId = orderId
                this.initialRating = initialRating
                this.onRatingSubmitted = onRatingSubmitted
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(R.id.bottomSheetHandler)?.parent as? View
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                it.background = ColorDrawable(Color.TRANSPARENT)
            }
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogRatingTutorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.background = ColorDrawable(Color.TRANSPARENT)

        (view.parent as? View)?.background = ColorDrawable(Color.TRANSPARENT)

        binding.ratingBar.rating = initialRating

        binding.btnSubmit.isEnabled = initialRating > 0
        binding.btnSubmit.alpha = if (initialRating > 0) 1f else 0.5f

        binding.ratingBar.setOnRatingBarChangeListener { _, _, _ ->
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.alpha = 1f
        }

        binding.btnSubmit.setOnClickListener {
            submitRating()
        }
    }

    private fun submitRating() {
        lifecycleScope.launch {
            try {
                val reviewRepository = ReviewRepository(requireContext())
                val result = reviewRepository.reviewOrder(
                    orderId,
                    binding.ratingBar.rating,
                    binding.etMessage.text.toString()
                )

                result.fold(
                    onSuccess = {
                        onRatingSubmitted?.invoke(orderId)
                        dismiss()
                    },
                    onFailure = { throwable ->
                        when (throwable) {
                            is ApiException -> {
                                Toast.makeText(
                                    requireContext(),
                                    throwable.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to review order",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("RatingBottomSheet", "Failed to review order", e)
            }
        }
    }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}