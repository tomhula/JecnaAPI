package me.tomasan7.jecnaapi.data.absence

import kotlinx.serialization.Serializable

/**
 * Represents a data transfer object for absence information.
 */
@Serializable
data class AbsenceInfo(
    val hoursAbsent: Int,
    val unexcusedHours: Int,
    val textAfter: String?,
    val numLateEntries: Int
)
