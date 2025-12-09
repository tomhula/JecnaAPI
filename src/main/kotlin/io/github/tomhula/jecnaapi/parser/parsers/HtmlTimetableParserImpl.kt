package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.timetable.*
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.util.Name
import io.github.tomhula.jecnaapi.util.emptyMutableLinkedList
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.Normalizer
import java.time.DayOfWeek

object HtmlTimetableParserImpl : HtmlTimetableParser
{
    override fun parse(html: String): Timetable
    {
        try
        {
            val timetableBuilder = Timetable.builder()

            val document = Jsoup.parse(html)

            /* All the rows (tr) in the timetable table. */
            val rowEles = document.select("table.timetable > tbody > tr")

            /* The row (tr) containing all the LessonPeriods details.
			 * Each LessonPeriod is a 'th' with class 'period'. */
            val lessonPeriodsEle = rowEles[0]

            /* All the LessonPeriod elements. */
            val lessonPeriodEles = lessonPeriodsEle.select("th.period")

            for (lessonPeriodEle in lessonPeriodEles)
                timetableBuilder.addLessonPeriod(parseLessonPeriod(lessonPeriodEle))

            /* Removes the row with the LessonPeriods, so it leaves all the subjects. */
            rowEles.removeAt(0)

            for (rowEle in rowEles)
            {
                val day = rowEle.selectFirstOrThrow(".day").text()

                /* All the LessonSpots in this day. */
                val lessonSpotEles = rowEle.select("td")

                for (lessonSpotEle in lessonSpotEles)
                    timetableBuilder.addLessonSpot(parseDayOfWeek(day)!!, parseLessonSpot(lessonSpotEle))
            }

            return timetableBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse timetable.", e)
        }
    }

    /**
     * Transform two-letter day abbreviation into a [String].
     */
    private fun parseDayOfWeek(dayOfWeekStr: String) = when (dayOfWeekStr.trim().lowercase().removeAccent())
    {
        "po" -> DayOfWeek.MONDAY
        "ut" -> DayOfWeek.TUESDAY
        "st" -> DayOfWeek.WEDNESDAY
        "ct" -> DayOfWeek.THURSDAY
        "pa" -> DayOfWeek.FRIDAY
        "so" -> DayOfWeek.SATURDAY
        "ne" -> DayOfWeek.SUNDAY
        else -> null
    }

    /**
     * Removes an accent from a [String].
     * eg. Turns "Žluťoučký kůň" into "Zlutoucky kun".
     */
    private fun String.removeAccent() = Normalizer.normalize(this, Normalizer.Form.NFKD).replace(Regex("""\p{M}"""), "")

    /**
     * Parses [LessonPeriod] from it's HTML element.
     *
     * @return The parsed [LessonPeriod].
     */
    private fun parseLessonPeriod(lessonPeriodEle: Element) = LessonPeriod.fromString(lessonPeriodEle.selectFirstOrThrow(".time").text())

    /**
     * Parses [LessonSpot] from it's HTML element.
     *
     * @return The parsed [LessonSpot].
     */
    private fun parseLessonSpot(lessonSpotEle: Element): LessonSpot
    {
        val periodSpan = if (lessonSpotEle.hasAttr("colspan")) lessonSpotEle.attr("colspan").toInt() else 1

        if (lessonSpotEle.hasClass("empty"))
            return LessonSpot.empty(periodSpan)

        /* All the lessons in the lesson spot. */
        val lessonEles = lessonSpotEle.select("div:not(.lessonEmpty)")

        val lessons = emptyMutableLinkedList<Lesson>()

        for (lessonEle in lessonEles)
            lessons.add(parseLesson(lessonEle))

        return LessonSpot(lessons, periodSpan)
    }

    /**
     * Parses [Lesson] from it's HTML element.
     *
     * @return The parsed [Lesson].
     */
    private fun parseLesson(lessonEle: Element): Lesson
    {
        val subjectEle = lessonEle.selectFirstOrThrow(".subject")
        val classEle = lessonEle.selectFirst(".class")
        val groupEle = lessonEle.selectFirst(".group")
        val employeeEle = lessonEle.selectFirst(".employee")

        val subjectName = Name(subjectEle.attr("title"), subjectEle.text())
        val teacherName = employeeEle?.let { Name(it.attr("title"), it.text()) }
        val classroom = lessonEle.selectFirst(".room")?.text()
        val group = groupEle?.text()

        return Lesson(
            subjectName = subjectName,
            clazz = classEle?.text(),
            teacherName = teacherName,
            classroom = classroom,
            group = group
        )
    }
}
