package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.timetable.Timetable

/** Parses correct HTML to [Timetable] instance. */
internal interface HtmlTimetableParser
{
    fun parse(html: String): Timetable
}
