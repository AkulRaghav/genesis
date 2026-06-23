package dev.genesis.templates

/**
 * Unified interface for template rendering.
 * Both the Kotlin DSL tier and Pebble tier implement this.
 */
interface TemplateEngine {
    /**
     * Render a template with the given context data.
     * @param templateName The template file name (relative to layouts directory)
     * @param context Data available to the template
     * @return Rendered HTML string
     */
    fun render(templateName: String, context: TemplateContext): String

    /**
     * Check if a template exists.
     */
    fun hasTemplate(templateName: String): Boolean
}

/**
 * Data context passed to templates during rendering.
 */
data class TemplateContext(
    /** The current page's content (rendered HTML) */
    val content: String = "",
    /** The current page's frontmatter/metadata */
    val page: Map<String, Any?> = emptyMap(),
    /** Site-wide configuration */
    val site: Map<String, Any?> = emptyMap(),
    /** Data from the data/ directory cascade */
    val data: Map<String, Any?> = emptyMap(),
    /** All pages in the site (for building nav, related content, etc.) */
    val pages: List<Map<String, Any?>> = emptyList(),
    /** Taxonomy terms and their pages */
    val taxonomies: Map<String, Map<String, List<Map<String, Any?>>>> = emptyMap(),
    /** Custom params passed by the user */
    val params: Map<String, Any?> = emptyMap()
)
