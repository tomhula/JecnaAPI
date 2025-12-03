@file:UseSerializers(LocalDateSerializer::class)

package me.tomasan7.jecnaapi.data.absence

import java.time.LocalDate
import kotlinx.serialization.UseSerializers
import me.tomasan7.jecnaapi.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable

/**
 * Represents a single day with absences.
 */
@Serializable
data class AbsenceDay(
    val date: LocalDate,
    val hoursAbsent: Int,
    val textAfter: String?,
    val unexcusedHours: Int,
    val numLateEntries: Int
)
