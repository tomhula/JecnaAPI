package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.timetable.TimetablePage
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.util.emptyMutableLinkedList
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.datetime.LocalDate

internal class HtmlTimetablePageParserImpl(private val timetableParser: HtmlTimetableParser) : HtmlTimetablePageParser
{
    override fun parse(html: String): TimetablePage
    {
        try
        {
            val timetablePageBuilder = TimetablePage.builder()

            val document = Ksoup.parse(html)

            val timetableEle = document.selectFirstOrThrow("table.timetable")
            val timetable = timetableParser.parse(timetableEle.outerHtml())

            timetablePageBuilder.setPeriodOptions(parsePeriodOptions(document))
            timetablePageBuilder.setSelectedSchoolYear(HtmlCommonParser.parseSelectedSchoolYear(document))
            timetablePageBuilder.setTimetable(timetable)

            return timetablePageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse timetable page.", e)
        }
    }

    private fun parsePeriodOptions(document: Document): List<TimetablePage.PeriodOption>
    {
        val periodOptions = emptyMutableLinkedList<TimetablePage.PeriodOption>()

        /* The form select element. */
        val optionEles = document.select("#timetableId option")

        for (optionEle in optionEles)
            periodOptions.add(parsePeriodOption(optionEle))

        return periodOptions
    }

    private fun parsePeriodOption(periodOptionEle: Element): TimetablePage.PeriodOption
    {
        val id = periodOptionEle.attr("value").toInt()
        val text = periodOptionEle.text()
        val selected = periodOptionEle.hasAttr("selected")

        val header = PERIOD_OPTION_HEADER_REGEX.find(text)?.value

        /* Sublist because when splitting with "Od " it returns empty string (before "Od ") at index 0. */
        val datesSplit = text.split(PERIOD_OPTION_DATES_SPLIT_REGEX).let { it.subList(1, it.size) }
        val fromStr = datesSplit[0]
        val toStr = datesSplit.getOrNull(1)

        val from = LocalDate.parse(fromStr, HtmlCommonParser.CZECH_DATE_FORMAT_WITHOUT_PADDING)
        val to = toStr?.let { LocalDate.parse(it, HtmlCommonParser.CZECH_DATE_FORMAT_WITHOUT_PADDING) }

        return TimetablePage.PeriodOption(id, header, from, to, selected)
    }

    companion object
    {
        /**
         * Matches " Od" or " do " in the period option text in the dropdown selection.
         */
        private val PERIOD_OPTION_DATES_SPLIT_REGEX = Regex("""[Oo]d | do """)

        /**
         * Matches the text before the dates in the [TimetablePage.PeriodOption] text.
         * Eg. "Mimořádný rozvrh" or "Dočasný rozvrh".
         */
        private val PERIOD_OPTION_HEADER_REGEX = Regex("""^.*?(?= -)""")
    }
}
