package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.student.Locker
import io.github.tomhula.jecnaapi.parser.ParseException
import com.fleeksoft.ksoup.Ksoup
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

internal object HtmlLockerPageParserImpl : HtmlLockerPageParser
{
    override fun parse(html: String): Locker?
    {
        try
        {
            val document = Ksoup.parse(html)

            val lockerEle = document.select("ul.list li .item .label").firstOrNull() ?: return null
            val lockerText = lockerEle.text()

            val numberMatch = LOCKER_NUMBER_REGEX.find(lockerText) ?: return null
            val number = numberMatch.groupValues[1]

            val descriptionMatch = LOCKER_DESCRIPTION_REGEX.find(lockerText) ?: return null
            val description = descriptionMatch.groupValues[1]

            val datesMatch = LOCKER_DATES_REGEX.find(lockerText)
            var assignedFrom: LocalDate? = null
            var assignedUntil: LocalDate? = null

            if (datesMatch != null) 
            {
                val fromDateStr = datesMatch.groupValues[1]
                assignedFrom = runCatching { LocalDate.parse(fromDateStr, HtmlCommonParser.CZECH_DATE_FORMAT_WITHOUT_PADDING) }.getOrNull()

                // Check if "do současnosti" (until now) or specific date
                val toDateStr = datesMatch.groupValues[2]
                if (!toDateStr.contains("současnosti"))
                    assignedUntil = runCatching { LocalDate.parse(toDateStr, HtmlCommonParser.CZECH_DATE_FORMAT_WITHOUT_PADDING) }.getOrNull()
            }

            return Locker(
                number = number,
                location = description,
                assignedFrom = assignedFrom,
                assignedUntil = assignedUntil
            )
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse locker page.", e)
        }
    }

    /**
     * Matches locker number from text like "skříňka č. 300"
     */
    private val LOCKER_NUMBER_REGEX = Regex("""skříňka\s+č\.\s+(\d+)""")

    /**
     * Matches description in parentheses
     */
    private val LOCKER_DESCRIPTION_REGEX = Regex("""\(([^)]+)\)""")

    /**
     * Matches date range like "od 1.9.2022 do současnosti" or "od 1.9.2022 do 31.8.2023"
     */
    private val LOCKER_DATES_REGEX = Regex("""od\s+([\d.]+)\s+do\s+(současnosti|[\d.]+)""")
}
