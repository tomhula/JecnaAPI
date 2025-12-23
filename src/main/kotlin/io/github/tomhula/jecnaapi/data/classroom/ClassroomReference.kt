package io.github.tomhula.jecnaapi.data.classroom

import io.github.tomhula.jecnaapi.serialization.ClassroomReferenceSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ClassroomReferenceSerializer::class)
class ClassroomReference(
    val title: String
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassroomReference

        return title == other.title
    }

    override fun hashCode(): Int
    {
        return title.hashCode()
    }

    override fun toString(): String
    {
        return "ClassroomReference(title='$title')"
    }
}
