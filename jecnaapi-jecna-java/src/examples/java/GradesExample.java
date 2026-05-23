import io.github.tomhula.jecnaapi.data.grade.Grade;
import io.github.tomhula.jecnaapi.data.grade.GradesPage;
import io.github.tomhula.jecnaapi.data.grade.Subject;
import io.github.tomhula.jecnaapi.data.grade.WeightedGradeAverageCalculator;
import io.github.tomhula.jecnaapi.java.JecnaClientJavaWrapper;
import io.github.tomhula.jecnaapi.util.SchoolYear;
import io.github.tomhula.jecnaapi.util.SchoolYearHalf;
import io.github.tomhula.jecnaapi.web.Auth;

import java.util.List;

public class GradesExample
{
    public static void main(String[] args)
    {
        /* Metody join() používáme aby jsme z asynchroního běhu "udělali" synchroní; pouze v příkladech */

        JecnaClientJavaWrapper jecnaClient = new JecnaClientJavaWrapper();

        jecnaClient.login("user", "password").join();

        /* Stáhne známky z roku 2021/2022 z druhého pololetí. */
        GradesPage gradesPage = jecnaClient.getGradesPage(new SchoolYear(2021), SchoolYearHalf.SECOND).join();

        Subject mathSubject = gradesPage.get("Matematika");

        /* Průmer známek */
        float mathAverage = mathSubject.getGrades().average();

        System.out.println("Průměr z matematiky: " + mathAverage);

        /* Do .get() patří string, který určuje část předmětu. (např. Teorie, Cvičení)
         * Pokud předmět není rozdělen, použijte null. */
        List<Grade> mathGrades = mathSubject.getGrades().get(null);

        System.out.println("Známky z matematiky: ");

        for (Grade grade : mathGrades)
            System.out.println(grade);
    }
}
