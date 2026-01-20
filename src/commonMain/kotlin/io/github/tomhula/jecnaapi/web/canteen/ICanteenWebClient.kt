package io.github.tomhula.jecnaapi.web.canteen

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.github.tomhula.jecnaapi.parser.HtmlElementNotFoundException
import io.github.tomhula.jecnaapi.web.Auth
import io.github.tomhula.jecnaapi.web.AuthWebClient
import io.github.tomhula.jecnaapi.web.AuthenticationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class ICanteenWebClient(
    val userAgent: String? = null,
    var autoLogin: Boolean = false
) : AuthWebClient
{
    private val cookieStorage = AcceptAllCookiesStorage()

    private val httpClient = HttpClient {
        install(HttpCookies) {
            storage = cookieStorage
        }
        defaultRequest {
            url {
                takeFrom("$ENDPOINT/$CANTEEN_CODE/")
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
    var lastSuccessfulLoginAuth: Auth? = null
        private set
    @OptIn(ExperimentalTime::class)
    var lastSuccessfulLoginTime: Instant? = null
        private set

    suspend fun getCsrfTokenFromCookie() = cookieStorage.get(Url("$ENDPOINT/$CANTEEN_CODE"))["XSRF-TOKEN"]?.value

    /** Tries to find a value of any `input` tag with name `_csrf`. */
    fun findCsrfToken(html: String) = Ksoup
        .parse(html)
        .selectFirst("input[name=_csrf]")
        ?.attr("value")

    fun HttpResponse.isRedirect() = status.value in 300..399

    fun findCsrfTokenOrThrow(html: String) = findCsrfToken(html)
        ?: throw HtmlElementNotFoundException.byName("CSRF token")

    @OptIn(ExperimentalTime::class)
    override suspend fun login(auth: Auth): Boolean
    {
        // Cannot be done like this: (requesting the page only if we don't have the token from cookie)
        // val csrfToken = getCsrfTokenFromCookie() ?: findCsrfTokenOrThrow(queryStringBody("login"))
        // This wouldn't work, because the login page request is necessary to get a new session cookie.
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

        val successful = !loginPostResponse.locationHeader()!!.contains("login_error=1")

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

    override suspend fun query(path: String, parameters: Parameters?): HttpResponse
    {
        val response = httpClient.get(path) {
            if (parameters != null)
                url.parameters.appendAll(parameters)
        }

        /* Autologin mechanism */
        if (response.locationHeader() == "$ENDPOINT/$CANTEEN_CODE/login" ||
            response.locationHeader()?.startsWith("/$CANTEEN_CODE/index.jsp") == true
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

    private fun HttpResponse.locationHeader() = headers[HttpHeaders.Location]

    companion object
    {
        const val ENDPOINT = "https://strav.nasejidelna.cz"
        const val CANTEEN_CODE = "0341"
    }
}
