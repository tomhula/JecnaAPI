package io.github.tomhula.jecnaapi.parser.parsers

import com.fleeksoft.ksoup.Ksoup
import io.github.tomhula.jecnaapi.data.document.DocumentFile
import io.github.tomhula.jecnaapi.data.document.DocumentFolder
import io.github.tomhula.jecnaapi.data.document.DocumentsPage
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

            var parentPath: String? = null
            val documents = document
                .select("ul.documents a.dir, ul.documents a.file")
                .mapNotNull { linkElement ->
                    val label = linkElement.selectFirst(".label")
                        ?.text()
                        // Replace non-breaking space with regular space
                        ?.replace("\u00A0", " ")
                        ?: return@mapNotNull null

                    val href = linkElement.attr("href")

                    if (label == "..")
                    {
                        parentPath = href.takeIf { it.isNotBlank() }
                        return@mapNotNull null
                    }

                    when
                    {
                        linkElement.hasClass("dir") -> DocumentFolder(label, href)
                        linkElement.hasClass("file") -> DocumentFile(label, href)
                        else -> null
                    }
                }

            return DocumentsPage(path = path, parentPath = parentPath, documents = documents)
        } catch (e: Exception)
        {
            throw ParseException("Failed to parse documents page.", e)
        }
    }
}
