package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.schoolStaff.Teacher
import io.github.tomhula.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal class HtmlTeacherParserImpl(private val timetableParser: HtmlTimetableParser) : HtmlTeacherParser
{
    override fun parse(html: String): Teacher
    {
        try
        {
            val document = Jsoup.parse(html)
            val table = document.selectFirstOrThrow(".userprofile", "data table")

            val fullName = getTableValue(table, "Jméno")!!
            val tag = getTableValue(table, "Zkratka")!!
            val username = getTableValue(table, "Uživatelské jméno")!!
            val schoolMail = getTableValue(table, "E-mail")!!
            val privateMail = getTableValue(table, "Soukromý e-mail")
            val phoneColumn = getTableValue(table, "Telefon")
            val phoneNumbers = phoneColumn?.let { phoneCol -> PHONE_NUMBER_REGEX.findAll(phoneCol).map { it.value }.toList() } ?: emptyList()
            val landline = phoneColumn?.let { phoneCol -> LAND_LINE_REGEX.find(phoneCol)?.value }
            val privatePhone = getTableValue(table, "Soukromý telefon")
            val cabinet = getTableValue(table, "Kabinet")
            val consultationHours = getTableValue(table, "Konzultační hodiny")
            val tutorOfClass = getTableValue(table, "Třídní učitel")
            val profilePicturePath = document.selectFirst(".profilephoto .image img")?.attr("src")

            val timetableEle = document.selectFirst("table.timetable")
            val timetable = timetableEle?.let { timetableParser.parse(it.outerHtml()) }

            return Teacher(
                fullName = fullName,
                username = username,
                schoolMail = schoolMail,
                privateMail = privateMail,
                phoneNumbers = phoneNumbers,
                profilePicturePath = profilePicturePath,
                tag = tag,
                privatePhoneNumber = privatePhone,
                landline = landline,
                cabinet = cabinet,
                tutorOfClass = tutorOfClass,
                consultationHours = consultationHours,
                timetable = timetable
            )
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse teacher.", e)
        }
    }

    private fun getTableValue(table: Element, key: String): String?
    {
        val rows = table.select("tr")
        val targetRow = rows.find { row -> row.selectFirst(".label")?.let { it.text() == key } ?: false }

        return targetRow?.selectFirst(".value,.link")?.text()
    }

    companion object
    {
        /**
         * Matches either a number without spaces (123456789) or a number with spaces in the middle (123 456 789).
         */
        private val PHONE_NUMBER_REGEX = Regex("""(?:\+\d{3} )?((?:\d{3} ){2}\d{3})""")

        /**
         * Matches three numbers preceded with "a linka".
         */
        private val LAND_LINE_REGEX = Regex("""(?<=a linka )\d{3}""")
    }
}
