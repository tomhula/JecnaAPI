package io.github.tomhula.jecnaapi.data.timetable

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import io.github.tomhula.jecnaapi.util.hasDuplicate
import kotlin.jvm.JvmStatic

/**
 * Represents a spot for [lessons][Lesson] in a timetable.
 * That spot can contain multiple [lessons][Lesson].
 * For example some lessons are split into more groups.
 * This class indicates the one whole lesson and contains the lessons for each group.
 *
 * @property periodSpan The number of [periods][LessonPeriod] this lesson spot spans over.
 */
@Serializable
class LessonSpot(val lessons: List<Lesson>, val periodSpan: Int = 1) : Iterable<Lesson>
{
    constructor(lesson: Lesson, periodSpan: Int = 1) : this(listOf(lesson), periodSpan)

    /** The number of lessons in this [LessonSpot]. */
    @Transient
    val size = lessons.size

    init
    {
        require(!hasDuplicateGroups(lessons)) { "Lessons cannot have duplicate groups." }
    }

    /** Returns whether there are any duplicate groups in provided [lessons][Lesson]. */
    private fun hasDuplicateGroups(lessons: Iterable<Lesson>) = lessons.hasDuplicate { it.group }

    /** Returns the [Lesson] on given [index]. */
    fun getLesson(index: Int) = lessons[index]

    /** Returns the [Lesson] on given [index]. */
    operator fun get(index: Int) = getLesson(index)

    /** Returns `true` if this [LessonSpot] contains no [lessons][Lesson]. */
    fun isEmpty() = lessons.isEmpty()

    /** Returns `true` if this [LessonSpot] contains at least one lesson. */
    fun isNotEmpty() = !isEmpty()

    /** Returns a [Lesson] with the specified [group], or `null` if there's no [Lesson] with that [group]. */
    fun getLessonByGroup(group: String) = lessons.find { it.group == group }

    /** @return An [Iterator] of [Lesson], which doesn't modify this [LessonSpot]. */
    override fun iterator() = lessons.iterator()

    override fun toString(): String
    {
        return "LessonSpot{" +
               "lessons=" + lessons +
                ", periodSpan=" + periodSpan +
               '}'
    }

    companion object
    {
        /** Creates a [LessonSpot] with no [lessons][Lesson] and provided [periodSpan]. */
        @JvmStatic
        fun empty(periodSpan: Int = 1) = LessonSpot(emptyList(), periodSpan)
    }
}
