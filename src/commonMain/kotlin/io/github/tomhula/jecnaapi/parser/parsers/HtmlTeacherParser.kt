package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.data.schoolStaff.Teacher

/**
 * Is responsible for parsing HTML source code in [String] to [Teacher] instance.
 */
internal interface HtmlTeacherParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): Teacher
}
