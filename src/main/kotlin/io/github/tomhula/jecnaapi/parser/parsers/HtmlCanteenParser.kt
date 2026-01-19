package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.canteen.DayMenu
import io.github.tomhula.jecnaapi.data.canteen.ExchangeItem
import io.github.tomhula.jecnaapi.data.canteen.Menu
import io.github.tomhula.jecnaapi.data.canteen.MenuItem
import io.github.tomhula.jecnaapi.data.canteen.MenuPage
import io.github.tomhula.jecnaapi.data.canteen.OrderResponse
import io.github.tomhula.jecnaapi.parser.ParseException
import java.time.LocalDate

/**
 * Is responsible for parsing HTML source code in [String] to [Menu] instance.
 */
internal interface HtmlCanteenParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): MenuPage

    fun parseDayMenu(html: String): DayMenu
    
    fun parseDayMenu(day: LocalDate, html: String): DayMenu

    fun parseOrderResponse(orderResponseHtml: String): OrderResponse

    fun parseCreditText(creditEleText: String): Float

    fun parseExchange(html: String): List<ExchangeItem>
}
