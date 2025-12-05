package me.tomasan7.jecnaapi.data.student

import java.time.LocalDate

/**
 * Represents a student's locker information.
 *
 * @param number The locker number (e.g., "300")
 * @param description The location description of the locker (e.g., "Přízemí - v místnosti se skříňkami 4. ulička vpravo od dveří k oknu")
 * @param assignedFrom The date when the locker was assigned
 * @param assignedUntil The date until when the locker is assigned (null if currently assigned)
 */
data class Locker(
    val number: String,
    val description: String,
    val assignedFrom: LocalDate? = null,
    val assignedUntil: LocalDate? = null
)

