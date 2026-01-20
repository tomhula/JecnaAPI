package io.github.tomhula.jecnaapi.util

import io.github.tomhula.jecnaapi.data.timetable.TimetablePage
import kotlinx.datetime.Month
import kotlinx.datetime.number

/**
 * Converts periods/times from Kotlin objects to a Jecna URL query parameters.
 */
internal object JecnaPeriodEncoder
{
    /**
     * School year, whose id is `0`.
     */
    const val ID_ZERO_YEAR = 2008
    /**
     * Id of the first school year half.
     */
    const val FIRST_HALF_ID = 21
    /**
     * Id of the second school year half.
     */
    const val SECOND_HALF_ID = 22
    /**
     * URL query parameter key for the school year.
     */
    const val SCHOOL_YEAR_ID_KEY = "schoolYearId"
    /**
     * URL query parameter key for the school year half.
     */
    const val SCHOOL_YEAR_HALF_ID_KEY = "schoolYearHalfId"
    /**
     * URL query parameter key for the month.
     */
    const val MONTH_ID_KEY = "schoolYearPartMonthId"

    /**
     * URL query parameter key for the period.
     */
    val TIMETABLE_PERIOD_ID_KEY = "timetableId"

    /**
     * @param schoolYear The [SchoolYear] to encode.
     * @return School year URL query parameter as [Pair] of a key and a value.
     * @throws IllegalArgumentException when there is no id for the [schoolYear].
     */
    fun encodeSchoolYear(schoolYear: SchoolYear): Pair<String, Int>
    {
        require(schoolYear.firstCalendarYear >= 2008) { "Lowest supported school year is 2008/2009. (got $schoolYear)" }

        return SCHOOL_YEAR_ID_KEY to schoolYear.firstCalendarYear - ID_ZERO_YEAR
    }

    /**
     * @receiver The [SchoolYear] to encode.
     * @return School year URL query parameter as [Pair] of a key and a value.
     * @throws IllegalArgumentException when there is no id for this [SchoolYear].
     * @see encodeSchoolYear
     */
    fun SchoolYear.jecnaEncode() = encodeSchoolYear(this)

    /**
     * @param id The id of the school year.
     * @return [SchoolYear] by the id.
     * @throws IllegalArgumentException when the [id] is less than `0`.
     */
    fun decodeSchoolYear(id: Int): SchoolYear
    {
        require(id >= 0) { "Id cannot be less than 0. (got $id)" }

        return SchoolYear(ID_ZERO_YEAR + id)
    }

    /**
     * @param id The id of the school year.
     * @return [SchoolYear] by the id.
     * @throws IllegalArgumentException when the [id] is less than `0`.
     * @see decodeSchoolYear
     */
    fun SchoolYear.Companion.jecnaDecode(id: Int) = decodeSchoolYear(id)

    /**
     * @param schoolYearHalf The [SchoolYearHalf] to encode.
     * @return School year half URL query parameter as [Pair] of a key and a value.
     */
    fun encodeSchoolYearHalf(schoolYearHalf: SchoolYearHalf) = SCHOOL_YEAR_HALF_ID_KEY to when (schoolYearHalf)
    {
        SchoolYearHalf.FIRST -> FIRST_HALF_ID
        SchoolYearHalf.SECOND -> SECOND_HALF_ID
    }

    /**
     * @receiver The [SchoolYearHalf] to encode.
     * @return School year half URL query parameter as [Pair] of a key and a value.
     * @see encodeSchoolYearHalf
     */
    fun SchoolYearHalf.jecnaEncode() = encodeSchoolYearHalf(this)

    /**
     * @param id The id of the school year half.
     * @return The corresponding [SchoolYearHalf].
     * @throws IllegalArgumentException when the [id] corresponds to neither half.
     */
    fun decodeSchoolYearHalf(id: Int) = when (id)
    {
        FIRST_HALF_ID  -> SchoolYearHalf.FIRST
        SECOND_HALF_ID -> SchoolYearHalf.SECOND
        else           -> throw IllegalArgumentException("Id doesn't correspond to any year half. (got $id)")
    }

    /**
     * @param id The id of the school year half.
     * @return The corresponding [SchoolYearHalf].
     * @throws IllegalArgumentException when the [id] corresponds to neither half.
     * @see decodeSchoolYearHalf
     */
    fun SchoolYearHalf.jecnaDecode(id: Int) = decodeSchoolYearHalf(id)

    /**
     * @param monthValue The value of the month to encode.
     * @return Month URL query parameter as [Pair] of a key and a value.
     * @throws IllegalArgumentException when the [monthValue] isn't a valid month.
     */
    fun encodeMonth(monthValue: Int): Pair<String, Int>
    {
        require(monthValue in 1..12) { "Month must be between 1 and 12. (got $monthValue)" }

        return MONTH_ID_KEY to monthValue
    }

    /**
     * @param month The month to encode.
     * @return Month URL query parameter as [Pair] of a key and a value.
     */
    fun encodeMonth(month: Month) = encodeMonth(month.number)

    /**
     * @param periodOption The [TimetablePage.PeriodOption] to encode.
     * @return Timetable period URL query parameter as [Pair] of a key and a value.
     */
    fun encodeTimetablePeriod(periodOption: TimetablePage.PeriodOption) = TIMETABLE_PERIOD_ID_KEY to periodOption.id

    /**
     * @receiver The [TimetablePage.PeriodOption] to encode.
     * @return Timetable period URL query parameter as [Pair] of a key and a value.
     */
    fun TimetablePage.PeriodOption.jecnaEncode() = encodeTimetablePeriod(this)
}
