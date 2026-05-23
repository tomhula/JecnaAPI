package io.github.tomhula.jecnaapi.parser.parsers

import com.fleeksoft.ksoup.nodes.Document
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.toSchoolYear

internal object JecnaCommonParser
{
    fun parseSelectedSchoolYear(document: Document): SchoolYear
    {
        val selectedSchoolYearEle = document.selectFirstOrThrow("#schoolYearId > option[selected]", "selected school year")
        return selectedSchoolYearEle.text().toSchoolYear()
    }
}
