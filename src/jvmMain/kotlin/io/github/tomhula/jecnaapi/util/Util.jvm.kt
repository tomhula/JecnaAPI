package io.github.tomhula.jecnaapi.util

import io.ktor.client.engine.cio.*
import io.ktor.client.engine.*

/**
 * Returns the CIO Ktor Engine factory for JVM.
 */
actual fun getKtorEngine(): HttpClientEngineFactory<*> = CIO
