package io.github.tomhula.jecnaapi.service

import io.ktor.client.statement.*
import io.github.tomhula.jecnaapi.data.substitution.SubstitutionResponse
import io.github.tomhula.jecnaapi.data.substitution.SubstitutionStatus
import io.github.tomhula.jecnaapi.data.substitution.TeacherAbsence
import io.github.tomhula.jecnaapi.data.substitution.LabeledTeacherAbsences
import io.github.tomhula.jecnaapi.data.timetable.TimetablePage
import io.github.tomhula.jecnaapi.web.jecna.JecnaWebClient
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * Service for managing substitutions and teacher absences.
 * Handles fetching, parsing, and merging substitution data.
 */
class SubstitutionService(private val webClient: JecnaWebClient) {

    /**
     * Fetches substitutions from the remote endpoint and deserializes them.
     * Returns an error response if the endpoint is unavailable.
     */
    suspend fun getSubstitutions(): SubstitutionResponse {
        return try {
            val response = webClient.plainQuery(SUBSTITUTION_ENDPOINT)
            json.decodeFromString(response.bodyAsText())
        } catch (e: Exception) {
            createSubstitutionErrorResponse()
        }
    }

    /**
     * Fetches substitutions and merges them into a timetable page.
     * Returns the original page if merging fails.
     */
    suspend fun fetchAndMergeSubstitutions(
        page: TimetablePage,
        getStudentClassNameProvider: suspend () -> String?
    ): TimetablePage {
        return try {
            val substitutions = getSubstitutions()
            val className = getStudentClassNameProvider()
            mergeSubstitutionsIntoPage(page, substitutions, className)
        } catch (e: Exception) {
            page
        }
    }

    /**
     * Merges substitution data into a timetable page.
     * If className is available, performs full merge; otherwise sets substitution message.
     */
    private fun mergeSubstitutionsIntoPage(
        page: TimetablePage,
        substitutions: SubstitutionResponse,
        className: String?
    ): TimetablePage = if (className != null) {
        page.mergeSubstitutions(substitutions, className)
    } else {
        page.copy(substitutionMessage = substitutions.status.message)
    }

    /**
     * Retrieves teacher absences from the substitution endpoint, labeled by date.
     * Each element corresponds to one day with the date label and list of absences.
     */
    suspend fun getTeacherAbsences(): List<LabeledTeacherAbsences> {
        val substitutions = getSubstitutions()
        return if (isSubstitutionEndpointDown(substitutions)) {
            createErrorAbsenceResponse(substitutions.status.message)
        } else {
            substitutions.labeledAbsencesByDay
        }
    }

    /**
     * Checks if the substitution endpoint is down based on response characteristics.
     */
    private fun isSubstitutionEndpointDown(substitutions: SubstitutionResponse): Boolean =
        substitutions.schedule.isEmpty() && substitutions.props.isEmpty() && substitutions.status.message != null

    /**
     * Creates a default error response when substitution endpoint is unavailable.
     */
    private fun createSubstitutionErrorResponse(): SubstitutionResponse = SubstitutionResponse(
        schedule = emptyList(),
        props = emptyList(),
        status = SubstitutionStatus(
            lastUpdated = "",
            currentUpdateSchedule = 0,
            message = "Endpoint na suplování je nyní nedostupný!"
        )
    )

    /**
     * Creates a placeholder error absence response when endpoint is down.
     */
    private fun createErrorAbsenceResponse(message: String?): List<LabeledTeacherAbsences> = listOf(
        LabeledTeacherAbsences(
            date = "(unknown date)",
            absences = listOf(
                TeacherAbsence(
                    teacher = null,
                    teacherCode = "",
                    type = "",
                    hours = null,
                    message = message ?: "Unknown error"
                )
            )
        )
    )

    companion object {
        private const val SUBSTITUTION_ENDPOINT = "https://jecnarozvrh.jzitnik.dev/versioned/v1"
    }
}

