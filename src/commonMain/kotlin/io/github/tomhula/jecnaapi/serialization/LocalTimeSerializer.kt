package io.github.tomhula.jecnaapi.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.datetime.LocalTime

internal object LocalTimeSerializer : KSerializer<LocalTime>
{
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString())
}
