package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.classroom.Classroom

interface HtmlClassroomParser
{
    fun parse(html: String): Classroom
}
