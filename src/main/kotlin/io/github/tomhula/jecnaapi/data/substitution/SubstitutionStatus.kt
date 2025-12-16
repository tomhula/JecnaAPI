package io.github.tomhula.jecnaapi.data.substitution

import kotlinx.serialization.Serializable

@Serializable
data class SubstitutionStatus(
    val lastUpdated: String,
    val currentUpdateSchedule: Int
)

