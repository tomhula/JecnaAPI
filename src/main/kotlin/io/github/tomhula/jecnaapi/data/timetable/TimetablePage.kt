package io.github.tomhula.jecnaapi.data.timetable

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.serialization.LocalDateSerializer
import io.github.tomhula.jecnaapi.data.substitution.SubstitutionResponse
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.emptyMutableLinkedList
import io.github.tomhula.jecnaapi.util.setAll
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Whole timetable containing [LessonSpot]s for each day and their [LessonPeriod]s.
 */
@ConsistentCopyVisibility
@Serializable
data class TimetablePage internal constructor(
    val timetable: Timetable,
    val periodOptions: List<PeriodOption> = emptyList(),
    val selectedSchoolYear: SchoolYear
)
{
    override fun toString() = "TimetablePage(timetable=$timetable, periodOptions=$periodOptions, selectedSchoolYear=$selectedSchoolYear)"

    fun mergeSubstitutions(substitutionResponse: SubstitutionResponse, className: String): TimetablePage {
        return copy(timetable = timetable.withSubstitutions(substitutionResponse, className))
    }

    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }

    /**
     * Single period option in the Timetable page dropdown selection.
     *
     * @property id The id used by the website.
     * @property header An optional text before the dates. (eg. "Mimořádný rozvrh" or "Dočasný rozvrh".)
     * @property from The start [date][LocalDate] of this [option][PeriodOption].
     * @property to Optional end [date][LocalDate] of this [option][PeriodOption]. `null` means that this period will continue for unknown time.
     * @property selected Whether this [option][PeriodOption] is selected or not.
     */
    @Serializable
    data class PeriodOption(
        val id: Int,
        val header: String?,
        @Serializable(with = LocalDateSerializer::class)
        val from: LocalDate,
        @Serializable(with = LocalDateSerializer::class)
        val to: LocalDate?,
        val selected: Boolean = false
    )
    {
        override fun toString(): String
        {
            val fromStr = DATE_FORMAT.format(from)
            val toStr = to?.let { DATE_FORMAT.format(it) } ?: "?"

            return if (header != null)
                "$header: $fromStr - $toStr"
            else
                "$fromStr - $toStr"
        }

        companion object
        {
            private val DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.yyyy")
        }
    }

    class Builder
    {
        private val periodOptions: MutableList<PeriodOption> = emptyMutableLinkedList()
        private lateinit var selectedSchoolYear: SchoolYear
        private lateinit var timetable: Timetable

        /**
         * Sets all the [PeriodOptions][PeriodOption].
         *
         * @param periodOptions The [PeriodOptions][PeriodOption] to use.
         * @return This [builder's][Builder] instance back.
         */
        fun setPeriodOptions(periodOptions: List<PeriodOption>): Builder
        {
            this.periodOptions.setAll(periodOptions)
            return this
        }

        /**
         * Adds a [PeriodOption] to the [TimetablePage].
         *
         * @param periodOption The [PeriodOption] to add.
         * @return This [builder's][Builder] instance back.
         */
        fun addPeriodOption(periodOption: PeriodOption): Builder
        {
            periodOptions.add(periodOption)
            return this
        }

        fun setSelectedSchoolYear(selectedSchoolYear: SchoolYear): Builder
        {
            this.selectedSchoolYear = selectedSchoolYear
            return this
        }

        fun setTimetable(timetable: Timetable): Builder
        {
            this.timetable = timetable
            return this
        }

        fun build(): TimetablePage
        {
            check(::selectedSchoolYear.isInitialized) { "selectedSchoolYear has not been set." }
            check(::timetable.isInitialized) { "Timetable has not been set." }

            return TimetablePage(timetable, periodOptions, selectedSchoolYear)
        }
    }
}
