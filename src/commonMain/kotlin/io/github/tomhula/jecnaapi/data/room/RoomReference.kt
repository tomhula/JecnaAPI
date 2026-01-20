package io.github.tomhula.jecnaapi.data.room

import io.github.tomhula.jecnaapi.serialization.RoomReferenceSerializer
import kotlinx.serialization.Serializable

@Serializable(with = RoomReferenceSerializer::class)
data class RoomReference(
    val name: String,
    val roomCode: String
)
