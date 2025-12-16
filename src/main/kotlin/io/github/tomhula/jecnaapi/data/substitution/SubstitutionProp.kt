package io.github.tomhula.jecnaapi.data.substitution
import kotlinx.serialization.Serializable
/**
 * Properties related to substitutions, such as preparation status and date.
 */
@Serializable
data class SubstitutionProp(
    val priprava: Boolean,
    val date: String,
)




