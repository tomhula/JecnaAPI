package io.github.tomhula.jecnaapi.data.classroom

import io.github.tomhula.jecnaapi.serialization.ClassroomReferenceSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ClassroomReferenceSerializer::class)
class ClassroomReference(
    val title: String,
    val floor: String,
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassroomReference

        if (title != other.title) return false
        if (floor != other.floor) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = title.hashCode()
        result = 31 * result + floor.hashCode()
        return result
    }
    
    override fun toString(): String
    {
        return "ClassroomReference(title='$title', floor='$floor')"
    }
}
