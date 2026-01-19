package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.parser.HtmlElementNotFoundException
import io.github.tomhula.jecnaapi.util.SchoolYear
import io.github.tomhula.jecnaapi.util.toSchoolYear
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*

/**
 * Functions used by multiple parsers.
 */
object HtmlCommonParser
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
}

fun Element.selectFirstOrThrow(selector: String) =
    selectFirst(selector) ?: throw HtmlElementNotFoundException.bySelector(this.cssSelector(), selector)

fun Element.selectFirstOrThrow(selector: String, selectedElementName: String) =
    selectFirst(selector) ?: throw HtmlElementNotFoundException.byName(selectedElementName)

fun Element?.expectElement(name: String) = this ?: throw HtmlElementNotFoundException.byName(name)
