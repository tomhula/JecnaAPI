package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.absence.AbsencesPage
import me.tomasan7.jecnaapi.data.absence.AbsenceDay
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.month
import me.tomasan7.jecnaapi.util.toSchoolYear
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Parses correct HTML to [AbsencesPage] instance.
 */
internal object HtmlAbsencesPageParserImpl : HtmlAbsencesPageParser {

    private val DATE_REGEX = Regex("[0-3]?\\d\\.[0-1]?\\d\\.", RegexOption.DOT_MATCHES_ALL)

    override fun parse(html: String): AbsencesPage {
        try {
            val document = Jsoup.parse(html)
            val builder = AbsencesPage.builder()

            val selectedSchoolYear = HtmlCommonParser.parseSelectedSchoolYear(document)
            builder.setSelectedSchoolYear(selectedSchoolYear)

            val rowEles = document.select(".absence-list > tbody > tr")

            for (rowEle in rowEles) {
                val dateTd = rowEle.selectFirstOrThrow(".date")
                val countTd = rowEle.selectFirstOrThrow(".count")

                val linkEle = dateTd.selectFirst("a")
                val dateText = (linkEle?.text() ?: dateTd.text()).trim()

                val date = parseDayDate(dateText, document)
                val hoursAbsent = parseHours(countTd.text()) ?: continue

                // text after the hours number - for notes
                val textAfter = countTd.text()
                    .let { full ->
                        val m = Regex("\\d+").find(full)
                        if (m != null && m.range.last + 1 < full.length)
                            full.substring(m.range.last + 1).trim()
                        else
                            ""
                    }

                val href = linkEle?.attr("href")?.takeIf { it.isNotBlank() }

                builder.addDay(date, hoursAbsent, textAfter)
            }

            return builder.build()
        } catch (e: Exception) {
            throw ParseException("Failed to parse absences page.", e)
        }
    }

    private fun parseDayDate(dayStr: String, document: Document): LocalDate {
        val match = DATE_REGEX.find(dayStr)
            ?: throw ParseException("Could not extract date from: \"$dayStr\"")

        val datePart = match.value
        val parts = datePart.split('.')
        val day = parts[0].toInt()
        val month = parts[1].toInt()

        val schoolYear = document.selectFirst("#schoolYearId > option[selected]")?.text()?.toSchoolYear() ?: SchoolYear.current()
        val year = schoolYear.getCalendarYear(month.month())

        return LocalDate.of(year, month, day)
    }

    private fun parseHours(text: String): Int? {
        val numberMatch = Regex("\\d+").find(text)
        return numberMatch?.value?.toIntOrNull()
    }
}
