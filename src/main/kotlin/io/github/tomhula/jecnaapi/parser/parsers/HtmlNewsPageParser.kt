package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.article.NewsPage
import io.github.tomhula.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [NewsPage] instance.
 */
internal interface HtmlNewsPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): NewsPage
}
