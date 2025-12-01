package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.ExchangeItem
import me.tomasan7.jecnaapi.data.canteen.Menu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.data.canteen.MenuPage
import me.tomasan7.jecnaapi.data.canteen.OrderResponse
import me.tomasan7.jecnaapi.parser.ParseException

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

    fun parseOrderResponse(orderResponseHtml: String): OrderResponse

    fun parseCreditText(creditEleText: String): Float

    fun parseExchange(html: String): List<ExchangeItem>
}
