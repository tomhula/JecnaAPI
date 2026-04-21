package io.github.tomhula.jecnaapi.data.document

import kotlinx.serialization.Serializable

@Serializable
sealed class SchoolDocument
{
    abstract val label: String
    abstract val path: String
}

@Serializable
data class DocumentFolder(
    override val label: String,
    override val path: String
) : SchoolDocument()

@Serializable
data class DocumentFile(
    override val label: String,
    override val path: String
) : SchoolDocument()

@Serializable
data class DocumentsPage(
    val path: String,
    val parentPath: String? = null,
    val documents: List<SchoolDocument>
)
