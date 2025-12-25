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

        encoder.encodeString(value.title + "$" + value.symbol)
    }

    override fun deserialize(decoder: Decoder): ClassroomReference
    {
        val split = decoder.decodeString().split("$")
        return ClassroomReference(split[0], split[1])
    }
}
