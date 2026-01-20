@file:UseSerializers(LocalTimeSerializer::class)

package io.github.tomhula.jecnaapi.data.timetable

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import io.github.tomhula.jecnaapi.serialization.LocalTimeSerializer
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlin.jvm.JvmStatic

/**
 * Represents a [Lesson]'s time period in a timetable.
 * @param from Lesson's starting time.
 * @param to Lesson's ending time.
 */
@Serializable
data class LessonPeriod(
    val from: LocalTime,
    val to: LocalTime
) : ClosedRange<LocalTime>
{
    override val start = from

    override val endInclusive = to

    override fun toString() = from.toFormattedString() + " - " + to.toFormattedString()

    companion object
    {
        private fun LocalTime.toFormattedString() = format(timeFormat)
        
        private val timeFormat = LocalTime.Format { 
            hour(Padding.NONE)
            char(':')
            minute(Padding.ZERO)
        }

        /**
         * Parses [LessonPeriod] from [String]. **The [String] must be in a "H:m - H:m" format.**
         * @param string The [String] to parse from.
         * @throws IllegalArgumentException When the provided [String] is in incorrect format.
         * @return The parsed [LessonPeriod].
         */
        @JvmStatic
        fun fromString(string: String): LessonPeriod
        {
            val split = string.split(" - ")

            try
            {
                return LessonPeriod(LocalTime.parse(split[0], timeFormat), LocalTime.parse(split[1], timeFormat))
            }
            catch (e: IndexOutOfBoundsException)
            {
                throw IllegalArgumentException("Provided string wasn't in correct format. Expected format \"HH:mm - HH:mm\", got \"$string\".")
            }
        }
    }
}
