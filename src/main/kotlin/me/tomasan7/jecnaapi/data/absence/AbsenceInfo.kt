package me.tomasan7.jecnaapi.data.absence

/**
 * Represents a data transfer object for absence information.
 */
data class AbsenceInfo(
    val hoursAbsent: Int,
    val unexcusedHours: Int,
    val textAfter: String?,
    val numLateEntries: Int
)
