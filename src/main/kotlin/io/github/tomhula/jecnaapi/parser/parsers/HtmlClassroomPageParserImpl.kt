package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.classroom.ClassroomPage
import io.github.tomhula.jecnaapi.data.classroom.ClassroomReference
import io.github.tomhula.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlClassroomPageParserImpl : HtmlClassroomPageParser
{
    override fun parse(html: String): ClassroomPage
    {
        try
        {
            val doc: Document = Jsoup.parse(html)
            val classroomPageBuilder = ClassroomPage.builder()
            doc.select("ul.list > li > a > span.label").forEach { labelSpan ->
                val text = labelSpan.text().trim()
                if (text.isNotEmpty())
                {
                    classroomPageBuilder.addClassroomReference(ClassroomReference(text))
                }
            }
            return classroomPageBuilder.build()
        } catch (e: ParseException)
        {
            throw ParseException("Failed to parse classroom page.", e)
        }
    }
}

