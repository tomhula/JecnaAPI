import kotlinx.coroutines.runBlocking
import io.github.tomhula.jecnaapi.JecnaClient
import java.time.DayOfWeek
import java.time.LocalTime

fun main(): Unit = runBlocking {

    val client = JecnaClient()

    client.login("user", "password")

    val timetablePage = client.getTimetablePage()
    val timetable = timetablePage.timetable

    /* Vypíše všechny hodiny v týdnu */
    for (day in timetable.days)
    {
        val subjects = timetable[day]?.joinToString { it.firstOrNull()?.subjectName?.full ?: "--" }
        println("$day: $subjects")
    }

    println()

    /* Vrátí konkrétní hodinu daného dne, v určeném čase */
    val lessonAtSpecificTime = timetable.getLessonSpot(DayOfWeek.TUESDAY, LocalTime.of(10, 45))
    println("Hodina v úterá v '10:45': $lessonAtSpecificTime")
}
