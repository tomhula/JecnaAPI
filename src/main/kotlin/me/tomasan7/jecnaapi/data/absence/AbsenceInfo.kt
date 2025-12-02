package me.tomasan7.jecnaapi.data.absence

/**
 * Represent a data transfer object for absence information.
 */
data class AbsenceInfo(
    val hoursAbsent: Int,
    val isLateEntry: Boolean,
    val unexcusedHours: Int,
    val textAfter: String?
)