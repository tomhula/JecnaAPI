package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.canteen.*
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object HtmlCanteenParserImpl : HtmlCanteenParser
{
    override fun parse(html: String): MenuPage
    {
        try
        {
            val menuBuilder = Menu.builder()

            val document = Jsoup.parse(html)

            val formEles = document.select("#mainContext > table > tbody > tr > td > form")

            for (formEle in formEles)
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

    override fun parseDayMenu(html: String): DayMenu
    {
        val element = Jsoup.parse(html).selectFirstOrThrow("body")
        return parseDayMenu(element)
    }

    override fun parseOrderResponse(orderResponseHtml: String): OrderResponse
    {
        val document = Jsoup.parse(orderResponseHtml)
        val creditEle = document.selectFirst("#Kredit")
        val timeEle = document.selectFirst("#time")

        val credit = creditEle?.let { parseCreditText(it.text()) }
        val time = timeEle?.text()?.toLong()

        return OrderResponse(credit, time)
    }

    override fun parseCreditText(creditEleText: String) = creditEleText
        .trim()
        .replace(" Kč", "")
        /* Comma replaced with dot to make conversion to float possible. */
        .replace(',', '.')
        /* Space removed, because there might be one between the thousand and so digits. */
        .replace(" ", "")
        .toFloat()

    private fun parseDayMenu(dayMenuEle: Element): DayMenu
    {
        val dayTitle = dayMenuEle.selectFirstOrThrow(".jidelnicekTop").text()
        val dayStr = DATE_REGEX.find(dayTitle)?.value ?: throw ParseException("Failed to parse day date.")
        val day = LocalDate.parse(dayStr, DATE_FORMAT)

        val dayMenuBuilder = DayMenu.builder(day)

        val menuItemEles = dayMenuEle.select(".jidelnicekMain > .jidelnicekItem")

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

        val onclick = orderButtonEle.attr("onclick").trim()
        val orderPath = extractDbProcessOrderPath(onclick)

        // Find exchange button - it shows either "do burzy" (to exchange) or "z burzy" (from exchange)
        // It can be in either .input-group (before exchange) or .icons.jidWrapRight (after exchange)
        val exchangeButtonEle = menuItemEle.select(".input-group, .icons.jidWrapRight")
            .flatMap { it.allElements }
            .find { it.ownText().contains("do burzy") || it.ownText().contains("z burzy") }

        val exchangeOnClick = exchangeButtonEle?.attr("onclick")?.trim()
        val isInExchange = exchangeButtonEle?.text()?.contains("z burzy") ?: false

        // If "do burzy" button exists, parse it with amount=1 logic
        val putOnExchangePath = if (exchangeButtonEle != null && !isInExchange) {
            exchangeOnClick?.let { buildBurzaPathWithAmountOne(it) }
        } else null

        // If "z burzy" button exists (item is in exchange), extract the removal URL directly
        val putAwayFromExchangePath = if (exchangeButtonEle != null && isInExchange) {
            exchangeOnClick?.let { extractDbProcessOrderPath(it) }
        } else null

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
            isInExchange = isInExchange,
            orderPath = orderPath,
            putOnExchangePath = putOnExchangePath,
            putAwayFromExchangePath = putAwayFromExchangePath
        )
    }

    /**
     * Extracts the "db/dbProcessOrder.jsp?..." part from the ajaxOrder onclick for ordering.
     */
    private fun extractDbProcessOrderPath(onclick: String): String
    {
        // Matches second argument in: ajaxOrder(this, 'db/dbProcessOrder.jsp?...', ...)
        val regex = Regex("ajaxOrder\\s*\\(.*?,\\s*'([^']+\\.jsp\\?[^']*)'")
        val match = regex.find(onclick) ?: throw ParseException("Failed to parse onclick attribute: $onclick")
        return match.groupValues[1]
    }

    /**
     * Builds burza (exchange) URL like the JS onclick:
     * "db/dbProcessOrder.jsp?...&amount=' + $('#burza-amountXXXX').val() + '&week=..."
     * We hardcode amount=1.
     */
    private fun buildBurzaPathWithAmountOne(onclick: String): String
    {
        // Extract the full second argument which is a concatenated string expression
        // onclick format: ajaxOrder(this, 'part1' + $('#id').val() + 'part2', 'arg3', 'arg4');
        // We need to extract and reconstruct the full URL from the concatenated parts

        // Find the start of the second argument (after first comma and optional whitespace)
        val secondArgStart = onclick.indexOf(',') + 1
        val fromSecondArg = onclick.substring(secondArgStart).trimStart()

        // Extract everything up to the closing of the second argument (before ', ' that starts third arg)
        // The second arg ends with ' + '&week=...' or similar, followed by ', '
        val secondArgEnd = fromSecondArg.indexOf("', '")
        if (secondArgEnd < 0) {
            throw ParseException("Failed to parse burza onclick - could not find end of second argument: $onclick")
        }
        val secondArgFull = fromSecondArg.substring(0, secondArgEnd)

        // Now parse the concatenated string: 'part1' + $('#id').val() + 'part2'
        // Extract only the literal string parts (in single quotes) that are NOT inside $()
        // Split by + to get individual parts, then extract string literals
        val parts = secondArgFull.split('+').map { it.trim() }

        val stringLiterals = parts.mapNotNull { part ->
            // Only extract if it's a plain string literal (starts with ')
            // Exclude jQuery selectors like $('#...')
            if (part.startsWith("'") && !part.contains("$(")) {
                // Remove leading and trailing quotes
                val content = if (part.endsWith("'")) {
                    part.substring(1, part.length - 1)
                } else {
                    // Handle case where quote is missing at end (shouldn't happen but be safe)
                    part.substring(1)
                }
                content
            } else {
                null
            }
        }
        if (stringLiterals.size < 2) {
            throw ParseException("Failed to parse burza onclick - expected at least 2 string literals: $onclick")
        }

        // Concatenate: 'db/dbProcessOrder.jsp?...&amount=' + [we insert '1'] + '&week=...'
        val beforeAmount = stringLiterals[0] // db/dbProcessOrder.jsp?...&amount=
        val afterAmount = stringLiterals[1]  // &week=&terminal=false&keyboard=false&printer=false

        val raw = beforeAmount + afterAmount

        // raw looks like: db/dbProcessOrder.jsp?time=...&token=...&ID=...&day=...&type=multiburza&amount=&week=&terminal=false...
        val jspAndFirstParams = raw.substringBefore("&amount=") // db/dbProcessOrder.jsp?time=...&token=...&ID=...&day=...&type=multiburza
        val rest = raw.substringAfter("&amount=", "") // &week=&terminal=false&keyboard=false&printer=false
        val afterAmountLiteral = rest.removePrefix("&") // week=&terminal=false&keyboard=false&printer=false

        val base = jspAndFirstParams
        val paramsMap = mutableMapOf<String, String>()

        val split = base.split('?', limit = 2)
        val path = split[0]
        val baseQuery = split.getOrNull(1).orEmpty()

        if (baseQuery.isNotBlank())
        {
            baseQuery.split('&')
                .filter { it.isNotBlank() }
                .forEach { pair ->
                    val idx = pair.indexOf('=')
                    if (idx >= 0)
                    {
                        val key = pair.substring(0, idx)
                        val value = pair.substring(idx + 1)
                        paramsMap[key] = value
                    }
                }
        }

        // Force amount=1
        paramsMap["amount"] = "1"

        if (afterAmountLiteral.isNotBlank())
        {
            afterAmountLiteral.split('&')
                .filter { it.isNotBlank() }
                .forEach { pair ->
                    val idx = pair.indexOf('=')
                    if (idx >= 0)
                    {
                        val key = pair.substring(0, idx)
                        val value = pair.substring(idx + 1)
                        paramsMap.putIfAbsent(key, value)
                    }
                }
        }

        // DON'T re-encode! The params are already URL-encoded in the HTML
        val query = paramsMap.entries.joinToString("&") { (k, v) -> "$k=$v" }

        return "$path?$query"
    }

    private fun rawText(html: String) = Jsoup.parse(html).text()

    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private val DATE_REGEX = Regex("""\d{2}\.\d{2}\.\d{4}""")

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
