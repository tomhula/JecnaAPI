package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.timetable.TimetablePage
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.util.emptyMutableLinkedList
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter


internal class HtmlTimetablePageParserImpl(private val timetableParser: HtmlTimetableParser) : HtmlTimetablePageParser
{
    override fun parse(html: String): TimetablePage
    {
        try
        {
            val timetablePageBuilder = TimetablePage.builder()

            val document = Jsoup.parse(html)

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

    /**
     * Parses [PeriodOptions][TimetablePage.PeriodOption] from the form.
     *
     * @param document page [Document], from which the options will be taken.
     * @return List of [TimetablePage.PeriodOption] in the order as they appear in the form.
     */
    private fun parsePeriodOptions(document: Document): List<TimetablePage.PeriodOption>
    {
        val periodOptions = emptyMutableLinkedList<TimetablePage.PeriodOption>()

        /* The form select element. */
        val optionEles = document.select("#timetableId option")

        for (optionEle in optionEles)
            periodOptions.add(parsePeriodOption(optionEle))

        return periodOptions
    }

    /**
     * Parses [TimetablePage.PeriodOption] from it's HTML element.
     *
     * @return The parsed PeriodOption.
     */
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

        val from = LocalDate.parse(fromStr, PERIOD_OPTION_DATE_FORMATTER)
        val to = toStr?.let { LocalDate.parse(it, PERIOD_OPTION_DATE_FORMATTER) }

        return TimetablePage.PeriodOption(id, header, from, to, selected)
    }

    companion object
    {
        private val PERIOD_OPTION_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")

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
