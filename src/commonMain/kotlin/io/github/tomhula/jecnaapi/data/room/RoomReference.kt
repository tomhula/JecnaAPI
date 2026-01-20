package io.github.tomhula.jecnaapi.data.room

import kotlinx.serialization.Serializable

@Serializable
data class RoomReference(
    val name: String,
    val roomCode: String
)
