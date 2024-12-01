package com.tutortoise.tutortoise.data.model

data class SubjectResponse(
    val id: String,
    val name: String,
    val iconUrl: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubjectResponse) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String = name
}