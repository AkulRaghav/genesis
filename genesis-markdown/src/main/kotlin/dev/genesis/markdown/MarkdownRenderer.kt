package dev.genesis.markdown

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

/**
 * Genesis Markdown rendering pipeline.
 * Wraps JetBrains/markdown with custom extensions for:
 * - Footnotes
 * - Callouts/admonitions
 * - Auto-generated heading anchors
 * - Table of contents extraction
 * - Task lists (via GFM)
 * - Tables (via GFM)
 */
class MarkdownRenderer(
    private val config: MarkdownConfig = MarkdownConfig()
) {
    private val flavour = GFMFlavourDescriptor()
    private val parser = MarkdownParser(flavour)

    data class RenderResult(
        val html: String,
        val headings: List<Heading>,
        val footnotes: List<Footnote>
    )

    data class Heading(
        val level: Int,
        val text: String,
        val id: String
    )

    data class Footnote(
        val id: String,
        val content: String
    )

    /**
     * Render markdown source to HTML with extensions applied.
     */
    fun render(source: String): RenderResult {
        // Extract footnotes before processing
        val footnotes = extractFootnotes(source)

        // Pre-process: strip footnote definitions, handle callouts, convert footnote refs
        val withoutFootnoteDefs = removeFootnoteDefinitions(source)
        val withFootnoteRefs = convertFootnoteReferences(withoutFootnoteDefs, footnotes)
        val preprocessed = preprocessCallouts(withFootnoteRefs)

        val tree = parser.buildMarkdownTreeFromString(preprocessed)
        val rawHtml = HtmlGenerator(preprocessed, tree, flavour, false).generateHtml()
        // Strip the <body> wrapper that JetBrains markdown adds
        val baseHtml = rawHtml
            .removePrefix("<body>")
            .removeSuffix("</body>")

        // Post-process: add heading anchors, extract TOC, fix callouts, append footnotes
        val headings = extractHeadings(preprocessed, tree)
        var html = baseHtml

        if (config.autoHeadingAnchors) {
            html = addHeadingAnchors(html, headings)
        }

        // Fix callout HTML structure
        html = fixCalloutHtml(html)

        // Append footnotes section if any exist
        if (footnotes.isNotEmpty()) {
            html = appendFootnotesSection(html, footnotes)
        }

        return RenderResult(
            html = html,
            headings = headings,
            footnotes = footnotes
        )
    }

    /**
     * Extract headings from the AST for TOC generation.
     */
    private fun extractHeadings(source: String, tree: ASTNode): List<Heading> {
        val headings = mutableListOf<Heading>()

        fun visit(node: ASTNode) {
            when (node.type) {
                MarkdownElementTypes.ATX_1,
                MarkdownElementTypes.ATX_2,
                MarkdownElementTypes.ATX_3,
                MarkdownElementTypes.ATX_4,
                MarkdownElementTypes.ATX_5,
                MarkdownElementTypes.ATX_6 -> {
                    val level = when (node.type) {
                        MarkdownElementTypes.ATX_1 -> 1
                        MarkdownElementTypes.ATX_2 -> 2
                        MarkdownElementTypes.ATX_3 -> 3
                        MarkdownElementTypes.ATX_4 -> 4
                        MarkdownElementTypes.ATX_5 -> 5
                        MarkdownElementTypes.ATX_6 -> 6
                        else -> 1
                    }
                    val contentNode = node.children.find { it.type == MarkdownTokenTypes.ATX_CONTENT }
                    val text = contentNode?.getTextInNode(source)?.toString()?.trim() ?: ""
                    val id = slugify(text)
                    headings.add(Heading(level, text, id))
                }
            }
            node.children.forEach { visit(it) }
        }

        visit(tree)
        return headings
    }

    /**
     * Process callout/admonition syntax: > [!NOTE], > [!WARNING], etc.
     * Transforms the first line of a callout blockquote into a div with class.
     * The closing div will be added in post-processing.
     */
    private fun preprocessCallouts(source: String): String {
        val lines = source.lines()
        val result = mutableListOf<String>()
        var inCallout = false

        for (i in lines.indices) {
            val line = lines[i]
            val calloutMatch = Regex("""^>\s*\[!(\w+)\]\s*(.*)$""").find(line)

            if (calloutMatch != null) {
                val type = calloutMatch.groupValues[1].lowercase()
                val title = calloutMatch.groupValues[2].ifEmpty {
                    type.replaceFirstChar { it.uppercase() }
                }
                // Start a callout - we'll use a marker that survives markdown rendering
                result.add("> GENESIS_CALLOUT_START_${type}_${title.replace("\"", "&quot;")}_GENESIS_CALLOUT_END")
                inCallout = true
            } else if (inCallout && line.startsWith(">")) {
                // Continue the callout content
                result.add(line)
            } else {
                if (inCallout) {
                    inCallout = false
                }
                result.add(line)
            }
        }
        return result.joinToString("\n")
    }

    /**
     * Post-process the rendered HTML to convert callout markers into proper divs.
     */
    private fun fixCalloutHtml(html: String): String {
        var result = html
        // Replace callout markers with proper HTML
        val markerRegex = Regex("""GENESIS_CALLOUT_START_(\w+)_(.+?)_GENESIS_CALLOUT_END""")
        result = markerRegex.replace(result) { match ->
            val type = match.groupValues[1]
            val title = match.groupValues[2].replace("&quot;", "\"")
            """<div class="callout callout-$type"><p class="callout-title">$title</p>"""
        }
        // Close callout divs at the end of blockquotes
        result = result.replace(Regex("""(<div class="callout[^"]*">[^<]*<p class="callout-title">[^<]*</p>\s*\n)(.*?)(</blockquote>)""", RegexOption.DOT_MATCHES_ALL)) { match ->
            "${match.groupValues[1]}${match.groupValues[2]}</div>${match.groupValues[3]}"
        }
        return result
    }

    /**
     * Convert footnote references [^1] to superscript links in source markdown.
     */
    private fun convertFootnoteReferences(source: String, footnotes: List<Footnote>): String {
        var result = source
        for (fn in footnotes) {
            // Replace [^id] references (not definitions) with a superscript HTML
            val refPattern = Regex("""\[\^${Regex.escape(fn.id)}\](?!:)""")
            result = refPattern.replace(result) {
                """<sup class="footnote-ref"><a href="#fn-${fn.id}" id="fnref-${fn.id}">${fn.id}</a></sup>"""
            }
        }
        return result
    }

    /**
     * Remove footnote definition lines from source (they'll be rendered as a section at the end).
     */
    private fun removeFootnoteDefinitions(source: String): String {
        return source.lines()
            .filter { !Regex("""^\[\^\w+\]:\s*""").containsMatchIn(it) }
            .joinToString("\n")
    }

    /**
     * Append a footnotes section at the end of the rendered HTML.
     */
    private fun appendFootnotesSection(html: String, footnotes: List<Footnote>): String {
        val footnotesHtml = buildString {
            append("""<section class="footnotes"><hr><ol>""")
            for (fn in footnotes) {
                append("""<li id="fn-${fn.id}"><p>${fn.content} <a href="#fnref-${fn.id}" class="footnote-backref">↩</a></p></li>""")
            }
            append("</ol></section>")
        }
        return "$html\n$footnotesHtml"
    }

    /**
     * Add anchor links to heading elements in the rendered HTML.
     */
    private fun addHeadingAnchors(html: String, headings: List<Heading>): String {
        var result = html
        for (heading in headings) {
            val headingRegex = Regex("""<h${heading.level}>(.*?)</h${heading.level}>""")
            val match = headingRegex.find(result) ?: continue
            val replacement = """<h${heading.level} id="${heading.id}"><a class="anchor" href="#${heading.id}">#</a>${match.groupValues[1]}</h${heading.level}>"""
            result = result.replaceFirst(match.value, replacement)
        }
        return result
    }

    /**
     * Extract footnote definitions from source.
     */
    private fun extractFootnotes(source: String): List<Footnote> {
        val footnoteRegex = Regex("""^\[\^(\w+)\]:\s*(.+)$""", RegexOption.MULTILINE)
        return footnoteRegex.findAll(source).map { match ->
            Footnote(match.groupValues[1], match.groupValues[2])
        }.toList()
    }

    companion object {
        /**
         * Convert text to a URL-friendly slug.
         */
        fun slugify(text: String): String {
            return text.lowercase()
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .replace(Regex("\\s+"), "-")
                .replace(Regex("-+"), "-")
                .trim('-')
        }
    }
}
