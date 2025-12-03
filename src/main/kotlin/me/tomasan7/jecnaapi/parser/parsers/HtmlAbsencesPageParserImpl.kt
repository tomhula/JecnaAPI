package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.absence.AbsencesPage
import me.tomasan7.jecnaapi.data.absence.AbsenceInfo
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.month
import me.tomasan7.jecnaapi.util.toSchoolYear
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate

/**
 * Parses correct HTML to [AbsencesPage] instance.
 */
internal object HtmlAbsencesPageParserImpl : HtmlAbsencesPageParser {

    private val DATE_REGEX = Regex("[0-3]?\\d\\.[0-1]?\\d\\.", RegexOption.DOT_MATCHES_ALL)

    override fun parse(html: String): AbsencesPage {
        try {
            val document = Jsoup.parse(html)
            val builder = AbsencesPage.builder()

            val selectedSchoolYear = HtmlCommonParser.parseSelectedSchoolYear(document)
            builder.setSelectedSchoolYear(selectedSchoolYear)

            val rowEles = document.select(".absence-list > tbody > tr")

            for (rowEle in rowEles) {
                val dateTd = rowEle.selectFirstOrThrow(".date")
                val countTd = rowEle.selectFirstOrThrow(".count")

                val linkEle = dateTd.selectFirst("a")
                val dateText = (linkEle?.text() ?: dateTd.text()).trim()

                val date = parseDayDate(dateText, document)
                val countText = countTd.text().trim()

                // Parse the absence information
                val absenceInfo = parseAbsenceInfo(countText)

                // Skip if no hours absent and no late entry
                if (absenceInfo.hoursAbsent == 0 && !absenceInfo.isLateEntry) continue

                builder.addDay(
                    date,
                    absenceInfo.hoursAbsent,
                    absenceInfo.textAfter,
                    absenceInfo.isLateEntry,
                    absenceInfo.unexcusedHours,
                    absenceInfo.numLateEntries
                )
            }

            return builder.build()
        } catch (e: Exception) {
            throw ParseException("Failed to parse absences page.", e)
        }
    }

    private fun parseDayDate(dayStr: String, document: Document): LocalDate {
        val match = DATE_REGEX.find(dayStr)
            ?: throw ParseException("Could not extract date from: \"$dayStr\"")

        val datePart = match.value
        val parts = datePart.split('.')
        val day = parts[0].toInt()
        val month = parts[1].toInt()

        val schoolYear =
            document.selectFirst("#schoolYearId > option[selected]")?.text()?.toSchoolYear() ?: SchoolYear.current()
        val year = schoolYear.getCalendarYear(month.month())

        return LocalDate.of(year, month, day)
    }

    /**
     * Parses absence text to extract hours absent, late entry status, and unexcused hours.
     *
     **/
    private fun parseAbsenceInfo(text: String): AbsenceInfo {
        //declaring local variables
        var hoursAbsent = 0
        var isLateEntry = false
        var unexcusedHours = 0
        var textAfter: String?
        var numLateEntries = 0

        // Check for late entry (pozdní příchod)
        val lateEntryRegex = Regex("(?:^|\\s)(\\d+)\\s+pozdní příchod(y)?")
        val lateEntryMatch = lateEntryRegex.find(text)
        if (lateEntryMatch != null) {
            isLateEntry = true
            val onlyLateEntryRegex = Regex("^(\\d+)\\s+pozdní příchod(y)?")

            val onlyLateMatch = onlyLateEntryRegex.find(text)
            if (onlyLateMatch != null) {
                // It's only a late entry, so no hours absent
                hoursAbsent = 0
                numLateEntries = onlyLateMatch.groupValues[1].toIntOrNull() ?: 0

                // Extract text after the number ("1 pozdní příchod" -> "pozdní příchod")
                textAfter = text.let { full ->
                    val m = Regex("\\d+").find(full)
                    if (m != null && m.range.last + 1 < full.length)
                        full.substring(m.range.last + 1).trim()
                    else
                        full
                }
                return AbsenceInfo(hoursAbsent, isLateEntry, unexcusedHours, textAfter, numLateEntries)
            }
        }
            // Parse hours absent (hodina/hodin) - czech grammar for the appropriate suffix.
            val hoursRegex = Regex("^(\\d+)\\s+hod(?:in[ay]?)?")
            val hoursMatch = hoursRegex.find(text)
            if (hoursMatch != null) {
                hoursAbsent = hoursMatch.groupValues[1].toInt()
            }

            // Check for unexcused hours (neomluvené)
            val unexcusedRegex = Regex("z toho\\s+(\\d+)\\s+neomluven(?:á|é|ých)?", RegexOption.IGNORE_CASE)
            val unexcusedMatch = unexcusedRegex.find(text)
            if (unexcusedMatch != null) {
                unexcusedHours = unexcusedMatch.groupValues[1].toInt()
            }

            // Extract text after the first number for backwards compatibility.
            textAfter = text.let { full ->
                val m = Regex("\\d+").find(full)
                if (m != null && m.range.last + 1 < full.length)
                    full.substring(m.range.last + 1).trim()
                else
                    ""
            }.ifEmpty { null }

            return AbsenceInfo(hoursAbsent, isLateEntry, unexcusedHours, textAfter, numLateEntries)
        }
    }
