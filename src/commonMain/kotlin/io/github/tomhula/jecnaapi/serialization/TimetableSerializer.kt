package io.github.tomhula.jecnaapi.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import io.github.tomhula.jecnaapi.data.timetable.LessonPeriod
import io.github.tomhula.jecnaapi.data.timetable.LessonSpot
import io.github.tomhula.jecnaapi.data.timetable.Timetable
import kotlinx.datetime.DayOfWeek

internal object TimetableSerializer : KSerializer<Timetable>
{
    /* https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#composite-serializer-via-surrogate */

    override val descriptor = TimetableSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Timetable)
    {
        val timetable = mutableMapOf<DayOfWeek, List<LessonSpot>>()
        for (day in value.days)
            timetable[day] = value.getLessonSpotsForDay(day)!!

        val surrogate = TimetableSurrogate(value.lessonPeriods, timetable)

        encoder.encodeSerializableValue(TimetableSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): Timetable
    {
        val surrogate = decoder.decodeSerializableValue(TimetableSurrogate.serializer())
        val timetableBuilder = Timetable.builder()
        timetableBuilder.setLessonPeriods(surrogate.lessonPeriods)

        surrogate.timetable.forEach { (day, lessonSpots) ->
            timetableBuilder.setLessonSpots(day, lessonSpots)
        }

        return timetableBuilder.build()
    }

    @Serializable
    private data class TimetableSurrogate(
        val lessonPeriods: List<LessonPeriod>,
        val timetable: Map<DayOfWeek, List<LessonSpot>>
    )
}
