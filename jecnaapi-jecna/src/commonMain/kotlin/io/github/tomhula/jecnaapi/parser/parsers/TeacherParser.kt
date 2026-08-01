package io.github.tomhula.jecnaapi.parser.parsers

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.github.tomhula.jecnaapi.data.cert.Certificate
import io.github.tomhula.jecnaapi.data.room.RoomReference
import io.github.tomhula.jecnaapi.data.schoolStaff.Teacher
import io.github.tomhula.jecnaapi.parser.ParseException
import kotlinx.datetime.LocalDate

/** https://www.spsejecna.cz/ucitel/{teacher-tag} */
internal class TeacherParser(private val timetableParser: TimetableParser)
{
    fun parse(html: String): Teacher
    {
        try
        {
            val document = Ksoup.parse(html)
            val table = document.selectFirstOrThrow(".userprofile", "data table")
            val fullName = getTableValueText(table, "Jméno")!!
            val tag = getTableValueText(table, "Zkratka")!!
            val username = getTableValueText(table, "Uživatelské jméno")!!
            val schoolMail = getTableValueText(table, "E-mail")!!
            val privateMail = getTableValueText(table, "Soukromý e-mail")
            val phoneColumn = getTableValueText(table, "Telefon")
            val phoneNumbers = phoneColumn?.let { phoneCol -> PHONE_NUMBER_REGEX.findAll(phoneCol).map { it.value }.toList() } ?: emptyList()
            val landline = phoneColumn?.let { phoneCol -> LAND_LINE_REGEX.find(phoneCol)?.value }
            val privatePhone = getTableValueText(table, "Soukromý telefon")
            
            val cabinetName = getTableValueText(table, "Kabinet")
            val cabinetHref = getTableValueHref(table, "Kabinet")
                ?.substringAfter("/ucebna/")
                ?.takeIf { it.isNotBlank() }
            val cabinet = if (cabinetName != null && cabinetHref != null) {
                RoomReference(cabinetName, cabinetHref)
            } else {
                null
            }
            
            val consultationHours = getTableValueText(table, "Konzultační hodiny")
            val tutorOfClass = getTableValueText(table, "Třídní učitel")
            val profilePicturePath = document.selectFirst(".profilephoto .image img")?.attr("src")
            val timetableEle = document.selectFirst("table.timetable")
            val timetable = timetableEle?.let { timetableParser.parse(it.outerHtml()) }

            val certificates = mutableListOf<Certificate>()
            val certList = document.select("ul.certifications > li")
            for (li in certList)
            {
                val date = LocalDate.parse(
                    li.selectFirst("span.date")?.text()?.trim().orEmpty(),
                    CommonParser.CZECH_DATE_FORMAT_WITH_PADDING
                ) 
                val infoSpan = li.selectFirst("span.info")
                val label = infoSpan?.selectFirst("span.label")?.text()?.trim().orEmpty()
                val institution = infoSpan?.selectFirst("span.institution")?.text()?.trim().orEmpty()
                if (institution.isNotEmpty())
                {
                    certificates.add(Certificate(date, institution, label))
                }
            }
            
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
                timetable = timetable,
                certificates = certificates
            )
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse teacher.", e)
        }
    }
    
    private fun getTableValue(table: Element, key: String): Element?
    {
        val rows = table.select("tr")
        val targetRow = rows.find { row -> row.selectFirst(".label")?.let { it.text() == key } ?: false }

        return targetRow?.selectFirst(".value,.link")
    }

    private fun getTableValueText(table: Element, key: String): String?
    {
        return getTableValue(table, key)?.text()
    }

    private fun getTableValueHref(table: Element, key: String): String?
    {
        return getTableValue(table, key)?.attr("href")
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
