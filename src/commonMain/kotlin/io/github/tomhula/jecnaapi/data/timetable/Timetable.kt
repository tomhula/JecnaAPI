package io.github.tomhula.jecnaapi.data.timetable

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.serialization.TimetableSerializer
import io.github.tomhula.jecnaapi.util.emptyMutableLinkedList
import io.github.tomhula.jecnaapi.util.next
import io.github.tomhula.jecnaapi.util.setAll
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.collections.getOrPut
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmOverloads
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable(with = TimetableSerializer::class)
class Timetable private constructor(
    lessonPeriods: List<LessonPeriod>,
    private val timetable: Map<DayOfWeek, List<LessonSpot>>
)
{
    val lessonPeriods = lessonPeriods.sortedBy { it.from }

    /** All days in the [Timetable]. There may be no [LessonSpots][LessonSpot] for some days. */
    val days = this.timetable.keys

    /**
     * All days in the [Timetable] sorted as they would be in a week.
     * There may be no [LessonSpots][LessonSpot] for some days.
     */
    val daysSorted = days.sorted()

    private val lessonPeriodsTrimmed = buildMap {
        for (day in days)
        {
            var lessonPeriodWithLastLessonIndex: Int = lessonPeriods.lastIndex

            for (i in lessonPeriods.indices)
            {
                val lessonSpot = getLessonSpot(day, i) ?: continue
                if (lessonSpot.isNotEmpty())
                    lessonPeriodWithLastLessonIndex = i
            }

            put(day, lessonPeriods.filterIndexed { i, _ -> i <= lessonPeriodWithLastLessonIndex })
        }
    }

    /**
     * Returns Whether the [Timetable] contains any days.
     * There may be no [LessonSpots][LessonSpot] for some days, that would return `false`.
     */
    fun isEmpty() = timetable.isEmpty()

    /** Returns index of the [LessonPeriod] at the given [time], or `null` if there is not any. */
    fun getIndexOfLessonPeriod(time: LocalTime) =
        lessonPeriods.indexOfFirst { time in it }.let { if (it != -1) it else null }

    /** Returns the [LessonPeriod] at the given [time], or `null` if there is not any. */
    fun getLessonPeriod(time: LocalTime) = getIndexOfLessonPeriod(time)?.let { lessonPeriods[it] }

    private fun LocalTime.toMinutes() = hour * 60 + minute
    private fun LocalTime.toSeconds() = toMinutes() * 60 + second

    /** Returns the index of next [LessonPeriod] from the given [time], or `null` if there is no next [LessonPeriod]. */
    fun getIndexOfNextLessonPeriod(time: LocalTime): Int?
    {
        fun minsUntilStartOf(lessonPeriodIndex: Int) =
            lessonPeriods[lessonPeriodIndex].from.toMinutes() - time.toMinutes()

        return lessonPeriods.indices.filter { minsUntilStartOf(it) > 0 }.minByOrNull { minsUntilStartOf(it) }
    }

    /** Returns the next [LessonPeriod] from the given [time], or `null` if there is no next [LessonPeriod]. */
    fun getNextLessonPeriod(time: LocalTime) = getIndexOfNextLessonPeriod(time)?.let { lessonPeriods[it] }

    /** Returns a list of all [LessonSpots][LessonSpot] in the given [day] ordered by their start time, or `null` if [day] is not in this [Timetable]. */
    fun getLessonSpotsForDay(day: DayOfWeek) = timetable[day]

    /** Returns a list of all [LessonSpots][LessonSpot] in the given [day] ordered by their start time, or `null` if [day] is not in this [Timetable]. */
    operator fun get(day: DayOfWeek) = getLessonSpotsForDay(day)

    /** Returns the [LessonSpot] that is happening at the given [lessonPeriodIndex]. */
    fun getLessonSpot(day: DayOfWeek, lessonPeriodIndex: Int): LessonSpot?
    {
        if (isEmpty()) return null

        val lessonSpots = getLessonSpotsForDay(day) ?: return null

        var x = 0

        for (lessonSpot in lessonSpots)
        {
            if (lessonPeriodIndex in x until x + lessonSpot.periodSpan)
                return lessonSpot

            x += lessonSpot.periodSpan
        }

        return null
    }

    /**
     * Returns the [LessonSpot] that is happening at the given [lessonPeriod].
     * Prefer using [getLessonSpot] if you know the [lessonPeriod] index.
     * @see getLessonSpot
     */
    fun getLessonSpot(day: DayOfWeek, lessonPeriod: LessonPeriod) =
        lessonPeriods.indexOf(lessonPeriod).let { if (it != -1) getLessonSpot(day, it) else null }

    /**
     * Returns the [LessonSpot] at the given [day] and [time], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     * @param trim Whether the empty [LessonSpots][LessonSpot] at the end of the day should not be returned.
     */
    @JvmOverloads
    fun getLessonSpot(day: DayOfWeek, time: LocalTime, takeEmpty: Boolean = false, trim: Boolean = true): LessonSpot?
    {
        val lessonPeriod = getLessonPeriod(time) ?: return null
        val lessonPeriodsTrimmed = lessonPeriodsTrimmed[day] ?: return null

        if (trim && lessonPeriod !in lessonPeriodsTrimmed)
            return null

        return getLessonSpot(day, lessonPeriod)?.takeIf { takeEmpty || it.isNotEmpty() }
    }

    /**
     * Returns the [LessonSpot] at the given [datetime], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     * @param trim Whether the empty [LessonSpots][LessonSpot] at the end of the day should not be returned.
     */
    @JvmOverloads
    fun getLessonSpot(datetime: LocalDateTime, takeEmpty: Boolean = false, trim: Boolean = true) =
        getLessonSpot(datetime.dayOfWeek, datetime.time, takeEmpty, trim)

    /**
     * Returns the [LessonSpot] at the given [instant], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     * @param trim Whether the empty [LessonSpots][LessonSpot] at the end of the day should not be returned.
     */
    @OptIn(ExperimentalTime::class)
    @JvmOverloads
    fun getLessonSpot(instant: Instant, takeEmpty: Boolean = false, trim: Boolean = true) =
        getLessonSpot(instant.toLocalDateTime(TimeZone.currentSystemDefault()), takeEmpty, trim)

    /**
     * Returns the next [LessonSpot] from the given [time] on the [day], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     * @param trim Whether the empty [LessonSpots][LessonSpot] at the end of the day should not be returned.
     */
    @JvmOverloads
    fun getNextLessonSpot(
        day: DayOfWeek,
        time: LocalTime,
        takeEmpty: Boolean = false,
        trim: Boolean = true
    ): LessonSpot?
    {
        if (timetable.isEmpty()) return null

        if (day !in days)
            return getNextLessonSpot(day.next(), LocalTime(0, 0), takeEmpty)

        val lessonPeriods = if (trim)
            lessonPeriodsTrimmed[day]!!
        else
            this.lessonPeriods

        val nextLessonPeriodIndex = lessonPeriods
            .map { it to it.from }
            .filter { it.second > time }
            .minByOrNull { it.second.toSeconds() - time.toSeconds() }
            ?.first
            ?.let { lp -> lessonPeriods.indexOfFirst { lp === it } }
            ?: -1

        val nextLessonSpot = getLessonSpot(day, nextLessonPeriodIndex)?.takeIf { takeEmpty || it.isNotEmpty() }

        return nextLessonSpot ?: getNextLessonSpot(day.next(), LocalTime(0, 0), takeEmpty)
    }

    /**
     * Returns the next [LessonSpot] from the given [datetime], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     * @param trim Whether the empty [LessonSpots][LessonSpot] at the end of the day should not be returned.
     */
    @JvmOverloads
    fun getNextLessonSpot(datetime: LocalDateTime, takeEmpty: Boolean = false, trim: Boolean = true) =
        getNextLessonSpot(datetime.dayOfWeek, datetime.time, takeEmpty, trim)

    /**
     * Returns the next [LessonSpot] from the given [instant], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     * @param trim Whether the empty [LessonSpots][LessonSpot] at the end of the day should not be returned.
     */
    @OptIn(ExperimentalTime::class)
    @JvmOverloads
    fun getNextLessonSpot(instant: Instant, takeEmpty: Boolean = false, trim: Boolean = true) =
        getNextLessonSpot(instant.toLocalDateTime(TimeZone.currentSystemDefault()), takeEmpty, trim)

    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder
    {
        private val lessonPeriods: MutableList<LessonPeriod> = emptyMutableLinkedList()
        private val timetable: MutableMap<DayOfWeek, MutableList<LessonSpot>> = mutableMapOf()

        /**
         * Sets all the [LessonPeriods][LessonPeriod].
         *
         * @param lessonPeriods The [LessonPeriods][LessonPeriod] to use.
         * @return This [builder's][Builder] instance back.
         */
        fun setLessonPeriods(lessonPeriods: Iterable<LessonPeriod>): Builder
        {
            this.lessonPeriods.setAll(lessonPeriods)
            return this
        }

        /**
         * Sets a [LessonPeriod] to a specified hour index.
         *
         * @param hour       The hour index to set the [LessonPeriod] to.
         * @param lessonPeriod The [LessonPeriod] to set.
         * @return This [builder's][Builder] instance back.
         */
        fun setLessonPeriod(hour: Int, lessonPeriod: LessonPeriod): Builder
        {
            lessonPeriods[hour] = lessonPeriod
            return this
        }

        /**
         * Adds a [LessonPeriod] to the [TimetablePage].
         *
         * @param lessonPeriod The [LessonPeriod] to add.
         * @return This [builder's][Builder] instance back.
         */
        fun addLessonPeriod(lessonPeriod: LessonPeriod): Builder
        {
            lessonPeriods.add(lessonPeriod)
            return this
        }

        /**
         * Sets all the [LessonSpots][LessonSpot].
         *
         * @param day The day to set the [lessonSpots] to.
         * @param lessonSpots The [LessonSpots][LessonSpot] to use.
         * @return This [builder's][Builder] instance back.
         */
        fun setLessonSpots(day: DayOfWeek, lessonSpots: Iterable<LessonSpot>): Builder
        {
            this.timetable.getOrPut(day) { emptyMutableLinkedList() }.setAll(lessonSpots)
            return this
        }

        /**
         * Adds a [LessonSpot] to a day.
         * Can be `null`, if there is no lesson at that time.
         * The [LessonSpot] gets appended to the end.
         *
         * @param day    The day to add the [LessonSpot] to.
         * @param lessonSpot The [LessonSpot] to add.
         * @return This [builder's][Builder] instance back.
         */
        fun addLessonSpot(day: DayOfWeek, lessonSpot: LessonSpot): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the lesson is added to that list. */
            timetable.getOrPut(day) { emptyMutableLinkedList() }.add(lessonSpot)
            return this
        }

        /**
         * Shorthand for `addLessonSpot(day, hour, new LessonSpot(lesson))`.
         * @see .setLessonSpot
         * @see LessonSpot
         */
        fun addLesson(day: DayOfWeek, lesson: Lesson): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the lesson is added to that list. */
            timetable.getOrPut(day) { emptyMutableLinkedList() }.add(LessonSpot(lesson))
            return this
        }

        fun build(): Timetable
        {
            /* TODO: Maybe check if there is equal or more lessonPeriods than lessons in any day?
             * Because that would mean there is a lesson without specified LessonPeriod. */
            return Timetable(lessonPeriods, timetable)
        }
    }
}
