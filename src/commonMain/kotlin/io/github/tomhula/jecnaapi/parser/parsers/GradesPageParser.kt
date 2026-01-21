package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.grade.*
import io.github.tomhula.jecnaapi.data.notification.NotificationReference
import io.github.tomhula.jecnaapi.parser.HtmlElementNotFoundException
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.util.Name
import io.github.tomhula.jecnaapi.util.SchoolYearHalf
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.datetime.LocalDate

/** https://www.spsejecna.cz/score/student */
internal object GradesPageParser
{
    fun parse(html: String): GradesPage
    {
        try
        {
            val gradesPageBuilder = GradesPage.builder()

            val document = Ksoup.parse(html)

            /* All the rows (tr) in the grades table. */
            val rowEles = document.select(".score > tbody > tr")

            lateinit var behaviour: Behaviour

            for (rowEle in rowEles)
            {
                /* The first column (th; the header column) containing the subject name. */
                val subjectEle = rowEle.selectFirstOrThrow("th")
                /* The second column (td; the first body column) with the main content. (subject parts, grades, commendations) */
                val mainColumnEle = rowEle.selectFirstOrThrow("td")

                val subjectName = parseSubjectName(subjectEle.text())

                /* If this row's subject name is the behaviour one, parse this row as behaviour. */
                if (subjectName.full == Behaviour.SUBJECT_NAME)
                    behaviour = Behaviour(parseBehaviourNotifications(mainColumnEle),
                                          parseFinalGrade(rowEle.findFinalGradeEle().expectElement("behaviour final grade"), subjectName))
                else
                    gradesPageBuilder.addSubject(Subject(subjectName,
                                                         parseSubjectGrades(mainColumnEle),
                                                         rowEle.findFinalGradeEle()?.let { parseFinalGrade(it, subjectName) }))
            }

            gradesPageBuilder.setBehaviour(behaviour)

            gradesPageBuilder.setSelectedSchoolYear(CommonParser.parseSelectedSchoolYear(document))
            gradesPageBuilder.setSelectedSchoolYearHalf(parseSelectedSchoolYearHalf(document))

            return gradesPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse grades page.", e)
        }
    }

    private fun parseSubjectGrades(gradesColumnEle: Element): Grades
    {
        val subjectGradesBuilder = Grades.builder()

        /* All the elements in the main content column. (either grade or subject part) */
        val columnContentEles = gradesColumnEle.selectFirstOrThrow("td").children()

        /* The last encountered subject part, so we know where the following grades belong. */
        var lastSubjectPart: String? = null

        for (contentEle in columnContentEles)
        {
            if (contentEle.classNames().contains("subjectPart"))
                /* The substring removes the colon (':') after the subject part. */
                lastSubjectPart = contentEle.text().let { it.substring(0, it.length - 1) }
            else if (contentEle.`is`("a"))
                subjectGradesBuilder.addGrade(lastSubjectPart, parseGrade(contentEle))
        }

        return subjectGradesBuilder.build()
    }

    /**
     * Parses the [notifications][NotificationReference] from the main content column.
     *
     * @param behaviourColumnEle The main content column.
     * @return The list of parsed [notifications][NotificationReference].
     */
    private fun parseBehaviourNotifications(behaviourColumnEle: Element): List<NotificationReference>
    {
        /* All the notification elements (a) in the main content column. */
        val notificationEles = behaviourColumnEle.select("span > a")

        val notifications = mutableListOf<NotificationReference>()

        val notificationIdRegex = Regex("""userStudentRecordId=(\d+)""")

        for (notificationEle in notificationEles)
        {
            /* The element of the icon (tick or cross) */
            val iconEle = notificationEle.selectFirstOrThrow(".sprite-icon-16")

            /* Choosing type based on it's icon. (tick = good; cross = bad) */
            val type = if (iconEle.classNames().contains("sprite-icon-tick-16"))
                NotificationReference.NotificationType.GOOD
            else
                NotificationReference.NotificationType.BAD

            val message = notificationEle.selectFirstOrThrow(".label").text()

            val id = notificationIdRegex.find(notificationEle.attr("href"))?.groupValues?.get(1) ?: "0"

            notifications.add(NotificationReference(type, message, id.toInt()))
        }

        return notifications.toList()
    }

    /**
     * Parses [FinalGrade] from it's HTML element.
     *
     * @return The parsed [FinalGrade].
     */
    private fun parseFinalGrade(finalGradeEle: Element, subjectName: Name): FinalGrade
    {
        val textContent = finalGradeEle.text()

        if (textContent == "U") return FinalGrade.Excused

        return if (finalGradeEle.hasClass("scoreValueWarning"))
            when (textContent)
            {
                "5?" -> FinalGrade.GradesWarning
                "N?" -> FinalGrade.AbsenceWarning
                /* Element#text() returns normalized text. That's why we use space here and not newline, which originally is there. */
                "5? N?",
                "N? 5?" -> FinalGrade.GradesAndAbsenceWarning
                else -> throw ParseException("Unknown final grade warning: $textContent")
            }
        else
            FinalGrade.Grade(textContent.toInt(), subjectName)
    }

