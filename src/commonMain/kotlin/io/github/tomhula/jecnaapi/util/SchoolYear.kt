package io.github.tomhula.jecnaapi.util

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.serialization.SchoolYearSerializer
import java.time.LocalDate
import java.time.Month

@Serializable(with = SchoolYearSerializer::class)
data class SchoolYear(val firstCalendarYear: Int): Comparable<SchoolYear>
{
    val secondCalendarYear: Int = firstCalendarYear + 1

    /**
     * Returns a calendar year the [month] is in based on this [SchoolYear]. Considers the summer holidays as a part of the ending [SchoolYear].
     */
    fun getCalendarYear(month: Month): Int
    {
        return if (month in FIRST_CALENDAR_YEAR_MONTHS)
            firstCalendarYear
        else
            secondCalendarYear
    }

    /**
     * @return [SchoolYearRange] created as if it would be an [IntRange] from this [firstCalendarYear] to [that] [firstCalendarYear].
     */
    operator fun rangeTo(that: SchoolYear) = SchoolYearRange(this, that)

    /**
     * @return new [SchoolYear], which is [increment] years after this one.
     */
    operator fun plus(increment: Int) = SchoolYear(firstCalendarYear + increment)

    /**
     * @return new [SchoolYear], which is [decrement] years before this one.
     */
    operator fun minus(decrement: Int) = SchoolYear(firstCalendarYear - decrement)


    /**
     * @return Whether the passed date is inside this [SchoolYear]. Considers the summer holidays as a part of the ending [SchoolYear].
     */
    operator fun contains(date: LocalDate): Boolean
    {
        return (date.year == firstCalendarYear
                && date.month in FIRST_CALENDAR_YEAR_MONTHS)
               ||
               (date.year == secondCalendarYear
                && date.month in SECOND_CALENDAR_YEAR_MONTHS)
    }

    override fun compareTo(other: SchoolYear) = firstCalendarYear.compareTo(other.firstCalendarYear)

    override fun equals(other: Any?): Boolean
    {
        other ?: return false

        if (other !is SchoolYear) return false

        return firstCalendarYear == other.firstCalendarYear
    }

    override fun hashCode() = firstCalendarYear

    override fun toString() = "$firstCalendarYear/$secondCalendarYear"

    companion object
    {
        /**
         * All [months][Month] in the first calendar year of a [SchoolYear]. (without summer holidays)
         */
        private val FIRST_CALENDAR_YEAR_MONTHS = Month.SEPTEMBER..Month.DECEMBER

        /**
         * All [months][Month] represented by their [Month.getValue] in the first calendar year of a [SchoolYear]. (without summer holidays)
         */
        private val FIRST_CALENDAR_YEAR_MONTHS_VALUES = FIRST_CALENDAR_YEAR_MONTHS.mapToIntRange { it.value }

        /**
         * All [months][Month] in the second calendar year of a [SchoolYear]. (with summer holidays)
         */
        private val SECOND_CALENDAR_YEAR_MONTHS = Month.JANUARY..Month.AUGUST

        /**
         * All [months][Month] represented by their [Month.getValue] in the second calendar year of a [SchoolYear]. (with summer holidays)
         */
        private val SECOND_CALENDAR_YEAR_MONTHS_VALUES = SECOND_CALENDAR_YEAR_MONTHS.mapToIntRange { it.value }

        /**
         * Constructs a [SchoolYear] this [date] belongs to. Considers the summer holidays as a part of the ending [SchoolYear].
         */
        @JvmStatic
        fun fromDate(date: LocalDate) = SchoolYear(if (date.month in FIRST_CALENDAR_YEAR_MONTHS) date.year else date.year - 1)

        /**
         * @return [SchoolYear] represented in [String]. The [string] **must be in `"firstYear/secondYear"` format.** (eg. 2021/2022)
         * @throws IllegalArgumentException When the [string] [String] is not in the correct format.
         * @see [SchoolYear.toString]
         */
        @JvmStatic
        fun fromString(string: String): SchoolYear
        {
            val split = string.split("/")

            val firstYear: Int
            val secondYear: Int

            try
            {
                firstYear = split[0].toInt()
                secondYear = split[1].toInt()
            }
            catch (e: Exception)
            {
                throw IllegalArgumentException("SchoolYear String wasn't in correct format. (firstYear/secondYear)")
            }

            if (firstYear + 1 != secondYear)
                throw IllegalArgumentException("SchoolYear String wasn't in correct format. SecondYear must be equal to firstYear + 1.")

            return SchoolYear(firstYear)
        }

        /**
         * @return Current [SchoolYear].
         */
        @JvmStatic
        fun current() = fromDate(LocalDate.now())
    }
}

/**
 * Creates [SchoolYear] from receiver [Int] as the first calendar year.
 * @receiver The first calendar year of the resulting [SchoolYear].
 */
fun Int.schoolYear() = SchoolYear(this)

/**
 * @return [SchoolYear] represented in [String]. The [String] **must be in `"firstYear/secondYear"` format.** (eg. 2021/2022)
 * @throws IllegalArgumentException When the [String] [String] is not in the correct format.
 * @see [SchoolYear.toString]
 */
fun String.toSchoolYear() = SchoolYear.fromString(this)

/**
 * Constructs a [SchoolYear] this [LocalDate] belongs to. Considers the summer holidays as a part of the ending [SchoolYear].
 */
fun LocalDate.schoolYear() = SchoolYear.fromDate(this)
