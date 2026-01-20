package io.github.tomhula.jecnaapi.data.grade

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.util.Name

/**
 * Final grade, which is shown for each subject at the end of the school year half.
 */
@Serializable
sealed interface FinalGrade
{
    /**
     * A closed final grade.
     */
    @Serializable
    data class Grade(
        val value: Int,
        val subject: Name? = null
    ) : FinalGrade
    {
        init
        {
            require(value in 1..5) { "Grade value must be between 1 and 5. (got $value)" }
        }
    }

    /**
     * Warning, when student has bad grades.
     */
    @Serializable
    data object GradesWarning : FinalGrade

    /**
     * Warning, when student doesn't have enough grades to be qualified.
     * Happens, when he is absent on tests.
     */
    @Serializable
    data object AbsenceWarning : FinalGrade

    /**
     * Student has both [GradesWarning] and [AbsenceWarning] at once.
     */
    @Serializable
    data object GradesAndAbsenceWarning : FinalGrade

    /**
     * Student is excused from the subject. (U)
     */
    @Serializable
    data object Excused: FinalGrade
}
