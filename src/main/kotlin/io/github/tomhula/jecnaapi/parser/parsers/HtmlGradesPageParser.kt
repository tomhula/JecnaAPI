package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.grade.GradesPage
import io.github.tomhula.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [GradesPage] instance.
 */
internal interface HtmlGradesPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): GradesPage
}
