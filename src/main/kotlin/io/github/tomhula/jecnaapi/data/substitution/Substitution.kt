package io.github.tomhula.jecnaapi.data.substitution

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SubstitutionResponse(
    val schedule: List<Map<String, JsonElement>>,
    val props: List<SubstitutionProp>,
    val status: SubstitutionStatus
)

@Serializable
data class SubstitutionProp(
    val date: String,
    val priprava: Boolean
)

@Serializable
data class SubstitutionStatus(
    val lastUpdated: String,
    val currentUpdateSchedule: Int
)

