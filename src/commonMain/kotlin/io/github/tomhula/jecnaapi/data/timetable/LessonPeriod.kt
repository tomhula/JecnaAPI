@file:UseSerializers(LocalTimeSerializer::class)

package io.github.tomhula.jecnaapi.data.timetable

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import io.github.tomhula.jecnaapi.serialization.LocalDateSerializer
import io.github.tomhula.jecnaapi.serialization.LocalTimeSerializer
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

    override fun toString() = from.format(formatter) + " - " + to.format(formatter)

    companion object
    {
        private val formatter = DateTimeFormatter.ofPattern("H:mm")

        /**
         * Parses [LessonPeriod] from [String]. **The [String] must be in a "HH:mm - HH:mm" format.**
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
                return LessonPeriod(LocalTime.parse(split[0], formatter),
                                    LocalTime.parse(split[1], formatter))
            }
            catch (e: IndexOutOfBoundsException)
            {
                throw IllegalArgumentException("Provided string wasn't in correct format. Expected format \"HH:mm - HH:mm\", got \"$string\".")
            }
        }
    }
}
