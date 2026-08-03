package io.github.tomhula.jecnaapi

import io.github.tomhula.jecnaapi.data.canteen.DayMenu
import io.github.tomhula.jecnaapi.data.canteen.ExchangeItem
import io.github.tomhula.jecnaapi.data.canteen.MenuItem
import io.github.tomhula.jecnaapi.data.canteen.MenuPage
import io.github.tomhula.jecnaapi.data.canteen.Orderable
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.web.Auth
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * A client to read and order menus.
 */
interface CanteenClient
{
    @Throws(RuntimeException::class)
    suspend fun login(auth: Auth): Boolean
    @Throws(RuntimeException::class)
    suspend fun login(username: String, password: String) = login(Auth(username, password))
    @Throws(RuntimeException::class)
    suspend fun logout()
    @Throws(RuntimeException::class)
    suspend fun isLoggedIn(): Boolean
    /** Prefer using [getMenuAsync] instead. */
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getMenuPage(): MenuPage
    @Throws(ParseException::class, RuntimeException::class)
    fun getMenuAsync(days: Iterable<LocalDate>): Flow<DayMenu>
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getDayMenu(day: LocalDate): DayMenu
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getExchange(): List<ExchangeItem>
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getCredit(): Float
    /**
     * Places an order for the given [orderable].
     *
     * When ordering an [ExchangeItem], all other cached data will become invalid and must be refetched.
     * Ordering an [ExchangeItem] will always return `0F` because no new credit can be obtained.
     *
     * @param orderable The [io.github.tomhula.jecnaapi.canteen.Orderable] to order.
     * @return -1f for success with no credit info, non-negative value for a new credit, or null for failure.
     */
    @Throws(RuntimeException::class)
    suspend fun order(orderable: Orderable): Float?
    @Throws(RuntimeException::class)
    suspend fun putOnExchange(menuItem: MenuItem): Boolean

    companion object
    {
        operator fun invoke(
            endpoint: String = WebCanteenClient.OFFICIAL_ENDPOINT,
            canteenCode: String = WebCanteenClient.OFFICIAL_CANTEEN_CODE,
            userAgent: String? = "JAPI",
            autoLogin: Boolean = false
        ): CanteenClient = WebCanteenClient(endpoint, canteenCode, userAgent, autoLogin)
    }
}
