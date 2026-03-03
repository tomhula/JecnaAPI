package io.github.tomhula.jecnaapi.parser.parsers

import com.fleeksoft.ksoup.Ksoup
import io.github.tomhula.jecnaapi.data.cert.Certificate

object CertificatePageParser
{
    fun parse(html: String): List<Certificate>
    {
        val document = Ksoup.parse(html)
        val lis = document.select("ul.list li")
        val certificates = lis.map { li ->
            val labelText = li.select("span.label").text()
            val parts = labelText.split(" / ", limit = 2)
            val issuer = parts[0]
            val rest = parts[1]
            val dateParts = rest.split(" ze dne ", limit = 2)
            val title = dateParts[0]
            val dateIssued = dateParts[1]
            Certificate(dateIssued, issuer, title)
        }
        return certificates
    }
}
