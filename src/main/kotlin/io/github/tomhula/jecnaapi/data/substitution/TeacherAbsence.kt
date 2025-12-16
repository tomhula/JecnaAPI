package io.github.tomhula.jecnaapi.data.substitution

import kotlinx.serialization.Serializable

@Serializable
data class TeacherAbsence(
    val teacher: String?,
    val teacherCode: String,
    val type: String,
    val hours: AbsenceHours?
)

