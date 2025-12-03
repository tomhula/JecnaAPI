package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.absence.AbsencesPage

/**
 * Is responsible for parsing HTML source code in [String] to [AbsencesPage] instance.
 */
internal interface HtmlAbsencesPageParser
{
    fun parse(html: String): AbsencesPage
}
