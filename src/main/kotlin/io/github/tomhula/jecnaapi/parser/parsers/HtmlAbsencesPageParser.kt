package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.absence.AbsencesPage

/**
 * Is responsible for parsing HTML source code in [String] to [AbsencesPage] instance.
 */
internal interface HtmlAbsencesPageParser
{
    fun parse(html: String): AbsencesPage
}
