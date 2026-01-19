package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.classroom.ClassroomPage
import io.github.tomhula.jecnaapi.data.classroom.ClassroomReference
import io.github.tomhula.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

internal object HtmlClassroomPageParserImpl : HtmlClassroomPageParser
{
    override fun parse(html: String): ClassroomPage
    {
        try
        {
            val doc: Document = Jsoup.parse(html)
            val classroomPageBuilder = ClassroomPage.builder()

            doc.select("ul.list > li > a.item").forEach { link ->
                val href = link.attr("href").trim()
                val symbol = href.substringAfter("/ucebna/").takeIf { it.isNotBlank() }

                val labelText = link.selectFirst("span.label")?.text()?.trim()
                if (labelText != null && labelText.isNotEmpty() && symbol != null)
                {
                    val nameOnly = labelText.replace(CLASSROOM_NAME_REGEX, "").trim()
                    classroomPageBuilder.addClassroomReference(ClassroomReference(name = nameOnly, roomCode = symbol))
                }
            }
            return classroomPageBuilder.build()
        } catch (e: ParseException)
        {
            throw ParseException("Failed to parse classroom page.", e)
        }
    }
    private val CLASSROOM_NAME_REGEX = Regex("\\s*\\(.*?\\)")
}
