package io.github.tomhula.jecnaapi.serialization

import io.github.tomhula.jecnaapi.data.room.RoomReference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object RoomReferenceSerializer : KSerializer<RoomReference>
{
    override val descriptor = PrimitiveSerialDescriptor("RoomReference", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: RoomReference)
    {
        encoder.encodeString(value.name + "$" + value.roomCode)
    }

    override fun deserialize(decoder: Decoder): RoomReference
    {
        val split = decoder.decodeString().split("$")
        return RoomReference(split[0], split[1])
    }
}
