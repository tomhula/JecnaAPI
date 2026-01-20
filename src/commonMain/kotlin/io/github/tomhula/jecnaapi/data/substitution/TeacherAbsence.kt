package io.github.tomhula.jecnaapi.data.substitution

import kotlinx.serialization.Serializable

@Serializable
data class TeacherAbsence(
    val teacher: String?,
    val teacherCode: String?,
    /**
     * Absence type, e.g. wholeDay/single/range/exkurze/invalid.
     */
    val type: String,
    val hours: AbsenceHours?,
    /**
     * Original token/segment that couldn't be parsed (used when [type] is "invalid").
     */
    val original: String? = null,
    /**
     * Optional user-facing message (e.g. when the substitution endpoint is down).
     */
    val message: String? = null
)
