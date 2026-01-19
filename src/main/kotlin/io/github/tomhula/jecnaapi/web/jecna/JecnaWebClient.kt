package io.github.tomhula.jecnaapi.web.jecna

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.github.tomhula.jecnaapi.web.Auth
import io.github.tomhula.jecnaapi.web.AuthWebClient
import io.github.tomhula.jecnaapi.web.AuthenticationException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.Instant
import kotlin.time.Duration

/**
 * Http client for accessing the Ječná web.
 *
 * @param autoLogin Saves [Auth] that led to successful [login] result.
 * Then when calling [query] and it fails because of [AuthenticationException], [login] is called with the saved [Auth] and the request retried.
 * If it fails again, [AuthenticationException] is thrown.
 */
class JecnaWebClient(
    requestTimeout: Duration,
    var autoLogin: Boolean = false,
    val userAgent: String? = null
) : AuthWebClient
{
    private val cookieStorage = AcceptAllCookiesStorage()
    private val httpClient = HttpClient(CIO) {
        install(HttpCookies) {
            storage = cookieStorage
        }
        defaultRequest {
            url(ENDPOINT)
            if (userAgent != null)
                userAgent(userAgent)
            else
                headers.remove(HttpHeaders.UserAgent)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeout.inWholeMilliseconds
        }
        followRedirects = false
    }
    private var autoLoginAttempted = false

    /**
     * [Auth] used by [autoLogin]. Is automatically updated by [login] on a successful login.
     * Is set to `null` on [logout].
     */
    var autoLoginAuth: Auth? = null
    var lastSuccessfulLoginTime: Instant? = null
        private set
    /* Value may be incorrect if the session has expired */
    var role: Role? = null
        private set

    suspend fun getCookieValue(name: String) = getCookie(name)?.value

    suspend fun setCookie(name: String, value: String) = cookieStorage.addCookie(ENDPOINT, Cookie(name, value))

    suspend fun getCookie(name: String) = cookieStorage.get(Url(ENDPOINT)).firstOrNull { it.name == name }

    suspend fun getSessionCookie() = getCookie(SESSION_ID_COOKIE_NAME)

    override suspend fun login(auth: Auth): Boolean
    {
        val rootStudentPageDocument = Jsoup.parse(getRootStudentPageHtml())

        /* If logout button is found, the user is already logged in. */
        if (rootStudentPageDocument.selectFirst("""[href="/user/logout"]""") != null)
            return true

        val token3 = findToken3(rootStudentPageDocument)
            ?: throw IllegalStateException("Token3 not found.")

        val response = httpClient.submitForm(
            "/user/login",
            formParameters = Parameters.build {
                append("user", auth.username)
                append("pass", auth.password)
                append("token3", token3)
            }
        )

        if (response.status != HttpStatusCode.Found)
            return false

        val locationHeader = response.headers[HttpHeaders.Location] ?: return false

        if (locationHeader != "/")
            return false

        autoLoginAuth = auth
        lastSuccessfulLoginTime = Instant.now()

        return true
    }

    suspend fun setRole(role: Role)
    {
        /* Refer to Role section in /internal_docs/Jecna_server.md */
        setCookie("WTDGUID", ROLE_TO_WTDGUID_COOKIE_VALUE_MAP[role]!!)
        //plainQuery("/user/role", parametersOf("role", role.value))
        this.role = role
    }

    override suspend fun logout()
    {
        autoLoginAuth = null
        plainQuery("/user/logout")
    }

    /* Responds with status 302 (redirect to login page) when user is not logged in. */
    override suspend fun isLoggedIn() = plainQuery(LOGIN_TEST_ENDPOINT).status == HttpStatusCode.OK

    /** A query without any authentication (autologin) handling. */
    suspend fun plainQuery(path: String, parameters: Parameters? = null): HttpResponse
    {
        val response = httpClient.get(path) {
            parameters?.let { url.parameters.appendAll(it) }
        }
        return response
    }

    /** A query without any authentication (autologin) handling. */
    suspend fun plainQueryStringBody(path: String, parameters: Parameters? = null) =
        plainQuery(path, parameters).bodyAsText()

    /**
     * A query with autologin handling.
     *
     * @throws AuthenticationException If the request fails because of authentication. (even after autologin)
     */
    override suspend fun query(path: String, parameters: Parameters?): HttpResponse
    {
        val response = plainQuery(path, parameters)

        /* No redirect to login. */
        val locationHeader = response.headers[HttpHeaders.Location] ?: return response.also { autoLoginAttempted = false }

        if (!locationHeader.startsWith("$ENDPOINT/user/need-login"))
            return response.also { autoLoginAttempted = false }

        /* Redirect to login. */

        if (!autoLogin || autoLoginAuth == null)
            throw AuthenticationException()

        if (autoLoginAttempted)
        {
            autoLoginAttempted = false
            throw AuthenticationException()
        }

        login(autoLoginAuth!!)

        autoLoginAttempted = true
        return query(path, parameters)
    }

    private suspend fun getRootStudentPageHtml(): String
    {
        setRole(Role.STUDENT)
        return plainQueryStringBody("/")
    }

    private fun findToken3(document: Document): String?
    {
        val token3Ele = document.selectFirst("input[name=token3]") ?: return null
        return token3Ele.attr("value")
    }

    private fun findToken3(htmlDocument: String) = findToken3(Jsoup.parse(htmlDocument))

    /**
     * Closes the HTTP client.
     */
    fun close() = httpClient.close()

    companion object
    {
        const val ENDPOINT = "https://www.spsejecna.cz"

        const val SESSION_ID_COOKIE_NAME = "JSESSIONID"

        /**
         * Endpoint used for testing whether user is logged in or not.
         * Using particularly this one, because it's the smallest => fastest to download.
         */
        const val LOGIN_TEST_ENDPOINT = "/user-student/record-list"

        /**
         * Returns the full URL for the given path.
         * @param path The path to query. Must include first slash.
         */
        fun getUrlForPath(path: String) = ENDPOINT + path

        /* Refer to Role section in /internal_docs/Jecna_server.md */
        val ROLE_TO_WTDGUID_COOKIE_VALUE_MAP = mapOf(
            Role.INTERESTED to "0",
            Role.STUDENT to "10",
            Role.EMPLOYEE to "100"
        )
    }
}
