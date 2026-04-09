package io.github.tomhula.jecnaapi.parser.parsers

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.github.tomhula.jecnaapi.data.document.DocumentFile
import io.github.tomhula.jecnaapi.data.document.DocumentFolder
import io.github.tomhula.jecnaapi.data.document.DocumentsPage
import io.github.tomhula.jecnaapi.data.document.SchoolDocument
import io.github.tomhula.jecnaapi.parser.ParseException

internal object DocumentsPageParser
{
    fun parse(html: String): DocumentsPage
    {
        try
        {
            val document = Ksoup.parse(html)
            val path = document.selectFirst(".documentPath")
                ?.text()
                ?.removePrefix("Adresa:")
                ?.trim()
                ?: "/dokumenty"

            val parentPath = document
                .select("ul.documents a.dir")
                .firstOrNull { link ->
                    val label = link.selectFirst(".label")
                        ?.text()
                        ?.replace("\u00A0", " ")
                        ?: return@firstOrNull false

                    label == ".."
                }
                ?.attr("href")
                ?.takeIf { it.isNotBlank() }

            val documents = document
                .select("ul.documents a.dir, ul.documents a.file")
                .mapNotNull { parseDocument(it) }

            return DocumentsPage(path = path, parentPath = parentPath, documents = documents)
        } catch (e: Exception)
        {
            throw ParseException("Failed to parse documents page.", e)
        }
    }

    private fun parseDocument(linkElement: Element): SchoolDocument?
    {
        val label = linkElement.selectFirst(".label")
            ?.text()
            ?.replace("\u00A0", " ")
            ?: return null

        if (label == "..") return null

        val href = linkElement.attr("href")

        return when
        {
            linkElement.hasClass("dir") -> DocumentFolder(label, href)
            linkElement.hasClass("file") -> DocumentFile(label, href)
            else -> null
        }
    }
}
