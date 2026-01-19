package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.article.Article
import io.github.tomhula.jecnaapi.data.article.ArticleFile
import io.github.tomhula.jecnaapi.data.article.NewsPage
import io.github.tomhula.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

/**
 * Parses correct HTML to [NewsPage] instance.
 */
internal object HtmlNewsPageParserImpl : HtmlNewsPageParser
{
    override fun parse(html: String): NewsPage
    {
        try
        {
            val newsPageBuilder = NewsPage.builder()

            val document = Jsoup.parse(html)

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

    /**
     * Finds and parses a date in [strWithDate].
     * Used to be more flexible, so if there are more things in the [strWithDate], it won't affect the date parsing.
     */
    private fun parseDate(strWithDate: String): LocalDate
    {
        val dateStr = DATE_REGEX.find(strWithDate)?.value ?: throw ParseException("Failed to parse date from string: $strWithDate")
        return LocalDate.parse(dateStr, DATE_FORMATTER)
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

    private val DATE_REGEX = Regex("""(?:[1-3]\d|\d)\.(?:ledna|února|března|dubna|května|června|července|srpna|září|října|listopadu|prosince)""")

    private val DATE_FORMATTER: DateTimeFormatter
        get() = DateTimeFormatterBuilder()
                .appendPattern("d.MMMM")
                .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
                .toFormatter(Locale.forLanguageTag("cs-CZ"))
}
