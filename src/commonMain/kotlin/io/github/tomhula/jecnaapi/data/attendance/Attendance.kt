package io.github.tomhula.jecnaapi.data.attendance

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalTime

/**
 * Represents a single pass by the school entrance.
 * Can be either enter, or exit.
 *
 * @property type [AttendanceType] about whether the person exited or entered.
 * @property time The time, the person entered/exited.
 */
@Serializable
data class Attendance(
    val type: AttendanceType,
    val time: LocalTime
)
