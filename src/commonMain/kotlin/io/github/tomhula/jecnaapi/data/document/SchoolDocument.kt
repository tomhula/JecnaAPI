package io.github.tomhula.jecnaapi.data.document

import kotlinx.serialization.Serializable

@Serializable
sealed class SchoolDocument
{
    abstract val label: String
}

@Serializable
data class DocumentFolder(
    override val label: String,
    val path: String
) : SchoolDocument()

@Serializable
data class DocumentFile(
    override val label: String,
    val downloadPath: String
) : SchoolDocument()

@Serializable
data class DocumentsPage(
    val path: String,
    val documents: List<SchoolDocument>
)
