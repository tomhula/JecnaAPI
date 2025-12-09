package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.student.Student
import io.github.tomhula.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [Student] instance.
 */
internal interface HtmlStudentProfileParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): Student
}
