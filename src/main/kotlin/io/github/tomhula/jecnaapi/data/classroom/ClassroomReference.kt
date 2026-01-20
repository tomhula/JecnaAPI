package io.github.tomhula.jecnaapi.data.classroom

import io.github.tomhula.jecnaapi.serialization.ClassroomReferenceSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ClassroomReferenceSerializer::class)
data class ClassroomReference(
    val name: String,
    val roomCode: String
)
