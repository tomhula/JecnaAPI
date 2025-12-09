package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.student.Locker
import me.tomasan7.jecnaapi.parser.ParseException

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
