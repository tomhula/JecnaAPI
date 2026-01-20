package io.github.tomhula.jecnaapi.parser.parsers

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.github.tomhula.jecnaapi.data.room.Room
import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference

internal class HtmlRoomParserImpl(private val timetableParser: HtmlTimetableParser) : HtmlRoomParser
{
    override fun parse(html: String): Room
    {
        val doc = Ksoup.parse(html)

        val rawTitle = doc.selectFirst("h1")?.text() ?: ""
        val name = rawTitle.replace(PARENTHESIS_REGEX, "").trim()
        val urlMetaProperty = doc.selectFirst("meta[property='og:url']")?.attr("content") ?: ""
        val roomCode = urlMetaProperty.substringAfterLast("/").substringBefore("?")
        val infoTable = doc.selectFirst("table.userprofile")
        val floor = getTableValueString(infoTable, "Podlaží")
        val homeroomOf = getTableValueString(infoTable, "Kmenová učebna")?.replace(PARENTHESIS_REGEX, "")?.trim()

        val managerAnchor = getTableValue(infoTable, "Správce")?.selectFirst("a")
        val managerReference = managerAnchor?.let { managerAnchor -> 
            val name = managerAnchor.selectFirst("span.label")?.text()?.trim() ?: return@let null
            val tag = managerAnchor.attr("href").substringAfterLast("/").ifBlank { return@let null }

            TeacherReference(name, tag)
        }
        
        val timetableHtml = doc.selectFirst("div.timetable")?.outerHtml()
        val timetable = timetableHtml?.let { runCatching { timetableParser.parse(timetableHtml) }.getOrNull() }

        return Room(name, roomCode, floor, homeroomOf, managerReference, timetable)
    }
    
    private fun getTableValue(table: Element?, key: String): Element?
    {
        if (table == null) return null
        val rows = table.select("tr")
        val targetRow = rows.find { row -> row.selectFirst(".label")?.text()?.trim() == key }

        return targetRow?.selectFirst("td")
    }

    private fun getTableValueString(table: Element?, key: String): String?
    {
        return getTableValue(table, key)?.selectFirst(".value")?.text()
    }

    companion object
    {
        /** Matches anything inside parenthesis '(...)' */
        private val PARENTHESIS_REGEX = Regex("""\(.*\)""")
    }
}
