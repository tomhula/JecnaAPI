package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.timetable.TimetablePage
import io.github.tomhula.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [TimetablePage] instance.
 */
internal interface HtmlTimetablePageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): TimetablePage
}
