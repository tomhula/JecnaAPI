package io.github.tomhula.jecnaapi

import io.github.tomhula.jecnaapi.data.document.DocumentsPage
import io.github.tomhula.jecnaapi.data.absence.AbsencesPage
import io.github.tomhula.jecnaapi.data.article.NewsPage
import io.github.tomhula.jecnaapi.data.attendance.AttendancesPage
import io.github.tomhula.jecnaapi.data.cert.Certificate
import io.github.tomhula.jecnaapi.data.grade.GradesPage
import io.github.tomhula.jecnaapi.data.notification.Notification
import io.github.tomhula.jecnaapi.data.notification.NotificationReference
import io.github.tomhula.jecnaapi.data.room.Room
import io.github.tomhula.jecnaapi.data.room.RoomReference
import io.github.tomhula.jecnaapi.data.room.RoomsPage
import io.github.tomhula.jecnaapi.data.schoolStaff.Teacher
import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import io.github.tomhula.jecnaapi.data.schoolStaff.TeachersPage
import io.github.tomhula.jecnaapi.data.student.Locker
import io.github.tomhula.jecnaapi.data.student.Student
import io.github.tomhula.jecnaapi.data.timetable.TimetablePage
import io.github.tomhula.jecnaapi.parser.ParseException
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.SchoolYearHalf
import io.github.tomhula.jecnaapi.web.Auth
import kotlinx.datetime.Month
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface JecnaClient
{
    @Throws(IllegalStateException::class, RuntimeException::class)
    suspend fun login(auth: Auth): Boolean
    @Throws(IllegalStateException::class, RuntimeException::class)
    suspend fun login(username: String, password: String): Boolean = login(Auth(username, password))
    @Throws(RuntimeException::class)
    suspend fun logout()
    @Throws(RuntimeException::class)
    suspend fun isLoggedIn(): Boolean
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getNewsPage(): NewsPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf): GradesPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getGradesPage(): GradesPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getTimetablePage(schoolYear: SchoolYear, periodId: Int?): TimetablePage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null) = getTimetablePage(schoolYear, periodOption?.id)
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getTimetablePage(): TimetablePage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month): AttendancesPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getAttendancesPage(): AttendancesPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getAbsencesPage(schoolYear: SchoolYear): AbsencesPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getAbsencesPage(): AbsencesPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getTeachersPage(): TeachersPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getTeacher(teacherTag: String): Teacher
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getTeacher(teacherReference: TeacherReference): Teacher = getTeacher(teacherReference.tag)
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getRoomsPage(): RoomsPage
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getRoom(roomCode: String): Room
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getRoom(roomReference: RoomReference): Room = getRoom(roomReference.roomCode)
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getLocker(): Locker?
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getStudentProfile(): Student
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getStudentProfile(username: String): Student
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getNotifications(): List<NotificationReference>
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getNotification(notification: NotificationReference): Notification

    /** @return the list of student's certificates or `null` if the student cannot have any certificates (i.e. he is not in the 4th grade) */
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getStudentCertificates(): List<Certificate>?
    @Throws(ParseException::class, RuntimeException::class)
    suspend fun getDocumentsPage(path: String = "/dokumenty"): DocumentsPage
    
    companion object
    {
        operator fun invoke(
            endpoint: String = WebJecnaClient.OFFICIAL_ENDPOINT,
            autoLogin: Boolean = false,
            userAgent: String? = "JAPI",
            requestTimeout: Duration = 10.seconds
        ) : JecnaClient = WebJecnaClient(endpoint, autoLogin, userAgent, requestTimeout)
    }
}
