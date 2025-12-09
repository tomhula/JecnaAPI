package io.github.tomhula.jecnaapi.util

import java.time.LocalDate
import java.time.Month

enum class SchoolYearHalf
{
    FIRST,
    SECOND;

    companion object
    {
        /**
         * @return [SchoolYearHalf] that was on the month provided in [date].
         */
        @JvmStatic
        fun fromDate(date: LocalDate) = if (date.month !in Month.FEBRUARY..Month.AUGUST)
            SchoolYearHalf.FIRST
        else
            SchoolYearHalf.SECOND

        /**
         * @return Current [SchoolYearHalf].
         */
        @JvmStatic
        fun current() = fromDate(LocalDate.now())
    }
}
