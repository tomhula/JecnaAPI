package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.attendance.AttendancesPage
import io.github.tomhula.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [AttendancesPage] instance.
 */
internal interface HtmlAttendancesPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): AttendancesPage
}
