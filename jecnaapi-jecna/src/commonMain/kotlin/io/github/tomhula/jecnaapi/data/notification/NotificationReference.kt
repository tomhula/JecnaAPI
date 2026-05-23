package io.github.tomhula.jecnaapi.data.notification

import kotlinx.serialization.Serializable

/**
 * Represents a single notification reference about a student's behaviour or general information for parents.
 */
@Serializable
data class NotificationReference(
    val type: NotificationType,
    val message: String,
    val recordId: Int
) {
    /**
     * Type of the [NotificationReference], either [GOOD], [BAD] or [INFORMATION].
     */
    @Serializable
    enum class NotificationType
    {
        GOOD,
        BAD,
        /**
         * Represents formal notifications.
         * Example usage: information about excuse from a subject.
         */
        INFORMATION
    }
}
