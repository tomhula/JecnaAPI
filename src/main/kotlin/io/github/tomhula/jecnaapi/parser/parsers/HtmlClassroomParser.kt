package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.classroom.Room

interface HtmlClassroomParser
{
    fun parse(html: String): Room
}
