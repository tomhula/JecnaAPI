@file:UseSerializers(LocalDateSerializer::class)

package me.tomasan7.jecnaapi.data.absence

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import me.tomasan7.jecnaapi.serialization.LocalDateSerializer
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.LocalDate

/**
 * Holds absence info for each day.
 */
@Serializable
data class AbsencesPage private constructor(
    private val absences: Map<LocalDate, AbsenceInfo>,
    val selectedSchoolYear: SchoolYear
)
{
    /** All days, this AbsencesPage has data for. */
    @Transient
    val days: Set<LocalDate> = absences.keys

    /** @return AbsenceInfo for the provided day or null when no data is present. */
    operator fun get(day: LocalDate): AbsenceInfo? = absences[day]

    class Builder
    {
        private val absences: MutableMap<LocalDate, AbsenceInfo> = HashMap()
        private lateinit var selectedSchoolYear: SchoolYear

        /**
         * Sets the AbsenceInfo for the given day.
         */
        fun setAbsence(day: LocalDate, info: AbsenceInfo): Builder
        {
            absences[day] = info
            return this
        }

        fun setSelectedSchoolYear(selectedSchoolYear: SchoolYear): Builder
        {
            this.selectedSchoolYear = selectedSchoolYear
            return this
        }

        fun build(): AbsencesPage
        {
            check(::selectedSchoolYear.isInitialized) { "selectedSchoolYear has not been set." }
            return AbsencesPage(absences, selectedSchoolYear)
        }
    }

    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }
}
