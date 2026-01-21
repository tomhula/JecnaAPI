package io.github.tomhula.jecnaapi
import io.github.tomhula.jecnaapi.data.absence.AbsencesPage
import io.github.tomhula.jecnaapi.data.article.NewsPage
import io.github.tomhula.jecnaapi.data.attendance.AttendancesPage
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
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.SchoolYearHalf
import io.github.tomhula.jecnaapi.web.Auth
import kotlinx.datetime.Month
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface JecnaClient
{
    suspend fun login(auth: Auth): Boolean
    suspend fun login(username: String, password: String): Boolean = login(Auth(username, password))
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
    suspend fun getNewsPage(): NewsPage
    suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf): GradesPage
    suspend fun getGradesPage(): GradesPage
    suspend fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null): TimetablePage
    suspend fun getTimetablePage(): TimetablePage
    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month): AttendancesPage
    suspend fun getAttendancesPage(): AttendancesPage
    suspend fun getAbsencesPage(schoolYear: SchoolYear): AbsencesPage
    suspend fun getAbsencesPage(): AbsencesPage
    suspend fun getTeachersPage(): TeachersPage
    suspend fun getTeacher(teacherTag: String): Teacher
    suspend fun getTeacher(teacherReference: TeacherReference): Teacher = getTeacher(teacherReference.tag)
    suspend fun getRoomsPage(): RoomsPage
    suspend fun getRoom(roomCode: String): Room
    suspend fun getRoom(roomReference: RoomReference): Room = getRoom(roomReference.roomCode)
    suspend fun getLocker(): Locker?
    suspend fun getStudentProfile(): Student
    suspend fun getStudentProfile(username: String): Student
    suspend fun getNotifications(): List<NotificationReference>
    suspend fun getNotification(notification: NotificationReference): Notification
    
    companion object
    {
        operator fun invoke(
            autoLogin: Boolean = false,
            userAgent: String? = "JAPI",
            requestTimeout: Duration = 10.seconds
        ) : JecnaClient = WebJecnaClient(autoLogin, userAgent, requestTimeout)
    }
}
