import kotlinx.coroutines.runBlocking
import io.github.tomhula.jecnaapi.JecnaClient

fun main(): Unit = runBlocking {

    val client = JecnaClient()

    client.login("user", "password")

    /* Lze specifikovat období v argumentu */
    val attendancesPage = client.getAttendancesPage()

    /* Všechny dny, pro které existují data */
    val attendedDays = attendancesPage.days

    /* Všechny příchody a odchody (v daném půlroce) */
    for (day in attendedDays)
        println(attendancesPage[day])
}
