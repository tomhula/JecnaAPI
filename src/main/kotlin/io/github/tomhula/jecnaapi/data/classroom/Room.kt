package io.github.tomhula.jecnaapi.data.classroom

import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import io.github.tomhula.jecnaapi.data.timetable.Timetable

/**
 * A classroom in the school. Can have a title, floor, and a manager or be a main classroom of some class (then the manager is the teacher).
 * Timetable is optional.
 */
data class Room(
    val name: String,
    val floor: String?,
    val homeroomOf: String? = null,
    val manager: TeacherReference? = null,
    val timetable: Timetable? = null
    )
{

    override fun toString(): String
    {
        return "Classroom(title='$name', floor='$floor', mainClassroomOf=$homeroomOf, manager=$manager, timetable=$timetable)"
    }
}
