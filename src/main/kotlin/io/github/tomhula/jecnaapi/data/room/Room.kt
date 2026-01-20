package io.github.tomhula.jecnaapi.data.room

import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import io.github.tomhula.jecnaapi.data.timetable.Timetable

/**
 * A room in the school. Can have a timetable. Can be a home-classroom of some class (then the manager is usually their teacher).
 */
data class Room(
    val name: String,
    val roomCode : String,
    val floor: String?,
    val homeroomOf: String? = null,
    val manager: TeacherReference? = null,
    val timetable: Timetable? = null
)
