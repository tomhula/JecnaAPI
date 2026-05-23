import io.github.tomhula.jecnaapi.data.timetable.LessonSpot;
import io.github.tomhula.jecnaapi.data.timetable.Timetable;
import io.github.tomhula.jecnaapi.data.timetable.TimetablePage;
import io.github.tomhula.jecnaapi.java.JecnaClientJavaWrapper;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class TimetableExample
{
    public static void main(String[] args)
    {
        /* Metody join() používáme aby jsme z asynchroního běhu "udělali" synchroní; pouze v příkladech */

        JecnaClientJavaWrapper jecnaClient = new JecnaClientJavaWrapper();

        jecnaClient.login("user", "password").join();

        TimetablePage timetablePage = jecnaClient.getTimetablePage().join();
        Timetable timetable = timetablePage.getTimetable();

        /* Vypíše všechny hodiny v týdnu */
        for (DayOfWeek day : timetable.getDays())
        {
            List<LessonSpot> lessonSpots = timetable.get(day);
            if (lessonSpots == null)
                continue;

            System.out.print(day + ": ");

            for (LessonSpot lessonSpot : lessonSpots)
                if (lessonSpot.getSize() != 0)
                    System.out.print(lessonSpot.get(0).getSubjectName().getFull() + ", ");

            System.out.println();
        }

        System.out.println();

        /* Vrátí konkrétní hodinu daného dne, v určeném čase */
        LessonSpot lessonAtSpecificTime = timetable.getLessonSpot(DayOfWeek.TUESDAY, LocalTime.of(10, 45));
        System.out.println("Hodina v úterá v '10:45': " + lessonAtSpecificTime);
    }
}
