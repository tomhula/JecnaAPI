package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.student.Locker
import io.github.tomhula.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [Locker] instance.
 */
internal interface HtmlLockerPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): Locker?
}
