package io.github.tomhula.jecnaapi.data.classroom

import io.github.tomhula.jecnaapi.serialization.ClassroomReferenceSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ClassroomReferenceSerializer::class)
class ClassroomReference(
    /** Human-readable classroom name, without extra details like manager in parentheses. */
    val title: String,
    /** URL segment used to open the classroom page, e.g. "21", "K5", "Byt+%C5%A1koln%C3%ADka". */
    val symbol: String
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassroomReference

        return title == other.title && symbol == other.symbol
    }

    override fun hashCode(): Int
    {
        var result = title.hashCode()
        result = 31 * result + symbol.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "ClassroomReference(title='$title', symbol='$symbol')"
    }
}
