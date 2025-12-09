package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.notification.Notification
import me.tomasan7.jecnaapi.data.notification.NotificationReference
import me.tomasan7.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [Notification] instance.
 */
internal interface HtmlNotificationParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): List<NotificationReference>

    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun getNotification(html: String): Notification
}
