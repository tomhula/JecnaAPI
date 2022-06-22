package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.Attendance
import me.tomasan7.jecnaapi.data.AttendancesPage
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Parses correct HTML to [AttendancesPage] instance.
 */
class HtmlAttendancesParser : AttendancesParser
{
    override fun parse(source: String): AttendancesPage
    {
        try
        {
            val attendancesPageBuilder = AttendancesPage.builder()

            val document = Jsoup.parse(source)
            /* All the rows (tr) in the absence table. */
            val rowEles = document.select(".tab.absence-list > tbody > tr")

            for (rowEle in rowEles)
            {
                /* The first column in the row, which contains the day date. */
                val dayEle = rowEle.selectFirst(".date")
                val day = parseDayDate(dayEle!!.text(), document)
                /* The second column in the row, which contains all the paragraphs with the texts. */
                val attendancesEle = rowEle.select("td")[1]
                val attendancesParagraphs = attendancesEle.select("p")
                /* Contains all entries/leaves in the day as string. */
                val dayAttendancesAsStr = mutableListOf<String>()

                for (attendanceParagraph in attendancesParagraphs)
                {
                    val dayAttendances = attendanceParagraph.text().split(", ")
                    dayAttendancesAsStr.addAll(dayAttendances)
                }


                for (dayAttendanceStr in dayAttendancesAsStr)
                {
                    /* Don't save the days, which have no presence. */
                    if (dayAttendanceStr.isBlank())
                        continue

                    /* If the attendance matches regex, then it is an exit. */
                    val exit = LEAVE_REGEX.containsMatchIn(dayAttendanceStr)
                    /* Find the time and parse it to the LocalTime object. */
                    val time = LocalTime.parse(TIME_REGEX.find(dayAttendanceStr)!!.value, DateTimeFormatter.ofPattern("H:mm"))

                    attendancesPageBuilder.addAttendance(day, Attendance(exit, time))
                }
            }

            return attendancesPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }

    /**
     * Parses a date from the string and then uses the data from a different part of the page to determine the year.
     * @param dayEleText The text to parse the date from.
     * @param document The document, because the method needs to find the year on the page.
     * @return The parsed [LocalDate] object.
     */
    private fun parseDayDate(dayEleText: String, document: Document): LocalDate
    {
        val date = DATE_REGEX.find(dayEleText)!!.value
        /* First index with the day and the second with the month. */
        val dateSplit = date.split(".")
        val monthOfYear = dateSplit[1].toByte()

        /* First index with the first year and the second with the second. (eg. 2021/2022) */
        val schoolYear = document.selectFirst("#schoolYearId > option")!!.text().split("/")
        val firstYear = schoolYear[0].toShort()
        val secondYear = schoolYear[1].toShort()

        /* If the month is earlier than or same as August, use the second calendar year, the first otherwise.  */
        val resultYear = if (monthOfYear <= 8) secondYear else firstYear

        return LocalDate.parse(date, DateTimeFormatterBuilder()
            .appendPattern("d.M.")
            .parseDefaulting(ChronoField.YEAR, resultYear.toLong())
            .toFormatter())
    }

    companion object
    {
        /* Matches a date in 'dd.MM.' format. (for speed and simplicity, it also matches non-existing dates) */
        private val DATE_REGEX = Regex("""[0-3]?\d\.[0-1]?\d\.""", RegexOption.DOT_MATCHES_ALL)

        /* Matches whole string if it contains "Odchod" => if it's a leave. */
        private val LEAVE_REGEX = Regex(""".*Odchod.*""", RegexOption.DOT_MATCHES_ALL)

        /* Matches time in hh:mm format. */
        private val TIME_REGEX = Regex("""(?:[0-1]?\d|2[0-3]):[0-5]\d""", RegexOption.DOT_MATCHES_ALL)
    }
}