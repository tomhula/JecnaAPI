package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.canteen.Menu
import io.github.tomhula.jecnaapi.data.classroom.ClassroomPage

/**
 * Is responsible for parsing source code in [String] to [ClassroomPage] instance.
 */
interface HtmlClassroomPageParser
{
    fun parse(html: String): ClassroomPage
}
