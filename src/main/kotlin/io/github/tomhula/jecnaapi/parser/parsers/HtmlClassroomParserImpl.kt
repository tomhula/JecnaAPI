package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.classroom.Room
import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal class HtmlClassroomParserImpl(private val timetableParser: HtmlTimetableParser) : HtmlClassroomParser
{
    override fun parse(html: String): Room
    {
        val doc = Jsoup.parse(html)

        val rawTitle = doc.selectFirst("h1")?.text() ?: ""
        val name = rawTitle.substringAfterLast(" - ")
            .replace(CLASSROOM_REGEX, "")
            .trim()
        val roomCode = name.substringAfter(" ")
        val table = doc.selectFirst("table.userprofile")
        val floor = getTableValue(table, "Podlaží")?.selectFirst("span.value")?.text()?.ifBlank { null }
        val homeroomOf = getTableValue(table, "Kmenová učebna")?.selectFirst("span.value")?.text()?.ifBlank { null }
        var manager: TeacherReference? = null

        val managerTd = getTableValue(table, "Správce")
        val managerCell = managerTd?.selectFirst("a span.label")?.text()?.trim()
        if (managerCell?.isNotEmpty() == true)
        {
            val tag = managerTd?.selectFirst("a")
                ?.attr("href")
                ?.substringAfterLast("/")
                ?.ifBlank { managerCell }
            manager = TeacherReference(managerCell, tag ?: managerCell)
        }
        // Timetable
        val timetableHtml = doc.select("table.timetable").outerHtml()
        val timetable =
            if (timetableHtml.isNotBlank()) timetableParser.parse(doc.select("div.timetable").outerHtml()) else null
        return Room(name, roomCode, floor, homeroomOf, manager, timetable)
    }
    companion object
    {
        private val CLASSROOM_REGEX = Regex("\\s*\\(.*?\\)")
    }
    
    private fun getTableValue(table: Element?, key: String): Element?
    {
        if (table == null) return null
        val rows = table.select("tr")
        val targetRow = rows.find { row -> row.selectFirst("th")?.text()?.trim() == key }

        return targetRow?.selectFirst("td")
    }
}
