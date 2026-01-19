package io.github.tomhula.jecnaapi.web

import io.github.tomhula.jecnaapi.web.Auth.Companion.decrypt
import java.nio.charset.StandardCharsets

/**
 * Simply holds a username and password pair.
 * Defines [encrypt] and [decrypt] methods.
 */
data class Auth(val username: String, val password: String)
{
    /**
     * Simply encrypts this login into `byte[]` so it isn't easily human-readable.
     * **Doesn't secure the information!**
     *
     * @return The encrypted login.
     */
    fun encrypt(): ByteArray
    {
        val usernameBytes = username.toByteArray(StandardCharsets.UTF_8)
        val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)

        /* The +1 in size is for the new line. (\n) */
        val bytes = ByteArray(usernameBytes.size + passwordBytes.size + 1)

        /* Copies usernameBytes, new line and passwordBytes in order into bytes. */
        System.arraycopy(usernameBytes, 0, bytes, 0, usernameBytes.size)
        bytes[usernameBytes.size] = '\n'.code.toByte()
        System.arraycopy(passwordBytes, 0, bytes, usernameBytes.size + 1, passwordBytes.size)

        /* Shift the bytes, so each character's byte is shifted. */
        for (i in bytes.indices)
            bytes[i] = (bytes[i] + 10).toByte()

        return bytes
    }

    companion object
    {
        /**
         * Decrypt encrypted [Auth].
         * @see [encrypt]
         * @param bytes The encrypted bytes.
         * @return The [Auth] instance.
         */
        @JvmStatic
        fun decrypt(bytes: ByteArray): Auth
        {
            /* Shift the characters bytes back. */
            for (i in bytes.indices)
                bytes[i] = (bytes[i] - 10).toByte()

            val asString = String(bytes, StandardCharsets.UTF_8)
            val split = asString.split("\n", limit = 2)

            val username = split[0]
            val password = split[1]

            return Auth(username, password)
        }
    }
}
