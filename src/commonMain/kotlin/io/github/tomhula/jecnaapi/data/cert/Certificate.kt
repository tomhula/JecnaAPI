package io.github.tomhula.jecnaapi.data.cert

import kotlinx.datetime.LocalDate

data class Certificate(
    val dateIssued: LocalDate,
    val issuer: String,
    val label: String,
)
