package io.github.tomhula.jecnaapi.data.grade

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.util.Name

@Serializable
data class Subject(
    val name: Name,
    val grades: Grades,
    val finalGrade: FinalGrade? = null
)
