package com.tutortoise.tutortoise.data.model

data class SubjectResponse(
    val id: String,
    val name: String,
    val iconUrl: String
) {
    override fun toString(): String {
        return name
    }
}
