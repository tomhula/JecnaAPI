package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.room.RoomsPage

/**
 * Is responsible for parsing HTML source code in [String] to [RoomsPage] instance.
 */
interface HtmlRoomsPageParser
{
    fun parse(html: String): RoomsPage
}
