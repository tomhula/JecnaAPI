package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.classroom.Classroom
import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import org.jsoup.Jsoup

internal class HtmlClassroomParserImpl(private val timetableParser: HtmlTimetableParser) : HtmlClassroomParser
{
    override fun parse(html: String): Classroom
    {
        val doc = Jsoup.parse(html)

        val rawTitle = doc.selectFirst("title")?.text() ?: ""
        val title = rawTitle.substringAfterLast(" - ")
            .replace(Regex("\\s*\\(.*?\\)"), "")
            .trim()
        val floor = doc.select("table.userprofile th:contains(Podlaží) + td span.value").text().ifBlank { null }
        val mainClassroomOf =
            doc.select("table.userprofile th:contains(Kmenová učebna) + td span.value").text().ifBlank { null }
        var manager: TeacherReference? = null

        val managerCell = doc.select("table.userprofile th:contains(Správce) + td a span.label").text().trim()
        if (managerCell.isNotEmpty())
        {
            val tag = doc.select("table.userprofile th:contains(Správce) + td a")
                .attr("href")
                .substringAfterLast("/")
                .ifBlank { managerCell }
            manager = TeacherReference(managerCell, tag)
        }
        // Timetable
        val timetableHtml = doc.select("table.timetable").outerHtml()
        val timetable =
            if (timetableHtml.isNotBlank()) timetableParser.parse(doc.select("div.timetable").outerHtml()) else null
        return Classroom(title, floor, mainClassroomOf, manager, timetable)
    }
}
