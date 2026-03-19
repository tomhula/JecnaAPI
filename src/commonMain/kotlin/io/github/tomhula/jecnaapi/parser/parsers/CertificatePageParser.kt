package io.github.tomhula.jecnaapi.parser.parsers

import com.fleeksoft.ksoup.Ksoup
import io.github.tomhula.jecnaapi.data.cert.Certificate
import kotlinx.datetime.LocalDate

object CertificatePageParser
{
    fun parse(html: String): List<Certificate>
    {
        val document = Ksoup.parse(html)
        val lis = document.select("ul.list li")
        val certificates = lis.map { li ->
            val labelText = li.select("span.label").text().trim()
            val parts = labelText.split(" / ", limit = 2)
            val issuer = parts[0]
            val rest = parts[1]
            val dateParts = rest.split(" ze dne ", limit = 2)
            val title = dateParts[0]
            val dateIssued = LocalDate.parse(dateParts[1], CommonParser.CZECH_DATE_FORMAT_WITH_PADDING) 
            Certificate(dateIssued, issuer, title)
        }
        return certificates
    }
}
