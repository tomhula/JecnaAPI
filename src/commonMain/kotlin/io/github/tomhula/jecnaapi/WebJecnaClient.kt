package io.github.tomhula.jecnaapi

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.statement.*
import io.ktor.http.*
import io.github.tomhula.jecnaapi.data.notification.NotificationReference
import io.github.tomhula.jecnaapi.data.timetable.TimetablePage
import io.github.tomhula.jecnaapi.parser.parsers.*
import io.github.tomhula.jecnaapi.util.JecnaPeriodEncoder
import io.github.tomhula.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.SchoolYearHalf
import io.github.tomhula.jecnaapi.web.Auth
import io.github.tomhula.jecnaapi.web.AuthenticationException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.addCookie
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import kotlinx.datetime.Month
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A client to access Jecna Web data.
 *
 * @param autoLogin Saves provided [Auth] on each [login] call.
 * Then when calling [query] and it fails because of [AuthenticationException], [login] is called with the saved [Auth] and the request retried.
 */
class WebJecnaClient(
    var autoLogin: Boolean = false,
    val userAgent: String? = "JAPI",
    requestTimeout: Duration = 10.seconds
) : JecnaClient
{
    private val cookieStorage = AcceptAllCookiesStorage()
    private val httpClient = HttpClient {
        install(HttpCookies) { storage = cookieStorage }
        defaultRequest {
            url(ENDPOINT)
            if (userAgent != null)
                userAgent(userAgent)
            else
                headers.remove(HttpHeaders.UserAgent)
        }
        install(HttpTimeout) { requestTimeoutMillis = requestTimeout.inWholeMilliseconds }
        followRedirects = false
    }
    private var autoLoginAttempted = false

    /**
     * [Auth] used by [autoLogin]. Is automatically updated by [login] on a successful login.
     * Is set to `null` on [logout].
     */
    var autoLoginAuth: Auth? = null
    
    @OptIn(ExperimentalTime::class)
    var lastSuccessfulLoginTime: Instant? = null
        private set
    
    /** Value may be incorrect if the session has expired */
    var role: Role? = null
        private set

    private val newsPageParser = NewsPageParser
    private val gradesPageParser = GradesPageParser
    private val timetablePageParser = TimetablePageParser(TimetableParser)
    private val attendancesPageParser = AttendancesPageParser
    private val absencesPageParser = AbsencesPageParser
    private val teachersPageParser = TeachersPageParser
    private val teacherParser = TeacherParser(TimetableParser)
    private val notificationParser = NotificationParser
    private val studentProfileParser = StudentProfileParser
    private val lockerPageParser = LockerPageParser
    private val roomsPageParser = RoomsPageParser
    private val roomParser = RoomParser(TimetableParser)

    @OptIn(ExperimentalTime::class)
    override suspend fun login(auth: Auth): Boolean
    {
        val rootStudentPageDocument = Ksoup.parse(getRootStudentPageHtml())

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
        lastSuccessfulLoginTime = Clock.System.now()

        return true
    }

    override suspend fun logout()
    {
        autoLoginAuth = null
        plainQuery("/user/logout")
    }

    /* Responds with status 302 (redirect to login page) when user is not logged in. */
    override suspend fun isLoggedIn() = plainQuery(LOGIN_TEST_ENDPOINT).status == HttpStatusCode.OK

    override suspend fun getNewsPage() = newsPageParser.parse(queryStringBody(PageWebPath.NEWS))
    override suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) =
        gradesPageParser.parse(queryStringBody(PageWebPath.GRADES, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(schoolYearHalf.jecnaEncode())
        }))
    override suspend fun getGradesPage() = gradesPageParser.parse(queryStringBody(PageWebPath.GRADES))
    override suspend fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption?) =
        timetablePageParser.parse(queryStringBody(PageWebPath.TIMETABLE, Parameters.build {
            append(schoolYear.jecnaEncode())
            periodOption?.let { append(it.jecnaEncode()) }
        }))
    override suspend fun getTimetablePage() = timetablePageParser.parse(queryStringBody(PageWebPath.TIMETABLE))
    override suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month) =
        attendancesPageParser.parse(queryStringBody(PageWebPath.ATTENDANCES, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(JecnaPeriodEncoder.encodeMonth(month))
        }))
    override suspend fun getAttendancesPage() = attendancesPageParser.parse(queryStringBody(PageWebPath.ATTENDANCES))
    override suspend fun getAbsencesPage(schoolYear: SchoolYear) =
        absencesPageParser.parse(queryStringBody(PageWebPath.ABSENCES, Parameters.build {
            append(schoolYear.jecnaEncode())
        }))
    override suspend fun getAbsencesPage() = absencesPageParser.parse(queryStringBody(PageWebPath.ABSENCES))
    override suspend fun getTeachersPage() = teachersPageParser.parse(queryStringBody(PageWebPath.TEACHERS))
    override suspend fun getTeacher(teacherTag: String) = teacherParser.parse(queryStringBody("${PageWebPath.TEACHERS}/$teacherTag"))
    override suspend fun getRoomsPage() = roomsPageParser.parse(queryStringBody(PageWebPath.ROOMS))
    override suspend fun getRoom(roomCode: String) = roomParser.parse(queryStringBody("${PageWebPath.ROOMS}/${roomCode}"))
    override suspend fun getLocker() = lockerPageParser.parse(queryStringBody(PageWebPath.LOCKER))
    override suspend fun getStudentProfile(username: String) = studentProfileParser.parse(queryStringBody("${PageWebPath.STUDENT}/$username"))
    override suspend fun getStudentProfile() = autoLoginAuth?.let { getStudentProfile(it.username)} ?: throw AuthenticationException()
    override suspend fun getNotification(notification: NotificationReference) = notificationParser.getNotification(queryStringBody("${PageWebPath.NOTIFICATION}?userStudentRecordId=${notification.recordId}"))
    override suspend fun getNotifications() = notificationParser.parse(queryStringBody(PageWebPath.NOTIFICATIONS))

    suspend fun setRole(role: Role)
    {
        /* Refer to Role section in /internal_docs/Jecna_server.md */
        setCookie("WTDGUID", ROLE_TO_WTDGUID_COOKIE_VALUE_MAP[role]!!)
        //plainQuery("/user/role", parametersOf("role", role.value))
        this.role = role
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

    suspend fun getCookieValue(name: String) = getCookie(name)?.value

    suspend fun setCookie(name: String, value: String) = cookieStorage.addCookie(ENDPOINT, Cookie(name, value))

    suspend fun getCookie(name: String) = cookieStorage.get(Url(ENDPOINT)).firstOrNull { it.name == name }

    suspend fun getSessionCookie() = getCookie(SESSION_ID_COOKIE_NAME)
    
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
    suspend fun query(path: String, parameters: Parameters?): HttpResponse
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

    suspend fun queryStringBody(path: String, parameters: Parameters? = null) = query(path, parameters).bodyAsText()

    /** Closes the HTTP client. */
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

        /* Refer to the Role section in /internal_docs/Jecna_server.md */
        val ROLE_TO_WTDGUID_COOKIE_VALUE_MAP = mapOf(
            Role.INTERESTED to "0",
            Role.STUDENT to "10",
            Role.EMPLOYEE to "100"
        )
        
        private object PageWebPath
        {
            const val NEWS = "/akce"
            const val GRADES = "/score/student"
            const val TIMETABLE = "/timetable/class"
            const val ATTENDANCES = "/absence/passing-student"
            const val TEACHERS = "/ucitel"
            const val ABSENCES = "/absence/student"
            const val NOTIFICATION = "/user-student/record"
            const val NOTIFICATIONS = "/user-student/record-list"
            const val STUDENT = "/student"
            const val LOCKER = "/locker/student"
            const val ROOMS = "/ucebna"
        }
    }

    enum class Role(val value: String)
    {
        INTERESTED("zajemce"),
        STUDENT("student"),
        EMPLOYEE("zamestnanec"),
    }
}

/**
 * [append] extension function on [ParametersBuilder], that takes [Pair] as a parameter.
 */
private fun ParametersBuilder.append(pair: Pair<String, Any>) = append(pair.first, pair.second.toString())
