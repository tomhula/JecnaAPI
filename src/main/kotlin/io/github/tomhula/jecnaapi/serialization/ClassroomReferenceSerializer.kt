package io.github.tomhula.jecnaapi.serialization

import io.github.tomhula.jecnaapi.data.classroom.ClassroomReference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object ClassroomReferenceSerializer: KSerializer<ClassroomReference>
{
    override val descriptor = PrimitiveSerialDescriptor("ClassroomReference", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ClassroomReference)
    {

        encoder.encodeString("${value.title}|${value.symbol}")
    }

    override fun deserialize(decoder: Decoder): ClassroomReference
    {
        val raw = decoder.decodeString()
        val parts = raw.split('|', limit = 2)
        return if (parts.size == 2)
        {
            ClassroomReference(title = parts[0], symbol = parts[1])
        }
        else
        {
            ClassroomReference(title = raw, symbol = raw)
        }
    }
}
