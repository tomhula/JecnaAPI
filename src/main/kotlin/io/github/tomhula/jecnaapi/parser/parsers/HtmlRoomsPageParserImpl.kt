package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.room.RoomsPage
import io.github.tomhula.jecnaapi.data.room.RoomReference
import io.github.tomhula.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

internal object HtmlRoomsPageParserImpl : HtmlRoomsPageParser
{
    override fun parse(html: String): RoomsPage
    {
        try
        {
            val doc: Document = Jsoup.parse(html)
            val roomsPageBuilder = RoomsPage.builder()

            doc.select("ul.list > li > a.item").forEach { link ->
                val href = link.attr("href").trim()
                val symbol = href.substringAfter("/ucebna/").takeIf { it.isNotBlank() }

                val labelText = link.selectFirst("span.label")?.text()?.trim()
                if (!labelText.isNullOrEmpty() && symbol != null)
                {
                    val nameOnly = labelText.replace(ROOM_NAME_REGEX, "").trim()
                    roomsPageBuilder.addRoomReference(RoomReference(name = nameOnly, roomCode = symbol))
                }
            }
            return roomsPageBuilder.build()
        } catch (e: ParseException)
        {
            throw ParseException("Failed to parse rooms page.", e)
        }
    }
    private val ROOM_NAME_REGEX = Regex("\\s*\\(.*?\\)")
}
