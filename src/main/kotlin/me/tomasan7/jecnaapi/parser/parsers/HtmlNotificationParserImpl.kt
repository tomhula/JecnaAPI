package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.notification.Notification
import me.tomasan7.jecnaapi.data.notification.NotificationReference
import me.tomasan7.jecnaapi.data.schoolStaff.TeacherReference
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object HtmlNotificationParserImpl : HtmlNotificationParser
{
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun parse(html: String): List<NotificationReference> {
        val notificationIdRegex = Regex("""userStudentRecordId=(\d+)""")

        try
        {
            val document = Jsoup.parse(html)
            val list = document.selectFirstOrThrow("main > div > ul.list")
            val items = list.select("li > a")

            return items.map { item ->
                val href = item.attr("href")
                val id = notificationIdRegex.find(href)?.groupValues?.get(1) ?: "0"
                val text = item.selectFirstOrThrow("span.label").text()
                val (_, rest) = text.split(", ", limit = 2)
                val type = getNotificationType(item.selectFirstOrThrow("span.sprite-icon-16"))

                NotificationReference(type, rest, id.toInt())
            }
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse teacher.", e)
        }
    }

    override fun getNotification(html: String): Notification
    {
        try
        {
            val document = Jsoup.parse(html)
            val table = document.selectFirstOrThrow("table.userprofile")

            val tableRows = table.select("tr")

            val map = mapTableRows(tableRows)

            val iconEle = document.selectFirstOrThrow("h1#h1 > span.icon.sprite-icon-32")
            val notificationType = getNotificationType(iconEle)

            val exactType = map["Typ"]?.selectFirstOrThrow("span")?.text() ?: ""
            val dateString = map["Datum"]?.selectFirstOrThrow("span")?.text() ?: ""
            val message = map["Sdělení"]?.selectFirstOrThrow("span")?.text() ?: ""
            val caseNumber = map["Číslo jednací"]?.selectFirst("span")?.text()

            val teacherReference = map["Udělil"]?.selectFirst("a")?.let { teacherObject ->
                val name = teacherObject.selectFirst(".label")?.text()
                val tag = teacherObject.attr("href").substringAfterLast("/")
                if (name != null) TeacherReference(name, tag) else null
            }

            val date = LocalDate.parse(dateString, dateFormatter)

            return Notification(
                notificationType,
                exactType,
                date,
                message,
                teacherReference,
                caseNumber
            )
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse teacher.", e)
        }
    }

    fun getNotificationType(iconEle: Element): NotificationReference.NotificationType {
        val classNames = iconEle.classNames()

        return when {
            classNames.any { it in listOf("sprite-icon-tick-32", "sprite-icon-tick-16") } ->
                NotificationReference.NotificationType.GOOD
            classNames.any { it in listOf("sprite-icon-auction_hammer_gavel-32", "sprite-icon-auction_hammer_gavel-16") } ->
                NotificationReference.NotificationType.INFORMATION
            else ->
                NotificationReference.NotificationType.BAD
        }
    }

    fun mapTableRows(tableRows: Elements): HashMap<String, Element>
    {
        val map = HashMap<String, Element>(tableRows.size)

        for (row in tableRows) {
            val title = row.selectFirstOrThrow("th > span.label").text()
            val body = row.selectFirstOrThrow("td")

            map[if (title == "Udělila") "Udělil" else title] = body
        }

        return map
    }
}