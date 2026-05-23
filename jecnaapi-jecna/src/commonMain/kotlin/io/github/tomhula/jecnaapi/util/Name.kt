package io.github.tomhula.jecnaapi.util

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.serialization.NameSerializer

/**
 * Holds a full and short name of a subject.
 * The short name is optional, can be null.
 * [Name] objects are only equal, when their [full] is equal, [short] is not considered.
 * [Name] can also be equal to a [String], when the [String] equals to the [full].
 * Same goes with hash code.
 */
@Serializable(with = NameSerializer::class)
data class Name(val full: String, val short: String? = null)
{
    /**
     * [Name] objects are only equal, when their [full] is equal, [short] is not considered.
     * [Name] can also be equal to a [String], when the [String] equals to the [full].
     */
    override fun equals(other: Any?): Boolean
    {
        return when (other)
        {
            is String -> full == other
            is Name   -> full == other.full
            else      -> false
        }
    }

    /**
     * @return `full.hashCode()`
     */
    override fun hashCode(): Int
    {
        return full.hashCode()
    }

    override fun toString(): String
    {
        return if (short != null)
            "$full ($short)"
        else
            full
    }
}

/**
 * Creates [Name] with this [String] as it's full name. The short is left `null`.
 */
fun String.toName() = Name(this)
