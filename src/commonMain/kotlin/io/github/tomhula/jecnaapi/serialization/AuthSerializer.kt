package io.github.tomhula.jecnaapi.serialization

import io.github.tomhula.jecnaapi.web.Auth
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object AuthSerializer: KSerializer<Auth>
{
    override val descriptor = SerialDescriptor("Auth", ByteArraySerializer().descriptor)

    override fun serialize(encoder: Encoder, value: Auth)
    {
        val bytes = value.encrypt()
        encoder.encodeSerializableValue(ByteArraySerializer(), bytes)
    }

    override fun deserialize(decoder: Decoder): Auth
    {
        val bytes = decoder.decodeSerializableValue(ByteArraySerializer())
        return Auth.decrypt(bytes)
    }
}
