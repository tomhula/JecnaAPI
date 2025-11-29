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
import java.time.format.DateTimeFormatter

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

    suspend fun getCredit(): Float
    {
        val html = webClient.queryStringBody("faces/secured/main.jsp")
        val creditEle = Jsoup.parse(html).selectFirstOrThrow("#Kredit")
        return canteenParser.parseCreditText(creditEle.text())
    }

    /**
     * Orders the [menuItem].
     *
     * @param menuItem The [MenuItem] to order.
     * @return Either new credit or null, if something went wrong.
     */
    suspend fun order(menuItem: MenuItem): Float?
    {
        if (!menuItem.isEnabled)
            return null

        val finalMenuItem = if (lastTime != 0L)
            menuItem.updated(lastTime)
        else
            menuItem

        val (successful, response) = ajaxOrder(finalMenuItem.orderPath)

        if (!successful)
            return null

        return try
        {
            canteenParser.parseOrderResponse(response).credit
        }
        catch (ignored: ParseException) { null }
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
    private suspend fun ajaxOrder(url: String): Pair<Boolean, String>
    {
        val response = webClient.queryStringBody("faces/secured/$url")

        /* Same check as on the official website. */
        if (response.contains("error"))
            return false to response

        val orderResponse = canteenParser.parseOrderResponse(response)
        lastTime = orderResponse.time

        return true to response
    }

    /**
     * Returns a [MenuItem] with updated time in the [MenuItem.orderPath] and possibly [MenuItem.putOnExchangePath].
     */
    private fun MenuItem.updated(time: Long): MenuItem
    {
        val newOrderPath = orderPath.replace(TIME_REPLACE_REGEX, time.toString())
        val newPutOnExchangePath = putOnExchangePath?.replace(TIME_REPLACE_REGEX, time.toString())
        return copy(orderPath = newOrderPath, putOnExchangePath = newPutOnExchangePath)
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
        private val TIME_REPLACE_REGEX = Regex("""(?<=time=)\d{13}""")
    }
}
