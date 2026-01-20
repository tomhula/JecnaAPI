package io.github.tomhula.jecnaapi.data.grade

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.util.Name
import kotlinx.datetime.LocalDate

/**
 * @property value       Grade's value. Is 0 for N.
 * @property small       Whether the grade is small or big. (it's weight)
 * @property teacher     The teacher, who gave you the grade.
 * @property description Description of the grade.
 * @property gradeId     The id of the grade in Jecna backend.
 */
@Serializable
data class Grade(
    val value: Int,
    val small: Boolean,
    val teacher: Name? = null,
    val description: String? = null,
    val receiveDate: LocalDate? = null,
    val gradeId: Int
)
{
    /**
     * Accepts value's representing char.
     *
     * @param valueChar The value's char representation.
     */
    constructor(
        valueChar: Char,
        small: Boolean,
        teacher: Name? = null,
        description: String? = null,
        receiveDate: LocalDate? = null,
        gradeId: Int
    ) : this(
        valueCharToValue(valueChar),
        small,
        teacher,
        description,
        receiveDate,
        gradeId
    )

    init
    {
        require(value in 0..5) { "Grade value must be between 0 and 5. (got $value)" }
    }

    /**
     * This grade's [value] as [Char]. Returns `'N'` for value `0`.
     */
    val valueChar = if (value == 0) 'N' else value.toString()[0]

    companion object
    {
        private val gradeCharValues = listOf('N', '0', '1', '2', '3', '4', '5')

        /**
         * Converts value's char representation to its value.
         */
        private fun valueCharToValue(valueChar: Char): Int
        {
            require(valueChar in gradeCharValues)

            return if (valueChar == 'N') 0 else valueChar.toString().toInt()
        }
    }
}
