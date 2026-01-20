package io.github.tomhula.jecnaapi.parser.parsers

import io.github.tomhula.jecnaapi.data.notification.Notification
import io.github.tomhula.jecnaapi.data.notification.NotificationReference
import io.github.tomhula.jecnaapi.parser.ParseException

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
