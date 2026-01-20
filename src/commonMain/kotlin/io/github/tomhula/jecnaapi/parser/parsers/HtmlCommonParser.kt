package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.parser.HtmlElementNotFoundException
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.toSchoolYear
import kotlinx.datetime.Month
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/**
 * Functions used by multiple parsers.
 */
internal object HtmlCommonParser
{
    fun parseSelectedSchoolYear(document: Document): SchoolYear
    {
        val selectedSchoolYearEle = document.selectFirstOrThrow("#schoolYearId > option[selected]", "selected school year")
        return selectedSchoolYearEle.text().toSchoolYear()
    }

    fun parseSelectedMonth(document: Document): Month
    {
        val selectedMonthEle = document.selectFirstOrThrow("#schoolYearPartMonthId > option[selected]", "selected month")
        return parseMonthByName(selectedMonthEle.text())
    }

    fun getSelectSelectedValue(document: Document, selectId: String) = document.selectFirst("#$selectId > option[selected]")

    private fun parseMonthByName(monthName: String) = when(monthName)
    {
        "leden" -> Month.JANUARY
        "únor" -> Month.FEBRUARY
        "březen" -> Month.MARCH
        "duben" -> Month.APRIL
        "květen" -> Month.MAY
        "červen" -> Month.JUNE
        "červenec" -> Month.JULY
        "srpen" -> Month.AUGUST
        "září" -> Month.SEPTEMBER
        "říjen" -> Month.OCTOBER
        "listopad" -> Month.NOVEMBER
        "prosinec" -> Month.DECEMBER
        else -> throw IllegalArgumentException("Unknown month name: $monthName")
    }
    
    val CZECH_DATE_FORMAT_WITH_PADDING = LocalDate.Format {
        day(padding = Padding.ZERO)
        char('.')
        monthNumber(padding = Padding.ZERO)
        char('.')
        year(padding = Padding.ZERO)
    }
    
    val CZECH_DATE_FORMAT_WITHOUT_PADDING = LocalDate.Format {
        day(padding = Padding.NONE)
        char('.')
        monthNumber(padding = Padding.NONE)
        char('.')
        year(padding = Padding.NONE)
    }
    
    val CZECH_DATE_REGEX = Regex("""\d{1,2}\.\d{1,2}\.\d{4}""")
}

internal fun Element.selectFirstOrThrow(selector: String) =
    selectFirst(selector) ?: throw HtmlElementNotFoundException.bySelector(this.cssSelector(), selector)

internal fun Element.selectFirstOrThrow(selector: String, selectedElementName: String) =
    selectFirst(selector) ?: throw HtmlElementNotFoundException.byName(selectedElementName)

internal fun Element?.expectElement(name: String) = this ?: throw HtmlElementNotFoundException.byName(name)