    /**
     * Finds the [FinalGrade]'s HTML element in the subject row element.
     *
     * @receiver The subject row element.
     * @return The [FinalGrade]'s HTML element.
     */
    private fun Element.findFinalGradeEle() = selectFirst(".scoreFinal")

    /**
     * Parses a [Grade] from it's HTML element.
     *
     * @return The parsed [Grade].
     */
    private fun parseGrade(gradeEle: Element): Grade
    {
        val valueChar = gradeEle.selectFirstOrThrow(".value").text()[0]
        val small = gradeEle.classNames().contains("scoreSmall")

        val teacherShort = gradeEle.selectFirstOrThrow(".employee").text()

        /* The title attribute of the grade element, which contains all the details. (description, date and teacher) */
        val titleAttr = gradeEle.attr("title")
        val hrefAttr = gradeEle.attr("href")
        val gradeId = GRADE_ID_REGEX.find(hrefAttr)?.value?.toIntOrNull()
            ?: throw ParseException("Failed to parse grade id from href: $hrefAttr")

        val detailsMatch = GRADE_DETAILS_REGEX.find(titleAttr) ?: return Grade(valueChar, small, gradeId = gradeId)

        /* Just description is optional, the rest is always there. */
        val description = detailsMatch.groups[GradeDetailsRegexGroups.DESCRIPTION]?.value
        val receiveDate = detailsMatch.groups[GradeDetailsRegexGroups.DATE]!!.value.let { LocalDate.parse(it, CommonParser.CZECH_DATE_FORMAT_WITH_PADDING) }
        val teacherFull = detailsMatch.groups[GradeDetailsRegexGroups.TEACHER]!!.value

        val teacherName = Name(teacherFull, teacherShort)

        return Grade(valueChar, small, teacherName, description, receiveDate, gradeId)
    }

    /**
     * Converts a [String] in "`name (shortname)`" format to a [Name] object.
     *
     * @return The [Name] instance.
     */
    private fun parseSubjectName(subjectNameStr: String): Name
    {
        val subjectNameMatch = SUBJECT_NAME_REGEX.find(subjectNameStr)!!

        val full = subjectNameMatch.groups[SubjectNameRegexGroups.FULL]!!.value
        val short = subjectNameMatch.groups[SubjectNameRegexGroups.SHORT]?.value

        return Name(full, short)
    }

    private fun parseSelectedSchoolYearHalf(document: Document): SchoolYearHalf
    {
        val selectedSchoolYearHalfEle = CommonParser.getSelectSelectedValue(document, "schoolYearHalfId")
            ?: throw HtmlElementNotFoundException.bySelector("#schoolYearHalfId")

        return getSchoolYearHalfByName(selectedSchoolYearHalfEle.text())
    }

    private fun getSchoolYearHalfByName(name: String): SchoolYearHalf
    {
        return when (name)
        {
            "1. pololetí" -> SchoolYearHalf.FIRST
            "2. pololetí" -> SchoolYearHalf.SECOND
            else          -> throw IllegalArgumentException("Unknown SchoolYearHalfName: $name")
        }
    }

    /**
     * Matches the [Grade]'s HTML element title. Match contains capturing groups listed in [GradeDetailsRegexGroups].
     */
    private val GRADE_DETAILS_REGEX = Regex($$"""(?:(?<$${GradeDetailsRegexGroups.DESCRIPTION}>.*) )?\((?<$${GradeDetailsRegexGroups.DATE}>\d{2}\.\d{2}\.\d{4}), (?<$${GradeDetailsRegexGroups.TEACHER}>.*)\)$""")

    /**
     * Contains names of regex capture groups inside [GRADE_DETAILS_REGEX].
     */
    private object GradeDetailsRegexGroups
    {
        const val DESCRIPTION = "description"
        const val DATE = "date"
        const val TEACHER = "teacher"
    }

    /**
     * Matches the whole name of a subject. Match contains capturing groups listed in [SubjectNameRegexGroups].
     */
    private val SUBJECT_NAME_REGEX = Regex($$"""(?<$${SubjectNameRegexGroups.FULL}>.*?)(?: \((?<$${SubjectNameRegexGroups.SHORT}>\w{1,4})\))?$""")

    /**
     * Matches the grade's id in it's href attribute.
     */
    private val GRADE_ID_REGEX = Regex("""(?<=scoreId=)\d+""")

    /**
     * Contains names of regex capture groups inside [GRADE_DETAILS_REGEX].
     */
    private object SubjectNameRegexGroups
    {
        const val FULL = "full"
        const val SHORT = "short"
    }
}
