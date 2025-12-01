package me.tomasan7.jecnaapi

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.data.canteen.*
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParser
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParserImpl
import me.tomasan7.jecnaapi.parser.parsers.selectFirstOrThrow
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.canteen.ICanteenWebClient
import org.jsoup.Jsoup
import java.time.LocalDate

/**
 * A client to read and order menus.
 */
class CanteenClient(
    userAgent: String? = "JAPI",
    autoLogin: Boolean = false
)
{
    private val webClient = ICanteenWebClient(userAgent, autoLogin)
    private val canteenParser: HtmlCanteenParser = HtmlCanteenParserImpl

    var autoLogin by webClient::autoLogin
    val userAgent by webClient::userAgent
    /** The last [time][java.time.Instant] a call to [login] was successful (returned `true`). */
    val lastSuccessfulLoginTime by webClient::lastSuccessfulLoginTime
    /** The [Auth], that was last used in a call to [login], which was successful (returned `true`). */
    val lastSuccessfulLoginAuth by webClient::lastSuccessfulLoginAuth

    private var lastTime = 0L

    suspend fun login(username: String, password: String) = login(Auth(username, password))

    suspend fun login(auth: Auth) = webClient.login(auth)

    suspend fun logout() = webClient.logout()

    suspend fun isLoggedIn() = webClient.isLoggedIn()

    /**
     * Prefer using [getMenuAsync] instead.
     */
    suspend fun getMenuPage() = canteenParser.parse(webClient.queryStringBody(WEB_PATH))

    fun getMenuAsync(days: Iterable<LocalDate>): Flow<DayMenu> = channelFlow {
        for (day in days)
            launch {
                val dayMenu = getDayMenu(day)
                send(dayMenu)
            }
    }

    suspend fun getDayMenu(day: LocalDate): DayMenu
    {
        val dayMenuHtml = webClient.queryStringBody(
            path = "faces/secured/db/dbJidelnicekOnDayView.jsp",
            parameters = parametersOf("day", day.toString())
        )
        return canteenParser.parseDayMenu(dayMenuHtml)
    }

    suspend fun getExchange(): List<ExchangeItem>
    {
        val exchangeHtml = webClient.queryStringBody("faces/secured/burza.jsp")
        return canteenParser.parseExchange(exchangeHtml)
    }

    suspend fun getCredit(): Float
    {
        val html = webClient.queryStringBody("faces/secured/main.jsp")
        val creditEle = Jsoup.parse(html).selectFirstOrThrow("#Kredit")
        return canteenParser.parseCreditText(creditEle.text())
    }

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
    {
        val timeUpdatedOrderable = if (lastTime != 0L && orderable !is ExchangeItem)
            orderable.updated(lastTime)
        else
            orderable

        val (successful, response) = ajaxOrder(timeUpdatedOrderable.orderPath, orderable is ExchangeItem)

        if (!successful)
            return null

        if (orderable is ExchangeItem)
            return -1f // We don't have new credit data

        return try
        {
            canteenParser.parseOrderResponse(response).credit
        }
        catch (ignored: ParseException) 
        { 
            -1f
        }
    }

    suspend fun putOnExchange(menuItem: MenuItem): Boolean
    {
        if (menuItem.putOnExchangePath == null)
            return false

        val finalMenuItem = if (lastTime != 0L)
            menuItem.updated(lastTime)
        else
            menuItem
        
        return ajaxOrder(finalMenuItem.putOnExchangePath!!).first
    }

    /** Also updates [lastTime] variable. */
    private suspend fun ajaxOrder(url: String, isFromExchange: Boolean = false): Pair<Boolean, String>
    {
        val response = webClient.queryStringBody("faces/secured/$url")

        if (isFromExchange) {
            // It would be better to have some error detection,
            // but for some reason it is throwing 500 even tho it orders the food from exchange
            return true to response
        }

        /* Same check as on the official website. */
        if (response.contains("error"))
            return false to response

        val orderResponse = canteenParser.parseOrderResponse(response)
        lastTime = orderResponse.time

        return true to response
    }

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The [HttpResponse].
     */
    suspend fun query(path: String, parameters: Parameters? = null) = webClient.query(path, parameters)

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The HTTP response's body as [String].
     */
    suspend fun queryStringBody(path: String, parameters: Parameters? = null) =
        webClient.queryStringBody(path, parameters)

    companion object
    {
        private const val WEB_PATH = "faces/secured/mobile.jsp"
    }
}
