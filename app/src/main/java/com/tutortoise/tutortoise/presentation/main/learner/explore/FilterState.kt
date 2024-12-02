package com.tutortoise.tutortoise.presentation.main.learner.explore

import com.tutortoise.tutortoise.data.model.CategoryResponse

data class FilterState(
    val categories: Set<CategoryResponse> = emptySet(),
    val locations: Set<String> = emptySet(),
    val priceRange: PriceRange? = null,
    val rating: Float? = null,
    val lessonType: String? = null
)

data class PriceRange(
    val min: Int,
    val max: Int
) {
    companion object {
        fun fromString(range: String): PriceRange? {
            return when (range) {
                "Rp30rb-Rp50rb" -> PriceRange(30000, 50000)
                "Rp50rb-Rp80rb" -> PriceRange(50000, 80000)
                "Rp80rb-Rp119rb" -> PriceRange(80000, 119000)
                "Rp120rb++" -> PriceRange(120000, Int.MAX_VALUE)
                else -> null
            }
        }
    }
}