package io.github.tomhula.jecnaapi.data.classroom

import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import io.github.tomhula.jecnaapi.data.timetable.Timetable

class Classroom(
    val title: String,
    val floor: String?,
    val mainClassroomOf: String? = null,
    val manager: TeacherReference? = null,
    val timetable: Timetable? = null
    )
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Classroom

        if (title != other.title) return false
        if (floor != other.floor) return false
        if (mainClassroomOf != other.mainClassroomOf) return false
        if (manager != other.manager) return false
        if (timetable != other.timetable) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = title.hashCode()
        result = 31 * result + floor.hashCode()
        result = 31 * result + (mainClassroomOf?.hashCode() ?: 0)
        result = 31 * result + (manager?.hashCode() ?: 0)
        result = 31 * result + (timetable?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String
    {
        return "Classroom(title='$title', floor='$floor', mainClassroomOf=$mainClassroomOf, manager=$manager, timetable=$timetable)"
    }
}
