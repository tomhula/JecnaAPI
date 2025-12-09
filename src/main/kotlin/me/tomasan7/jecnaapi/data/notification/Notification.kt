package me.tomasan7.jecnaapi.data.notification

import me.tomasan7.jecnaapi.data.schoolStaff.TeacherReference
import java.time.LocalDate

/**
 * Represents a single notification about a student's behaviour or general information for parents.
 */
data class Notification(
    val notificationType: NotificationReference.NotificationType,
    val exactType: String,
    val date: LocalDate,
    val message: String,
    val issuedBy: TeacherReference?,
    val caseNumber: String?
)