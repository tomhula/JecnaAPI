package io.github.tomhula.jecnaapi.data.substitution

import kotlinx.serialization.Serializable

/**
 * Represents hours affected by a teacher absence.
 *
 * - Range: from/to set (e.g. 2-4)
 * - Single: only [from] set (e.g. 3)
 */
@Serializable
data class AbsenceHours(
    val from: Int,
    val to: Int? = null
)
