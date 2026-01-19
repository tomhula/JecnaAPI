@file:UseSerializers(LocalTimeSerializer::class)

package io.github.tomhula.jecnaapi.data.attendance

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import io.github.tomhula.jecnaapi.serialization.LocalTimeSerializer
import kotlinx.datetime.LocalTime

/**
 * Represents single pass by the school entrance.
 * It can be either enter, or exit.
 *
 * @property type [AttendanceType] about whether the person exited or entered.
 * @property time The time, the person entered/exited.
 */
@Serializable
data class Attendance(
    val type: AttendanceType,
    val time: LocalTime
)
