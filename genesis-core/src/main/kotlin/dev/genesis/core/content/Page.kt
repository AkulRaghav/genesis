package dev.genesis.core.content

import java.nio.file.Path
import java.time.LocalDate

/**
 * Represents a fully parsed content page, ready for rendering.
 */
data class Page(
    /** URL slug for this page */
    val slug: String,
    /** Page title from frontmatter */
    val title: String,
    /** Optional page description */
    val description: String = "",
    /** Content section (top-level directory) */
    val section: String = "",
    /** Publication date */
    val date: LocalDate? = null,
    /** Whether this page is a draft */
    val draft: Boolean = false,
    /** Layout template to use */
    val layout: String? = null,
    /** Content type for typed collections */
    val contentType: String = "page",
    /** Weight for manual ordering */
    val weight: Int = 0,
    /** Tags taxonomy */
    val tags: List<String> = emptyList(),
    /** Categories taxonomy */
    val categories: List<String> = emptyList(),
    /** Custom taxonomy terms */
    val taxonomyTerms: Map<String, List<String>> = emptyMap(),
    /** All frontmatter data */
    val frontmatter: Map<String, Any?> = emptyMap(),
    /** Raw markdown/content body */
    val rawContent: String = "",
    /** Rendered HTML content */
    val renderedContent: String = "",
    /** Source file path */
    val sourcePath: Path? = null,
    /** Computed output path */
    val outputPath: String = "",
    /** Table of contents extracted from headings */
    val tableOfContents: List<TocEntry> = emptyList(),
    /** Estimated reading time in minutes */
    val readingTime: Int = 0,
    /** Word count */
    val wordCount: Int = 0
) {
    /**
     * The URL for this page (with pretty URLs).
     */
    val url: String
        get() = if (slug.isEmpty()) "/" else "/$slug/"

    /**
     * Convert page to a map for template context.
     */
    fun toMap(): Map<String, Any?> = buildMap {
        put("slug", slug)
        put("title", title)
        put("description", description)
        put("section", section)
        put("date", date?.toString())
        put("draft", draft)
        put("layout", layout)
        put("contentType", contentType)
        put("weight", weight)
        put("tags", tags)
        put("categories", categories)
        put("url", url)
        put("content", renderedContent)
        put("rawContent", rawContent)
        put("tableOfContents", tableOfContents.map { it.toMap() })
        put("readingTime", readingTime)
        put("wordCount", wordCount)
        putAll(frontmatter)
    }
}

data class TocEntry(
    val level: Int,
    val text: String,
    val id: String
) {
    fun toMap(): Map<String, Any> = mapOf(
        "level" to level,
        "text" to text,
        "id" to id
    )
}
