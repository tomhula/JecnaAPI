package io.github.tomhula.jecnaapi.data.notification

import io.github.tomhula.jecnaapi.data.schoolStaff.TeacherReference
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents a single notification about a student's behaviour or general information for parents.
 */
@Serializable
data class Notification(
    val notificationType: NotificationReference.NotificationType,
    val exactType: String,
    val date: LocalDate,
    val message: String,
    val issuedBy: TeacherReference?,
    val caseNumber: String?
)
