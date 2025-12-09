package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.student.Guardian
import me.tomasan7.jecnaapi.data.student.Student
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object HtmlStudentProfileParserImpl : HtmlStudentProfileParser
{
    override fun parse(html: String): Student
    {
        try
        {
            val document = Jsoup.parse(html)
            val table = document.selectFirstOrThrow(".userprofile", "data table")

            val fullName = getTableValue(table, "Celé jméno")!!
            val username = getTableValue(table, "Uživatelské jméno")!!
            val schoolMail = getTableValue(table, "Školní e-mail")?.let { extractEmail(it) }!!
            val privateMail = getTableValue(table, "Soukromý e-mail")?.let { extractEmail(it) }
            val phoneColumn = getTableValue(table, "Telefon")
            val phoneNumbers = phoneColumn?.let { phoneCol -> PHONE_NUMBER_REGEX.findAll(phoneCol).map { it.value }.toList() } ?: emptyList()

            val ageStr = getTableValue(table, "Věk")
            val age = ageStr?.let { AGE_REGEX.find(it)?.groupValues?.get(1)?.toIntOrNull() }

            val birthStr = getTableValue(table, "Narození")
            val birthDate = birthStr?.let { parseBirthDate(it) }
            val birthPlace = birthStr?.let { parseBirthPlace(it) }

            val permanentAddress = getTableValue(table, "Trvalá adresa")

            val classInfo = getTableValue(table, "Třída, skupiny")
            val className = classInfo?.let { parseClassName(it) }
            val classGroups = classInfo?.let { parseClassGroups(it) }

            val classNumber = getTableValue(table, "Číslo v tříd. výkazu")?.toIntOrNull()

            val profilePicturePath = document.selectFirst(".profilephoto .image img")?.attr("src")

            // Parse guardians
            val guardiansList = document.select("h2:contains(Rodiče a zákonní zástupci) ~ ul.list li")
            val guardians = guardiansList.mapNotNull { parseGuardian(it) }

            // Parse SPOSA information (of course)
            val sposaTable = document.select("h2:contains(Spolek pro podporu studentských aktivit) ~ table.userprofile").firstOrNull()
            val sposaVariableSymbol = sposaTable?.let { getTableValue(it, "Variabilní symbol žáka") }
            val sposaBankAccount = sposaTable?.let { getTableValue(it, "Bankovní účet") }

            return Student(
                fullName = fullName,
                username = username,
                schoolMail = schoolMail,
                privateMail = privateMail,
                phoneNumbers = phoneNumbers,
                profilePicturePath = profilePicturePath,
                age = age,
                birthDate = birthDate,
                birthPlace = birthPlace,
                permanentAddress = permanentAddress,
                className = className,
                classGroups = classGroups,
                classNumber = classNumber,
                guardians = guardians,
                sposaVariableSymbol = sposaVariableSymbol,
                sposaBankAccount = sposaBankAccount
            )
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse student profile.", e)
        }
    }

    private fun getTableValue(table: Element, key: String): String?
    {
        val rows = table.select("tr")
        val targetRow = rows.find { row -> row.selectFirst(".label")?.let { it.text() == key } ?: false }

        return targetRow?.selectFirst(".value,.link")?.text()
    }

    private fun extractEmail(text: String): String?
    {
        // Extract email from text that may contain additional HTML/text
        val emailMatch = EMAIL_REGEX.find(text)
        return emailMatch?.value
    }

    private fun parseBirthDate(birthStr: String): LocalDate?
    {
        // Format: "01.01.2000, <city>"
        val datePart = birthStr.split(",").firstOrNull()?.trim()
        return datePart?.let {
            try {
                LocalDate.parse(it, DATE_FORMATTER)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parseBirthPlace(birthStr: String): String?
    {
        // Format: "01.01.2000, <city>"
        val parts = birthStr.split(",")
        return if (parts.size > 1) parts[1].trim() else null
    }

    private fun parseClassName(classInfo: String): String?
    {
        // Format: "<class>, skupiny: A<1,2>, O"
        return classInfo.split(",").firstOrNull()?.trim()
    }

    private fun parseClassGroups(classInfo: String): String?
    {
        // Format: "<class>, skupiny: A<1,2>, O"
        val parts = classInfo.split("skupiny:")
        return if (parts.size > 1) parts[1].trim() else null
    }

    private fun parseGuardian(guardianEle: Element): Guardian?
    {
        // Format: "<name of guardian>, <phone of guardian> <email of guardian>"
        val text = guardianEle.text()
        val parts = text.split(",").map { it.trim() }

        if (parts.isEmpty()) return null

        val name = parts[0]
        val phoneNumber = parts.getOrNull(1)?.takeIf { PHONE_NUMBER_REGEX.matches(it) }
        val email = parts.getOrNull(2)?.takeIf { EMAIL_REGEX.matches(it) }
            ?: parts.getOrNull(1)?.takeIf { EMAIL_REGEX.matches(it) }

        return Guardian(name, phoneNumber, email)
    }

    /**
     * Matches phone numbers with or without spaces (+xxx 123 456 789), (+xxx123456789), prefix 1-3 numbers (if guardian is foreign)
     */
    private val PHONE_NUMBER_REGEX = Regex("""(?:\+\d{1,3}\s?)?\d{3}\s?\d{3}\s?\d{3}""")

    /**
     * Matches email addresses
     */
    private val EMAIL_REGEX = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")

    /**
     * Matches age from text like "18 let"
     */
    private val AGE_REGEX = Regex("""(\d+)\s+let?""")

    /**
     * Date formatter for Czech date format (dd.MM.yyyy)
     */
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
}
