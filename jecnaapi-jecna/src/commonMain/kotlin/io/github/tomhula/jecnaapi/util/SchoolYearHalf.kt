package io.github.tomhula.jecnaapi.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.jvm.JvmStatic
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
        @OptIn(ExperimentalTime::class)
        @JvmStatic
        fun current() = fromDate(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
}
