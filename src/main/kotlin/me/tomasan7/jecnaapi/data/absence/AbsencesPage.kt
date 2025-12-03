@file:UseSerializers(LocalDateSerializer::class)

package me.tomasan7.jecnaapi.data.absence

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import me.tomasan7.jecnaapi.serialization.LocalDateSerializer
import me.tomasan7.jecnaapi.util.SchoolYear
import java.time.LocalDate

/**
 * A page of absences, containing multiple [AbsenceDay]-s.
 */
@Serializable
data class AbsencesPage private constructor(
    private val daysInternal: List<AbsenceDay>,
    val selectedSchoolYear: SchoolYear
) {
    @Transient
    val days: List<AbsenceDay> = daysInternal

    @Transient
    private val byDate: Map<LocalDate, AbsenceDay> = daysInternal.associateBy { it.date }

    operator fun get(date: LocalDate): AbsenceDay? = byDate[date]

    class Builder {
        private val days: MutableList<AbsenceDay> = mutableListOf()
        private lateinit var selectedSchoolYear: SchoolYear

        fun addDay(day: AbsenceDay): Builder {
            days.add(day)
            return this
        }

        fun addDay(date: LocalDate, hoursAbsent: Int, textAfter: String?, unexcusedHours: Int = 0, numLateEntries : Int): Builder {
            days.add(AbsenceDay(date, hoursAbsent, textAfter, unexcusedHours, numLateEntries))
            return this
        }

        fun setSelectedSchoolYear(selectedSchoolYear: SchoolYear): Builder {
            this.selectedSchoolYear = selectedSchoolYear
            return this
        }

        fun build(): AbsencesPage {
            check(::selectedSchoolYear.isInitialized) { "selectedSchoolYear has not been set." }
            return AbsencesPage(days.sortedBy { it.date }, selectedSchoolYear)
        }
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}

