package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import io.github.tomhula.jecnaapi.data.schoolStaff.TeachersPage
import io.github.tomhula.jecnaapi.parser.ParseException
import com.fleeksoft.ksoup.Ksoup

/** https://www.spsejecna.cz/ucitel */
internal object TeachersPageParser
{
    fun parse(html: String): TeachersPage
    {
        try
        {
            val document = Ksoup.parse(html)

            val teachersPageBuilder = TeachersPage.builder()

            val teacherEles = document.select(".contentLeftColumn > ul a, .contentRightColumn > ul a")

            for (teacherEle in teacherEles)
            {
                val fullName = teacherEle.text()
                val tag = teacherEle.attr("href").let { it.substring(URL_PATH_START_REMOVE_LENGTH until it.length) }

                teachersPageBuilder.addTeacherReference(TeacherReference(fullName, tag))
            }

            return teachersPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse teachers page.", e)
        }
    }

    private const val URL_PATH_START_REMOVE_LENGTH = "/ucitel/".length
}
