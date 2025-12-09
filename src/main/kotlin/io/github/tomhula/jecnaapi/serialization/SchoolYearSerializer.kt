package io.github.tomhula.jecnaapi.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import io.github.tomhula.jecnaapi.util.SchoolYear

internal object SchoolYearSerializer : KSerializer<SchoolYear>
{
    override val descriptor = PrimitiveSerialDescriptor("SchoolYear", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SchoolYear) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder) = SchoolYear.fromString(decoder.decodeString())
}
