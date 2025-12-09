package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.data.schoolStaff.TeachersPage

/**
 * Is responsible for parsing HTML source code in [String] to [TeachersPage] instance.
 */
internal interface HtmlTeachersPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): TeachersPage
}
