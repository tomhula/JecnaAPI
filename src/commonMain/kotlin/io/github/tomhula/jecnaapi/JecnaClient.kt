package io.github.tomhula.jecnaapi

import io.github.tomhula.jecnaapi.data.room.RoomReference
import io.ktor.client.statement.*
import io.ktor.http.*
import io.github.tomhula.jecnaapi.data.notification.NotificationReference
import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import io.github.tomhula.jecnaapi.data.timetable.TimetablePage
import io.github.tomhula.jecnaapi.parser.parsers.*
import io.github.tomhula.jecnaapi.util.JecnaPeriodEncoder
import io.github.tomhula.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.SchoolYearHalf
import io.github.tomhula.jecnaapi.web.Auth
import io.github.tomhula.jecnaapi.web.AuthenticationException
import io.github.tomhula.jecnaapi.web.append
import io.github.tomhula.jecnaapi.web.jecna.JecnaWebClient
import io.github.tomhula.jecnaapi.web.jecna.Role
import kotlinx.datetime.Month
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

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
    /** The last [time][kotlin.time.Instant] a call to [login] was successful (returned `true`). */
    @OptIn(ExperimentalTime::class)
    val lastSuccessfulLoginTime by webClient::lastSuccessfulLoginTime
    /**
     * [Auth] used by [autoLogin]. Is automatically updated by [login] on a successful login.
     * Is set to `null` on [logout].
     */
    var autoLoginAuth by webClient::autoLoginAuth
    val role by webClient::role

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

    suspend fun login(username: String, password: String) = login(Auth(username, password))

    suspend fun login(auth: Auth) = webClient.login(auth)

    suspend fun logout() = webClient.logout()

    suspend fun isLoggedIn() = webClient.isLoggedIn()

    suspend fun getCookieValue(name: String) = webClient.getCookieValue(name)

    suspend fun getCookie(name: String) = webClient.getCookie(name)

    suspend fun getSessionCookie() = webClient.getSessionCookie()

    suspend fun setCookie(name: String, value: String) = webClient.setCookie(name, value)

    suspend fun setRole(role: Role) = webClient.setRole(role)

    suspend fun getNewsPage() = newsPageParser.parse(queryStringBody(PageWebPath.NEWS))

    suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) =
        gradesPageParser.parse(queryStringBody(PageWebPath.GRADES, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(schoolYearHalf.jecnaEncode())
        }))

    suspend fun getGradesPage() = gradesPageParser.parse(queryStringBody(PageWebPath.GRADES))

    suspend fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null) =
        timetablePageParser.parse(queryStringBody(PageWebPath.TIMETABLE, Parameters.build {
            append(schoolYear.jecnaEncode())
            periodOption?.let { append(it.jecnaEncode()) }
        }))

    suspend fun getTimetablePage() = timetablePageParser.parse(queryStringBody(PageWebPath.TIMETABLE))

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month) =
        attendancesPageParser.parse(queryStringBody(PageWebPath.ATTENDANCES, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(JecnaPeriodEncoder.encodeMonth(month))
        }))

    suspend fun getAttendancesPage() = attendancesPageParser.parse(queryStringBody(PageWebPath.ATTENDANCES))

    suspend fun getAbsencesPage(schoolYear: SchoolYear) =
        absencesPageParser.parse(queryStringBody(PageWebPath.ABSENCES, Parameters.build {
            append(schoolYear.jecnaEncode())
        }))

    suspend fun getAbsencesPage() = absencesPageParser.parse(queryStringBody(PageWebPath.ABSENCES))

    suspend fun getTeachersPage() = teachersPageParser.parse(queryStringBody(PageWebPath.TEACHERS))

    suspend fun getTeacher(teacherTag: String) = teacherParser.parse(queryStringBody("${PageWebPath.TEACHERS}/$teacherTag"))

    suspend fun getTeacher(teacherReference: TeacherReference) = getTeacher(teacherReference.tag)
    
    suspend fun getRoomsPage() = roomsPageParser.parse(queryStringBody(PageWebPath.ROOMS))
    
    suspend fun getRoom(roomCode: String) = roomParser.parse(queryStringBody("${PageWebPath.ROOMS}/${roomCode}"))
    
    suspend fun getRoom(roomReference: RoomReference) = getRoom(roomReference.roomCode)
    
    suspend fun getLocker() = lockerPageParser.parse(queryStringBody(PageWebPath.LOCKER))

    suspend fun getStudentProfile(username: String) = studentProfileParser.parse(queryStringBody("${PageWebPath.STUDENT}/$username"))

    suspend fun getStudentProfile() = autoLoginAuth?.let { getStudentProfile(it.username)}
        ?: throw AuthenticationException()

    suspend fun getNotification(notification: NotificationReference) = notificationParser.getNotification(queryStringBody("${PageWebPath.NOTIFICATION}?userStudentRecordId=${notification.recordId}"))

    suspend fun getNotifications() = notificationParser.parse(queryStringBody(PageWebPath.NOTIFICATIONS))

    /** A query without any authentication (autologin) handling. */
    suspend fun plainQuery(path: String, parameters: Parameters? = null) = webClient.plainQuery(path, parameters)

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
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
}
