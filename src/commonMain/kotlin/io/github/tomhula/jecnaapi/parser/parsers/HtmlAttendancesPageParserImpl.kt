package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.attendance.Attendance
import io.github.tomhula.jecnaapi.data.attendance.AttendanceType
import io.github.tomhula.jecnaapi.data.attendance.AttendancesPage
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.emptyMutableLinkedList
import io.github.tomhula.jecnaapi.util.month
import io.github.tomhula.jecnaapi.util.toSchoolYear
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Parses correct HTML to [AttendancesPage] instance.
 */
internal object HtmlAttendancesPageParserImpl : HtmlAttendancesPageParser
{
    override fun parse(html: String): AttendancesPage
    {
        try
        {
            val attendancesPageBuilder = AttendancesPage.builder()

            val document = Ksoup.parse(html)

            /* All the rows (tr) in the absence table. */
            val rowEles = document.select(".absence-list > tbody > tr")

            for (rowEle in rowEles)
            {
                /* The first column in the row, which contains the day date. */
                val dayEle = rowEle.selectFirstOrThrow(".date")
                val day = parseDayDate(dayEle.text(), document)
                /* The second column in the row, which contains all the attendances. */
                val mainColumnEle = rowEle.select("td")[1]

                val attendanceList = parseAttendances(mainColumnEle)

                if (attendanceList.isNotEmpty())
                    attendancesPageBuilder.setAttendances(day, attendanceList)
            }

            attendancesPageBuilder.setSelectedSchoolYear(HtmlCommonParser.parseSelectedSchoolYear(document))
            attendancesPageBuilder.setSelectedMonth(HtmlCommonParser.parseSelectedMonth(document))

            return attendancesPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse attendances page.", e)
        }
    }

    /**
     * Parses all [attendances][Attendance] from the main column.
     *
     * @return [List] of [Attendance].
     */
    private fun parseAttendances(attendancesColumnEle: Element): List<Attendance>
    {
        val attendanceList = emptyMutableLinkedList<Attendance>()

        /* All the lines with the attendances. (as of now, two attendances per line) */
        val lines = attendancesColumnEle.select("p")

        /* Contains all entries/leaves in the day as string. */
        val dayAttendancesAsStr = emptyMutableLinkedList<String>()

        for (attendanceParagraph in lines)
            dayAttendancesAsStr.addAll(attendanceParagraph.text().split(", "))

        for (dayAttendanceStr in dayAttendancesAsStr)
        {
            /* If the attendance matches regex, then it is an exit. */
            val exit = LEAVE_REGEX.containsMatchIn(dayAttendanceStr)
            val timeStr = TIME_REGEX.find(dayAttendanceStr)!!.value
            val time = LocalTime.parse(if (timeStr.length == 4) "0$timeStr" else timeStr)

            attendanceList.add(Attendance(if (exit) AttendanceType.EXIT else AttendanceType.ENTER, time))
        }

        return attendanceList
    }

    /**
     * Parses a date from the string and then uses the data from a different part of the page to determine the year.
     * Uses current year if the page does not contain it.
     * @param dayStr The text to parse the date from.
     * @param document The document, because the method needs to find the year on the page.
     * @return The parsed [LocalDate] object.
     */
    private fun parseDayDate(dayStr: String, document: Document): LocalDate
    {
        val date = DATE_REGEX.find(dayStr)!!.value
        val dateParts = date.split('.')
        /* First index with the day and the second with the month. */
        val dayOfMonth = dateParts[0].toInt()
        val monthOfYear = dateParts[1].toInt()

        /* Finds the current calendar year from the year dropdown selection. */
        val schoolYear = document.selectFirst("#schoolYearId > option[selected]")?.text()?.toSchoolYear() ?: SchoolYear.current()
        val resultYear = schoolYear.getCalendarYear(monthOfYear.month())

        return LocalDate(resultYear, monthOfYear, dayOfMonth)
    }

    /**
     * Matches the day date in the first column.
     *
     * Matches a date in 'dd.MM.' format. (for speed and simplicity, it also matches non-existing dates)
     */
    // A RegexOption.DOT_MATCHES_ALL was used, but is not available on multiplatform
    private val DATE_REGEX = Regex("""[0-3]?\d\.[0-1]?\d\.""")

    /**
     *  Matches "Odchod" **only** => if it's a leave.
     */
    private val LEAVE_REGEX = Regex("""Odchod""")

    /**
     * Matches the time in each attendance.
     *
     * Matches time in hh:mm format.
     */
    // A RegexOption.DOT_MATCHES_ALL was used, but is not available on multiplatform
    private val TIME_REGEX = Regex("""(?:[0-1]?\d|2[0-3]):[0-5]\d""")
}
