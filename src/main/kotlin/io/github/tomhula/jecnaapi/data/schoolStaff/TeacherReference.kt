package io.github.tomhula.jecnaapi.data.schoolStaff

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.serialization.TeacherReferenceSerializer

@Serializable(with = TeacherReferenceSerializer::class)
class TeacherReference(
    val fullName: String,
    tag: String
)
{
    val tag = tag.trim().lowercase().replaceFirstChar { it.uppercaseChar() }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TeacherReference

        if (fullName != other.fullName) return false
        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = fullName.hashCode()
        result = 31 * result + tag.hashCode()
        return result
    }

    override fun toString() = "$fullName ($tag)"
}
