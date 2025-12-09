import io.github.tomhula.jecnaapi.data.attendance.AttendancesPage;
import io.github.tomhula.jecnaapi.java.JecnaClientJavaWrapper;

import java.time.LocalDate;
import java.util.Set;


public class AttendanceExample
{
    public static void main(String[] args)
    {
        /* Metody join() používáme aby jsme z asynchroního běhu "udělali" synchroní; pouze v příkladech */

        JecnaClientJavaWrapper client = new JecnaClientJavaWrapper();

        client.login("user", "password").join();

        /* Lze specifikovat období v argumentu */
        AttendancesPage attendancesPage = client.getAttendancePage().join();

        /* Všechny dny, pro které existují data */
        Set<LocalDate> attendances = attendancesPage.getDays();

        /* Všechny příchody a odchody (v daném půlroce) */
        for (LocalDate date : attendances)
            System.out.println(attendancesPage.get(date));
    }
}
