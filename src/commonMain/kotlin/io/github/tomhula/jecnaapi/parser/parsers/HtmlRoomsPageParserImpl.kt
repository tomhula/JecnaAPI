package io.github.tomhula.jecnaapi.parser.parsers

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.github.tomhula.jecnaapi.data.room.RoomsPage
import io.github.tomhula.jecnaapi.data.room.RoomReference
import io.github.tomhula.jecnaapi.parser.ParseException

internal object HtmlRoomsPageParserImpl : HtmlRoomsPageParser
{
    override fun parse(html: String): RoomsPage
    {
        try
        {
            val doc: Document = Ksoup.parse(html)
            val roomsPageBuilder = RoomsPage.builder()

            val listItemEles = doc.select("ul.list a.item")
            
            for (listItemEle in listItemEles)
            {
                val href = listItemEle.attr("href").trim()
                val roomCode = href.substringAfter("/ucebna/").takeIf { it.isNotBlank() }

                val labelText = listItemEle.selectFirst(".label")?.text()?.trim()

                if (!labelText.isNullOrEmpty() && roomCode != null)
                {
                    val name = labelText.replace(PARENTHESIS_REGEX, "").trim()
                    roomsPageBuilder.addRoomReference(RoomReference(name = name, roomCode = roomCode))
                }
            }
            return roomsPageBuilder.build()
        } catch (e: ParseException)
        {
            throw ParseException("Failed to parse rooms page.", e)
        }
    }
    
    /** Matches anything inside parenthesis '(...)' */
    private val PARENTHESIS_REGEX = Regex("""\(.*\)""")
}
