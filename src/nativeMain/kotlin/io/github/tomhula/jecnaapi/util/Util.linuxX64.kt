package io.github.tomhula.jecnaapi.util

import io.ktor.client.engine.curl.*
import io.ktor.client.engine.*

/**
 * Returns the Curl Ktor Engine factory for native.
 */
actual fun getKtorEngine(): HttpClientEngineFactory<*> = Curl
