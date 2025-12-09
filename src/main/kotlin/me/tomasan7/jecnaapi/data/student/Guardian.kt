package me.tomasan7.jecnaapi.data.student

/**
 * Represents a student's guardian.
 */
data class Guardian(
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null
)
