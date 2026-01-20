package io.github.tomhula.jecnaapi.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference

internal object TeacherReferenceSerializer : KSerializer<TeacherReference>
{
    override val descriptor = PrimitiveSerialDescriptor("Name", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TeacherReference)
    {
        encoder.encodeString(value.fullName + "$" + value.tag)
    }

    override fun deserialize(decoder: Decoder): TeacherReference
    {
        val split = decoder.decodeString().split("$")
        return TeacherReference(split[0], split[1])
    }
}
