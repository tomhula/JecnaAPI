package io.github.tomhula.jecnaapi

import io.github.tomhula.jecnaapi.data.canteen.*
import io.github.tomhula.jecnaapi.web.Auth
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * A client to read and order menus.
 */
interface CanteenClient
{
    suspend fun login(auth: Auth): Boolean
    suspend fun login(username: String, password: String) = login(Auth(username, password))
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
    /** Prefer using [getMenuAsync] instead. */
    suspend fun getMenuPage(): MenuPage
    fun getMenuAsync(days: Iterable<LocalDate>): Flow<DayMenu>
    suspend fun getDayMenu(day: LocalDate): DayMenu
    suspend fun getExchange(): List<ExchangeItem>
    suspend fun getCredit(): Float
    /**
     * Places an order for the given [orderable].
     *
     * When ordering an [ExchangeItem], all other cached data will become invalid and must be refetched.
     * Ordering an [ExchangeItem] will always return `0F` because no new credit can be obtained.
     *
     * @param orderable The [Orderable] to order.
     * @return -1f for success with no credit info, non-negative value for a new credit, or null for failure.
     */
    suspend fun order(orderable: Orderable): Float?
    suspend fun putOnExchange(menuItem: MenuItem): Boolean

    companion object
    {
        operator fun invoke(
            userAgent: String? = "JAPI",
            autoLogin: Boolean = false
        ): CanteenClient = WebCanteenClient(userAgent, autoLogin)
    }
}
