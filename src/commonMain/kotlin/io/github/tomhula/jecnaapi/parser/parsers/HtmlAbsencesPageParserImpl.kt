package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.absence.AbsencesPage
import io.github.tomhula.jecnaapi.data.absence.AbsenceInfo
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.month
import io.github.tomhula.jecnaapi.util.toSchoolYear
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import kotlinx.datetime.LocalDate

/**
 * Parses correct HTML to [AbsencesPage] instance.
 */
internal object HtmlAbsencesPageParserImpl : HtmlAbsencesPageParser
{
    override fun parse(html: String): AbsencesPage
    {
        try
        {
            val document = Ksoup.parse(html)
            val builder = AbsencesPage.builder()

            val selectedSchoolYear = HtmlCommonParser.parseSelectedSchoolYear(document)
            builder.setSelectedSchoolYear(selectedSchoolYear)

            val rowEles = document.select(".absence-list > tbody > tr")

            for (rowEle in rowEles)
            {
                val dateTd = rowEle.selectFirstOrThrow(".date")
                val countTd = rowEle.selectFirstOrThrow(".count")

                val linkEle = dateTd.selectFirst("a")
                val dateText = (linkEle?.text() ?: dateTd.text()).trim()

                val date = parseDayDate(dateText, document)
                val countText = countTd.text().trim()

                val absenceInfo = parseAbsenceInfo(countText)

                if (absenceInfo.hoursAbsent == 0 && absenceInfo.lateEntryCount == 0)
                    continue

                builder.setAbsence(date, absenceInfo)
            }

            return builder.build()
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse absences page.", e)
        }
    }

    private fun parseDayDate(dayStr: String, document: Document): LocalDate
    {
        val match = DATE_REGEX.find(dayStr)
            ?: throw ParseException("Could not extract date from: \"$dayStr\"")

        val datePart = match.value
        val parts = datePart.split('.')
        val day = parts[0].toInt()
        val month = parts[1].toInt()

        val schoolYear = document.selectFirst("#schoolYearId > option[selected]")?.text()?.toSchoolYear() ?: SchoolYear.current()
        val year = schoolYear.getCalendarYear(month.month())

        return LocalDate(year, month, day)
    }

     private fun parseAbsenceInfo(text: String): AbsenceInfo
    {
        var hoursAbsent = 0
        var unexcusedHours = 0
        var lateEntryCount = 0

        // Check for late entry (pozdní příchod)
        val lateEntryRegex = Regex("""(?:^|\s)(\d+)\s+pozdní příchody?""")
        val lateEntryMatch = lateEntryRegex.find(text)
        if (lateEntryMatch != null)
        {
            // Capture number of late entries even when combined with hours absent
            lateEntryCount = lateEntryMatch.groupValues.getOrNull(1)?.toIntOrNull() ?: 0
        }
        val onlyLateEntryRegex = Regex("""^(\d+)\s+pozdní příchody?""")

        val onlyLateMatch = onlyLateEntryRegex.find(text)
        if (onlyLateMatch != null)
        {
            // It's only a late entry, so no hours absent
            hoursAbsent = 0
            // numLateEntries already set above if lateEntryMatch caught it; ensure fallback from onlyLateMatch.
            if (lateEntryCount == 0)
                lateEntryCount = onlyLateMatch.groupValues.getOrNull(1)?.toIntOrNull() ?: 0
            
            return AbsenceInfo(hoursAbsent, unexcusedHours, lateEntryCount)
        }
        // Parse hours absent (hodina/hodin) - czech grammar for the appropriate suffix.
        val hoursRegex = Regex("""^(\d+)\s+hod(?:in[ay]?)?""")
        val hoursMatch = hoursRegex.find(text)
        if (hoursMatch != null)
            hoursAbsent = hoursMatch.groupValues[1].toInt()

        // Check for unexcused hours (neomluvené)
        val unexcusedRegex = Regex("""z toho\s+(\d+)\s+neomluven(?:á|é|ých)?""", RegexOption.IGNORE_CASE)
        val unexcusedMatch = unexcusedRegex.find(text)
        
        if (unexcusedMatch != null)
            unexcusedHours = unexcusedMatch.groupValues[1].toInt()

        return AbsenceInfo(hoursAbsent, unexcusedHours, lateEntryCount)
    }

    // A RegexOption.DOT_MATCHES_ALL was used, but is not available on multiplatform
    private val DATE_REGEX = Regex("""[0-3]?\d\.[0-1]?\d\.""")
}
