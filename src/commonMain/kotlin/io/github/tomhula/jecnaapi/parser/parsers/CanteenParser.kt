package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.canteen.*
import io.github.tomhula.jecnaapi.parser.ParseException
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.datetime.LocalDate

/** https://strav.nasejidelna.cz/0341/... */
internal object CanteenParser
{
    fun parse(html: String): MenuPage
    {
        try
        {
            val menuBuilder = Menu.builder()
            val document = Ksoup.parse(html)
            val dayMenuEles = document.select(".orderContent")

            for (formEle in dayMenuEles)
            {
                val dayMenu = parseDayMenu(formEle)
                menuBuilder.addDayMenu(dayMenu.day, dayMenu)
            }

            val credit = parseCreditText(document.selectFirstOrThrow("#Kredit").text())

            return MenuPage(menuBuilder.build(), credit)
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse canteen.", e)
        }
    }

    fun parseDayMenu(html: String): DayMenu
    {
        val element = Ksoup.parse(html).body().child(0)
        return parseDayMenu(element)
    }

    fun parseOrderResponse(orderResponseHtml: String): OrderResponse
    {
        val document = Ksoup.parse(orderResponseHtml)

        val creditEle = document.selectFirstOrThrow("#Kredit")
        val timeEle = document.selectFirstOrThrow("#time")

        val credit = parseCreditText(creditEle.text())
        val time = timeEle.text().toLong()

        return OrderResponse(credit, time)
    }

    fun parseCreditText(creditEleText: String) = creditEleText
        .trim()
        .replace(" Kč", "")
        /* Comma replaced with dot to make conversion to float possible. */
        .replace(',', '.')
        /* Space removed, because there might be one between the thousand and so digits. */
        .replace(" ", "")
        .toFloat()

    fun parseExchange(html: String): List<ExchangeItem>
    {
        val body = Ksoup.parse(html).selectFirstOrThrow("body")
        val table = body.selectFirstOrThrow("div.mainContext > table.tableDataShow > tbody")
        val exchangeItemEles = table.select(".mouseOutRow")
        val items = ArrayList<ExchangeItem>(exchangeItemEles.size)

        for (exchangeItemEle in exchangeItemEles)
            items.add(parseExchange(exchangeItemEle))

        return items
    }

    private fun parseExchange(element: Element): ExchangeItem
    {
        val tds = element.select("td")
        val numberEle = tds[0]
        val dateEle = tds[1]
        val descriptionEle = tds[2]
        val amountEle = tds[4]
        val buttonEle = tds[5].selectFirstOrThrow("input")

        val number = numberEle.text().replace("Oběd ", "").toInt()

        val dayStr = CommonParser.CZECH_DATE_REGEX.find(dateEle.text())?.value ?: throw ParseException("Failed to parse day date.")
        val day = LocalDate.parse(dayStr, CommonParser.CZECH_DATE_FORMAT_WITH_PADDING)

        val itemDescriptionMatch = ITEM_DESCRIPTION_REGEX.find(descriptionEle.text())
        val soup = itemDescriptionMatch?.groups?.get(ItemDescriptionRegexGroups.SOUP)?.value
        val rest = itemDescriptionMatch?.groups?.get(ItemDescriptionRegexGroups.REST)?.value
        val description = rest?.let { ItemDescription(soup, it) }

        val amount = amountEle.text().replace(" ks", "").toInt()

        val match = EXCHANGE_ONCLICK_URL_REGEX.find(buttonEle.attr("onclick"))

        val url = match?.groupValues?.getOrNull(1)?.replace("&amp;", "&") ?: ""

        return ExchangeItem(
            number = number,
            description = description,
            amount = amount,
            orderPath = url,
            day = day
        )
    }

    private fun parseDayMenu(dayMenuEle: Element): DayMenu
    {
        val dayStr = dayMenuEle.id().removePrefix("orderContent")
        val day = LocalDate.parse(dayStr, LocalDate.Formats.ISO)

        val dayMenuBuilder = DayMenu.builder(day)

        val menuItemEles = dayMenuEle.select(".jidelnicekItem")

        for (menuItemEle in menuItemEles)
            dayMenuBuilder.addMenuItem(parseMenuItem(menuItemEle))

        return dayMenuBuilder.build()
    }

