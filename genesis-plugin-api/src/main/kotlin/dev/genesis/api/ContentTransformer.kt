package dev.genesis.api

/**
 * Transforms content before or after rendering.
 * Implementations can modify the raw source (pre) or the rendered HTML (post).
 */
interface ContentTransformer {
    /** Unique identifier for this transformer */
    val id: String

    /** Execution priority (lower = runs earlier). Default transformers run at 1000. */
    val priority: Int get() = 1000

    /** The phase at which this transformer runs */
    val phase: TransformPhase

    /**
     * Transform the content string.
     * @param content The content to transform (raw markdown for PRE, HTML for POST)
     * @param page Metadata about the page being transformed
     * @return The transformed content
     */
    fun transform(content: String, page: PageMetadata): String
}

enum class TransformPhase {
    /** Before markdown parsing */
    PRE_PARSE,
    /** After markdown parsing, before template rendering */
    POST_PARSE,
    /** After full rendering */
    POST_RENDER
}

/**
 * Metadata about a page, available to transformers and plugins.
 */
data class PageMetadata(
    val slug: String,
    val title: String,
    val sourcePath: String,
    val outputPath: String,
    val frontmatter: Map<String, Any?>,
    val contentType: String = "page",
    val language: String = "en"
)
