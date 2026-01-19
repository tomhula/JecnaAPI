package io.github.tomhula.jecnaapi.data.classroom

import io.github.tomhula.jecnaapi.serialization.ClassroomReferenceSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ClassroomReferenceSerializer::class)
class ClassroomReference(
    val name: String,
    val roomCode: String
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassroomReference

        return name == other.name && roomCode == other.roomCode
    }

    override fun hashCode(): Int
    {
        var result = name.hashCode()
        result = 31 * result + roomCode.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "ClassroomReference(title='$name', symbol='$roomCode')"
    }
}
