package io.github.tomhula.jecnaapi.data.substitution

import kotlinx.serialization.Serializable

@Serializable
data class AbsenceHours(
    val from: Int,
    val to: Int
)
