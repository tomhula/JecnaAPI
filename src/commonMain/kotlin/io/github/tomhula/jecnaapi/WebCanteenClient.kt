package io.github.tomhula.jecnaapi

import com.fleeksoft.ksoup.Ksoup
import io.github.tomhula.jecnaapi.data.canteen.*
import io.github.tomhula.jecnaapi.parser.HtmlElementNotFoundException
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.parser.parsers.CanteenParser
import io.github.tomhula.jecnaapi.parser.parsers.selectFirstOrThrow
import io.github.tomhula.jecnaapi.web.Auth
import io.github.tomhula.jecnaapi.web.AuthenticationException
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

class WebCanteenClient(
    endpoint: String,
    private val canteenCode: String,
    val userAgent: String?,
    var autoLogin: Boolean = false
) : CanteenClient
{
    val endpoint = endpoint.removeSuffix("/")
    
    private val cookieStorage = AcceptAllCookiesStorage()
    private val httpClient = HttpClient {
        install(HttpCookies) { storage = cookieStorage }
        defaultRequest {
            url {
                takeFrom("$endpoint/$canteenCode/")
                parameters.append("terminal", "false")
                parameters.append("printer", "false")
                parameters.append("keyboard", "false")
                parameters.append("status", "true")
            }
            if (userAgent != null)
                userAgent(userAgent)
            else
                headers.remove(HttpHeaders.UserAgent)
        }
        followRedirects = false
        // Debugging only
        /*install(Logging) {
            logger = object: Logger
            {
                override fun log(message: String)
                {
                    if (!message.contains("REQUEST"))
                        return
                    val message1 = message.split("\n")[0].replace("$ENDPOINT/$CANTEEN_CODE", "")
                    println(message1)
                }
            }
            level = LogLevel.INFO
        }*/
    }

    private var autoLoginAttempted = false
    /** The [Auth], that was last used in a call to [login], which was successful (returned `true`). */
    var lastSuccessfulLoginAuth: Auth? = null
        private set
    /** The last [time][kotlin.time.Instant] a call to [login] was successful (returned `true`). */
    @OptIn(ExperimentalTime::class)
    var lastSuccessfulLoginTime: Instant? = null
        private set

    private var lastTime: Long? = null
    
    private val canteenParser = CanteenParser

    @OptIn(ExperimentalTime::class)
    override suspend fun login(auth: Auth): Boolean
    {
        val csrfToken = findCsrfTokenOrThrow(queryStringBody("login"))

        val loginPostResponse = httpClient.submitForm(
            url = "j_spring_security_check",
            formParameters = Parameters.build {
                append("j_username", auth.username)
                append("j_password", auth.password)
                append("_spring_security_remember_me", "on")
                append("type", "web")
                append("_csrf", csrfToken)
                append("targetUrl", "/")
            })

        val successful = !loginPostResponse.locationHeader!!.contains("login_error=1")

        if (successful)
        {
            lastSuccessfulLoginAuth = auth
            lastSuccessfulLoginTime = Clock.System.now()
        }

        return successful
    }

    override suspend fun logout()
    {
        val csrfToken = getCsrfTokenFromCookie() ?: run {
            val response = query("faces/secured/main.jsp")
            /* Redirects to login, if no one is logged in */
            if (response.isRedirect())
                return

            findCsrfTokenOrThrow(response.bodyAsText())
        }

        httpClient.submitForm("logout", parametersOf("_csrf", csrfToken))
        httpClient.get("logoutall")

        lastSuccessfulLoginAuth = null
    }

    override suspend fun isLoggedIn() = !query("faces/secured/main.jsp").isRedirect()

    override suspend fun getMenuPage() = canteenParser.parse(queryStringBody(WEB_PATH))

    override fun getMenuAsync(days: Iterable<LocalDate>): Flow<DayMenu> = channelFlow {
        for (day in days)
            launch {
                val dayMenu = getDayMenu(day)
                send(dayMenu)
            }
    }

    override suspend fun getDayMenu(day: LocalDate): DayMenu
    {
        val dayMenuHtml = queryStringBody(
            path = "faces/secured/db/dbJidelnicekOnDayView.jsp",
            parameters = parametersOf("day", day.toString())
        )
        return canteenParser.parseDayMenu(dayMenuHtml)
    }

    override suspend fun getExchange(): List<ExchangeItem>
    {
        val exchangeHtml = queryStringBody("faces/secured/burza.jsp")
        return canteenParser.parseExchange(exchangeHtml)
    }

    override suspend fun getCredit(): Float
    {
        val html = queryStringBody("faces/secured/main.jsp")
        val creditEle = Ksoup.parse(html).selectFirstOrThrow("#Kredit")
        return canteenParser.parseCreditText(creditEle.text())
    }

    override suspend fun order(orderable: Orderable): Float?
    {
        val timeUpdatedOrderable = if (lastTime != null && orderable !is ExchangeItem)
            orderable.updated(lastTime!!)
        else
            orderable

        val (successful, response) = ajaxOrder(timeUpdatedOrderable.orderPath, orderable is ExchangeItem)

        if (!successful)
            return null

        if (orderable is ExchangeItem)
        {
            lastTime = null
            return -1f // We don't have new credit data
        }

        return try
        {
            canteenParser.parseOrderResponse(response).credit
        }
        catch (_: ParseException)
        {
            -1f
        }
    }

    override suspend fun putOnExchange(menuItem: MenuItem): Boolean
    {
        if (menuItem.putOnExchangePath == null)
            return false

        val finalMenuItem = if (lastTime != null)
            menuItem.updated(lastTime!!)
        else
            menuItem

        return ajaxOrder(finalMenuItem.putOnExchangePath!!).first
    }

    suspend fun getCsrfTokenFromCookie() = cookieStorage.get(Url("$endpoint/$canteenCode"))["XSRF-TOKEN"]?.value

    /** Tries to find a value of any `input` tag with name `_csrf`. */
    fun findCsrfToken(html: String) = Ksoup
        .parse(html)
        .selectFirst("input[name=_csrf]")
        ?.attr("value")

    private fun HttpResponse.isRedirect() = status.value in 300..399

    private fun findCsrfTokenOrThrow(html: String) = findCsrfToken(html)
        ?: throw HtmlElementNotFoundException.byName("CSRF token")

    /** Also updates [lastTime] variable. */
    private suspend fun ajaxOrder(url: String, isFromExchange: Boolean = false): Pair<Boolean, String>
    {
        val response = queryStringBody("faces/secured/$url")

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
    suspend fun query(path: String, parameters: Parameters? = null): HttpResponse
    {
        val response = httpClient.get(path) {
            if (parameters != null)
                url.parameters.appendAll(parameters)
        }

        /* Autologin mechanism */
        if (response.locationHeader == "$endpoint/$canteenCode/login" ||
            response.locationHeader?.startsWith("/$canteenCode/index.jsp") == true
        )
        {
            if (autoLogin && !autoLoginAttempted)
            {
                autoLoginAttempted = true
                login(lastSuccessfulLoginAuth ?: throw AuthenticationException())
                return query(path, parameters)
            }
            else
                throw AuthenticationException()
        }

        return response
    }

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The HTTP response's body as [String].
     */
    suspend fun queryStringBody(path: String, parameters: Parameters? = null) =
        query(path, parameters).bodyAsText()

    private val HttpResponse.locationHeader
        get() = headers[HttpHeaders.Location]

    companion object
    {
        private const val WEB_PATH = "faces/secured/mobile.jsp"
    }
}
