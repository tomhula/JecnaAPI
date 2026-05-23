package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.article.Article
import io.github.tomhula.jecnaapi.data.article.ArticleFile
import io.github.tomhula.jecnaapi.data.article.NewsPage
import io.github.tomhula.jecnaapi.parser.ParseException
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** https://www.spsejecna.cz/akce */
internal object NewsPageParser
{
    fun parse(html: String): NewsPage
    {
        try
        {
            val newsPageBuilder = NewsPage.builder()

            val document = Ksoup.parse(html)

            val articleEles = document.select(".event")

            for (articleEle in articleEles)
                newsPageBuilder.addArticle(parseArticle(articleEle))

            return newsPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse news page.", e)
        }
    }

    private fun parseArticle(articleEle: Element): Article
    {
        val title = articleEle.selectFirstOrThrow(".name").text()
        val content = articleEle.selectFirstOrThrow(".text").text()
        val htmlContent = articleEle.selectFirstOrThrow(".text").html()
        val dateEle = articleEle.selectFirst(".date")

        val articleFiles = parseArticleFiles(articleEle)
        val images = parseImages(articleEle)

        val footer = articleEle.selectFirstOrThrow(".footer").text()
        val footerSplit = footer.split(" | ")

        /* The date either has its own element, or is embedded in footer. */

        if (dateEle == null)
        {
            val dateStr = footerSplit[0]
            val author = footerSplit[1]
            val schoolOnly = footerSplit.size == 3
            val date = parseDate(dateStr)

            return Article(title, content, htmlContent, date, author, schoolOnly, articleFiles, images)
        }
        else
        {
            val date = parseDate(dateEle.text())

            val author = footerSplit[0]
            val schoolOnly = footerSplit.size == 2

            return Article(title, content, htmlContent, date, author, schoolOnly, articleFiles, images)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun parseDate(strWithDate: String): LocalDate
    {
        val match = DATE_REGEX.find(strWithDate) ?: throw ParseException("Failed to parse date from string: $strWithDate")
        val day = match.groups[1]!!.value.toInt()
        val monthName = match.groups[2]!!.value
        val month = CZECH_MONTH_NAME_MAP[monthName] ?: throw ParseException("Unknown month name: $monthName")
        val year = Clock.System.now().toLocalDateTime(TimeZone.UTC).year
        
        return LocalDate(year, month, day)
    }

    private fun parseArticleFiles(articleEle: Element): List<ArticleFile>
    {
        val articleFileEles = articleEle.select(".files li a")

        return articleFileEles.map { parseArticleFile(it) }
    }

    private fun parseArticleFile(articleFileEle: Element): ArticleFile
    {
        val label = articleFileEle.selectFirstOrThrow(".label").text()
        val downloadPath = articleFileEle.attr("href")

        return ArticleFile(label, downloadPath)
    }

    private fun parseImages(articleEle: Element): List<String>
    {
        val imageEles = articleEle.select(".images").flatMap { it.select("a") }

        return imageEles.map { it.attr("href") }
    }

    private val CZECH_MONTH_NAME_MAP = mapOf(
        "ledna" to Month.JANUARY,
        "února" to Month.FEBRUARY,
        "března" to Month.MARCH,
        "dubna" to Month.APRIL,
        "května" to Month.MAY,
        "června" to Month.JUNE,
        "července" to Month.JULY,
        "srpna" to Month.AUGUST,
        "září" to Month.SEPTEMBER,
        "října" to Month.OCTOBER,
        "listopadu" to Month.NOVEMBER,
        "prosince" to Month.DECEMBER
    )

    private val DATE_REGEX = Regex("""(\d{1,2})\.(${CZECH_MONTH_NAME_MAP.keys.joinToString("|")})""")
}
