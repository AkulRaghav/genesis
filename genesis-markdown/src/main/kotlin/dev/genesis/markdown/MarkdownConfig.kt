package dev.genesis.markdown

/**
 * Configuration for the Markdown rendering pipeline.
 */
data class MarkdownConfig(
    /** Whether to automatically add anchor links to headings */
    val autoHeadingAnchors: Boolean = true,
    /** Whether to enable GFM task lists */
    val taskLists: Boolean = true,
    /** Whether to enable GFM tables */
    val tables: Boolean = true,
    /** Whether to enable callout/admonition syntax */
    val callouts: Boolean = true,
    /** Whether to enable footnote syntax */
    val footnotes: Boolean = true,
    /** Whether to generate a table of contents */
    val tableOfContents: Boolean = true,
    /** Whether to enable syntax highlighting for code blocks */
    val syntaxHighlight: Boolean = true,
    /** CSS class prefix for syntax highlighting */
    val highlightClassPrefix: String = "hl-"
)
