package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.student.Locker
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object HtmlLockerPageParserImpl : HtmlLockerPageParser
{
    override fun parse(html: String): Locker?
    {
        try
        {
            val document = Jsoup.parse(html)

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
                assignedFrom = runCatching { LocalDate.parse(fromDateStr, DATE_FORMATTER) }.getOrNull()

                // Check if "do současnosti" (until now) or specific date
                val toDateStr = datesMatch.groupValues[2]
                if (!toDateStr.contains("současnosti"))
                    assignedUntil = runCatching { LocalDate.parse(toDateStr, DATE_FORMATTER) }.getOrNull()
            }

            return Locker(
                number = number,
                description = description,
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

    /**
     * Date formatter for Czech date format (d.M.yyyy)
     */
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy")
}
