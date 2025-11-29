package me.tomasan7.jecnaapi

import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.data.schoolStaff.TeacherReference
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.parser.parsers.*
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.AuthenticationException
import me.tomasan7.jecnaapi.web.append
import me.tomasan7.jecnaapi.web.jecna.JecnaWebClient
import me.tomasan7.jecnaapi.web.jecna.Role
import java.time.Month
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A client to access Jecna Web data.
 *
 * @param autoLogin Saves provided [Auth] on each [login] call.
 * Then when calling [query] and it fails because of [AuthenticationException], [login] is called with the saved [Auth] and the request retried.
 */
class JecnaClient(
    autoLogin: Boolean = false,
    requestTimout: Duration = 10.seconds,
    userAgent: String? = "JAPI"
)
{
    private val webClient = JecnaWebClient(requestTimout, autoLogin, userAgent)

    var autoLogin by webClient::autoLogin
    val userAgent by webClient::userAgent
    /** The last [time][java.time.Instant] a call to [login] was successful (returned `true`). */
    val lastSuccessfulLoginTime by webClient::lastSuccessfulLoginTime
    /**
     * [Auth] used by [autoLogin]. Is automatically updated by [login] on a successful login.
     * Is set to `null` on [logout].
     */
    var autoLoginAuth by webClient::autoLoginAuth
    val role by webClient::role

    private val newsPageParser: HtmlNewsPageParser = HtmlNewsPageParserImpl
    private val gradesPageParser: HtmlGradesPageParser = HtmlGradesPageParserImpl
    private val timetablePageParser: HtmlTimetablePageParser = HtmlTimetablePageParserImpl(HtmlTimetableParserImpl)
    private val attendancesPageParser: HtmlAttendancesPageParser = HtmlAttendancesPageParserImpl
    private val teachersPageParser: HtmlTeachersPageParser = HtmlTeachersPageParserImpl
    private val teacherParser: HtmlTeacherParser = HtmlTeacherParserImpl(HtmlTimetableParserImpl)

    suspend fun login(username: String, password: String) = login(Auth(username, password))

    suspend fun login(auth: Auth) = webClient.login(auth)

    suspend fun logout() = webClient.logout()

    suspend fun isLoggedIn() = webClient.isLoggedIn()

    suspend fun getCookieValue(name: String) = webClient.getCookieValue(name)

    suspend fun getCookie(name: String) = webClient.getCookie(name)

    suspend fun getSessionCookie() = webClient.getSessionCookie()

    suspend fun setCookie(name: String, value: String) = webClient.setCookie(name, value)

    suspend fun setRole(role: Role) = webClient.setRole(role)

    suspend fun getNewsPage() = newsPageParser.parse(queryStringBody(PageWebPath.news))

    suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) =
        gradesPageParser.parse(queryStringBody(PageWebPath.grades, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(schoolYearHalf.jecnaEncode())
        }))

    suspend fun getGradesPage() = gradesPageParser.parse(queryStringBody(PageWebPath.grades))

    suspend fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null) =
        timetablePageParser.parse(queryStringBody(PageWebPath.timetable, Parameters.build {
            append(schoolYear.jecnaEncode())
            periodOption?.let { append(it.jecnaEncode()) }
        }))

    suspend fun getTimetablePage() = timetablePageParser.parse(queryStringBody(PageWebPath.timetable))

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month) = getAttendancesPage(schoolYear, month.value)

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Int) =
        attendancesPageParser.parse(queryStringBody(PageWebPath.attendances, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(JecnaPeriodEncoder.encodeMonth(month))
        }))

    suspend fun getAttendancesPage() = attendancesPageParser.parse(queryStringBody(PageWebPath.attendances))

    suspend fun getTeachersPage() = teachersPageParser.parse(queryStringBody(PageWebPath.teachers))

    suspend fun getTeacher(teacherTag: String) = teacherParser.parse(queryStringBody("${PageWebPath.teachers}/$teacherTag"))

    suspend fun getTeacher(teacherReference: TeacherReference) = teacherParser.parse(queryStringBody("${PageWebPath.teachers}/${teacherReference.tag}"))

    /** A query without any authentication (autologin) handling. */
    suspend fun plainQuery(path: String, parameters: Parameters? = null) = webClient.plainQuery(path, parameters)

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @throws AuthenticationException When the query fails because user is not authenticated.
     * @return The [HttpResponse].
     */
    suspend fun query(path: String, parameters: Parameters? = null) = webClient.query(path, parameters)

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @throws AuthenticationException When the query fails because user is not authenticated.
     * @return The [HttpResponse].
     */
    suspend fun queryStringBody(path: String, parameters: Parameters? = null) = webClient.queryStringBody(path, parameters)

    /** Closes the HTTP client. */
    fun close() = webClient.close()

    companion object
    {
        private object PageWebPath
        {
            const val news = "/"
            const val grades = "/score/student"
            const val timetable = "/timetable/class"
            const val attendances = "/absence/passing-student"
            const val teachers = "/ucitel"
        }
    }
}
