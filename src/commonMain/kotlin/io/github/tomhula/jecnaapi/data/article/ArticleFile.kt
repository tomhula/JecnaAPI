package io.github.tomhula.jecnaapi.data.article

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.JvmStatic

/**
 * An attachment file to the [articles][Article] on the main news page.
 *
 * @property downloadPath A relative path to the file from the page's root. ("/")
 */
@Serializable
data class ArticleFile(
    val label: String,
    val downloadPath: String,
    val filename: String = downloadPath.split("/").last()
)
{
    @Transient
    val fileNameNoExtension = filename.split(FILE_EXTENSION_DOT_REGEX)[0]

    @Transient
    val fileExtension = filename.split(FILE_EXTENSION_DOT_REGEX)[1]

    companion object
    {
        /**
         * Matches the dot between file name and extension.
         */
        @JvmStatic
        val FILE_EXTENSION_DOT_REGEX = Regex("""\.(?!.*\.)""")
    }
}
