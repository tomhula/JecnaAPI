package io.github.tomhula.jecnaapi.data.substitution

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Response containing substitution schedule data.
 */
@Serializable
data class SubstitutionResponse(
    val schedule: List<Map<String, JsonElement>>,
    val props: List<SubstitutionProp>,
    val status: SubstitutionStatus
)
{
    /**
     * Returns absences for each day (same index as in [schedule]/[props]).
     */
    val absencesByDay: List<List<TeacherAbsence>> by lazy {
        schedule.map { dayEntry ->
            val rawAbsences = dayEntry["ABSENCE"]
            if (rawAbsences == null || rawAbsences is JsonNull) emptyList() else
            when (rawAbsences)
            {
                is JsonArray -> rawAbsences.mapNotNull { it.toTeacherAbsenceOrNull() }
                else -> emptyList()
            }
        }
    }

    /**
     * Returns absences for each day labeled with the corresponding date from [props].
     *
     * The list index still corresponds to the same index as in [schedule]/[props], but each
     * element carries the date label for easier consumption.
     */
    val labeledAbsencesByDay: List<LabeledTeacherAbsences> by lazy {
        props.mapIndexed { index, prop ->
            val absences = absencesByDay.getOrNull(index).orEmpty()
            LabeledTeacherAbsences(
                date = prop.date,
                absences = absences
            )
        }
    }
}

/**
 * Container for teacher absences on a specific day.
 *
 * @property date The date string associated with this day, as provided by [SubstitutionProp].
 * @property absences List of [TeacherAbsence] for this date.
 */
@Serializable
data class LabeledTeacherAbsences(
    val date: String,
    val absences: List<TeacherAbsence>
)

private fun JsonElement.toTeacherAbsenceOrNull(): TeacherAbsence?
{
    if (this !is JsonObject) return null

    val teacher = this["teacher"]?.let { it.toString().trim('"') }
    val teacherCode = this["teacherCode"]?.let { it.toString().trim('"') } ?: return null
    val type = this["type"]?.let { it.toString().trim('"') } ?: return null
    val hoursEl = this["hours"]

    val hours = if (hoursEl == null || hoursEl is JsonNull)
        null
    else
    {
        val obj = hoursEl.jsonObject
        val from = obj["from"]?.toString()?.toIntOrNull()
        val to = obj["to"]?.toString()?.toIntOrNull()
        if (from != null && to != null) AbsenceHours(from, to) else null
    }

    return TeacherAbsence(
        teacher = teacher,
        teacherCode = teacherCode,
        type = type,
        hours = hours
    )
}
