package io.github.tomhula.jecnaapi.data.article

import kotlinx.serialization.Serializable
import io.github.tomhula.jecnaapi.util.emptyMutableLinkedList
import io.github.tomhula.jecnaapi.util.setAll
import kotlin.jvm.JvmStatic

/**
 * Holds all the [articles][Article] on the main news page.
 */
@Serializable
data class NewsPage(val articles: List<Article>)
{
    companion object
    {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder
    {
        private val articles = emptyMutableLinkedList<Article>()

        fun addArticle(article: Article): Builder
        {
            articles.add(article)
            return this
        }

        fun setArticles(articles: List<Article>): Builder
        {
            this.articles.setAll(articles)
            return this
        }

        fun build() = NewsPage(articles)
    }
}