    private fun parseMenuItem(menuItemEle: Element): MenuItem
    {
        val orderButtonEle = menuItemEle.selectFirstOrThrow(".jidWrapLeft > a", "order button")
        val foodNameEle = menuItemEle.selectFirstOrThrow(".jidWrapCenter", "food name")
        val itemDescriptionStr = foodNameEle.ownText()
        val numberText = orderButtonEle.selectFirstOrThrow(".smallBoldTitle.button-link-align", "lunch number text")
        val number = numberText.text().replace("Oběd ", "").toInt()

        val itemDescription = if (itemDescriptionStr.isNotEmpty())
        {
            val itemDescriptionMatch = ITEM_DESCRIPTION_REGEX.find(itemDescriptionStr)

            val soup = itemDescriptionMatch?.groups?.get(ItemDescriptionRegexGroups.SOUP)?.value
            val rest = itemDescriptionMatch?.groups?.get(ItemDescriptionRegexGroups.REST)?.value

            rest?.let { ItemDescription(soup, it) }
        }
        else null

        val allergens = menuItemEle.select(".textGrey > .textGrey").map { rawText(it.attr("title")) }

        val onclick = orderButtonEle.attr("onclick")

        val putOnExchangeButtonEle = menuItemEle.getAllElements().find { it.ownText().contains(PUT_ON_EXCHANGE_BUTTON_REGEX) }
        val putOnExchangeButtonTextMatch = putOnExchangeButtonEle?.let { PUT_ON_EXCHANGE_BUTTON_REGEX.find(it.ownText()) }
        val isOnExchange = putOnExchangeButtonTextMatch?.groupValues?.getOrNull(1) == "z"
        val putOnExchangeOnClick = putOnExchangeButtonEle?.attr("onclick")

        val orderPath = parseAjaxOrderPath(onclick)
        val putOnExchangePath = putOnExchangeOnClick?.let { processPutOnExchangePath(parseAjaxOrderPath(it)) }
        
        return MenuItem(
            number = number,
            description = itemDescription,
            allergens = allergens,
            price = parseCreditText(
                orderButtonEle.selectFirstOrThrow(".important.warning.button-link-align", "order price").text()
            ),
            isEnabled = !orderButtonEle.hasClass("disabled"),
            /* Query for the check mark in the button ele. */
            isOrdered = orderButtonEle.selectFirst(".fa.fa-check.fa-2x") != null,
            isInExchange = isOnExchange,
            orderPath = orderPath,
            putOnExchangePath = putOnExchangePath
        )
    }
    
    private fun parseAjaxOrderPath(ajaxOrderCallString: String): String
    {
        val match = AJAX_ORDER_PATH_REGEX.find(ajaxOrderCallString) 
        val orderPath = match?.groupValues?.get(1) ?: throw ParseException("Failed to parse ajaxOrder path from string: $ajaxOrderCallString.")
        
        return orderPath
    }
    
    /* Replaces the inline JS for the amount with a fixed amount of 1. */
    private fun processPutOnExchangePath(path: String) = PUT_ON_EXCHANGE_AMOUNT_REGEX.replace(path, "1")

    private fun rawText(html: String) = Ksoup.parse(html).text()
    
    private val EXCHANGE_ONCLICK_URL_REGEX = Regex("""'([^']+)'""")
    
    /**
     * Matches the part of the putOnExchange link that is the inline JS for the amount field.
     */
    private val PUT_ON_EXCHANGE_AMOUNT_REGEX = Regex("""'\s*\+.*?\s*\+\s*'""")
    
    private val AJAX_ORDER_PATH_REGEX = Regex("""ajaxOrder\s*\(\s*this\s*,\s*'(.*?)',""")

    private val PUT_ON_EXCHANGE_BUTTON_REGEX = Regex("ks (z|do) burzy")
    
    /**
     * Matches the whole item description. Match contains capturing groups listed in [ItemDescriptionRegexGroups].
     */
    private val ITEM_DESCRIPTION_REGEX = Regex("""^(?<${ItemDescriptionRegexGroups.SOUP}>.*?), ;(?<${ItemDescriptionRegexGroups.REST}>.*)""")

    /**
     * Contains names of regex capture groups inside [ITEM_DESCRIPTION_REGEX].
     */
    private object ItemDescriptionRegexGroups
    {
        const val SOUP = "soup"
        const val REST = "rest"
    }
}
