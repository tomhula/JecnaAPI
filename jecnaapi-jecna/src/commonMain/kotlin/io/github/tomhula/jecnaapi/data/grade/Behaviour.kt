package io.github.tomhula.jecnaapi.data.grade

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.data.notification.NotificationReference

/**
 * Represents the behaviour row in the grades table.
 */
@Serializable
data class Behaviour(
    val notifications: List<NotificationReference>,
    val finalGrade: FinalGrade
)
{
    companion object
    {
        const val SUBJECT_NAME = "Chování"
    }
}
