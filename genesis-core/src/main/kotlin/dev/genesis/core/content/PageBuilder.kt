package dev.genesis.core.content

import dev.genesis.markdown.MarkdownRenderer
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.io.path.readText

/**
 * Builds Page objects from ContentFiles by parsing frontmatter,
 * rendering markdown, and computing metadata.
 */
class PageBuilder(
    private val markdownRenderer: MarkdownRenderer,
    private val frontmatterParser: FrontmatterParser,
    private val dataCascade: DataCascade,
    private val prettyUrls: Boolean = true
) {
    /**
     * Build a fully-resolved Page from a ContentFile.
     */
    fun build(contentFile: ContentFile): Page {
        val rawText = contentFile.sourcePath.readText()
        val (frontmatter, body) = frontmatterParser.parse(rawText)
        val mergedData = dataCascade.resolveFor(contentFile.relativePath, frontmatter)

        val renderResult = markdownRenderer.render(body)

        val title = mergedData["title"]?.toString() ?: contentFile.slug.substringAfterLast('/')
            .replace('-', ' ')
            .replaceFirstChar { it.uppercase() }

        val date = parseDate(mergedData["date"])
        val tags = parseStringList(mergedData["tags"])
        val categories = parseStringList(mergedData["categories"])
        val wordCount = body.split(Regex("\\s+")).count { it.isNotBlank() }
        val readingTime = (wordCount / 200).coerceAtLeast(1)

        val outputPath = computeOutputPath(contentFile.slug)

        return Page(
            slug = contentFile.slug,
            title = title,
            description = mergedData["description"]?.toString() ?: "",
            section = contentFile.section,
            date = date,
            draft = mergedData["draft"] == true,
            layout = mergedData["layout"]?.toString(),
            contentType = mergedData["type"]?.toString() ?: "page",
            weight = (mergedData["weight"] as? Number)?.toInt() ?: 0,
            tags = tags,
            categories = categories,
            taxonomyTerms = mapOf("tags" to tags, "categories" to categories),
            frontmatter = mergedData,
            rawContent = body,
            renderedContent = renderResult.html,
            sourcePath = contentFile.sourcePath,
            outputPath = outputPath,
            tableOfContents = renderResult.headings.map {
                TocEntry(it.level, it.text, it.id)
            },
            readingTime = readingTime,
            wordCount = wordCount
        )
    }

    private fun computeOutputPath(slug: String): String {
        return if (prettyUrls) {
            if (slug.isEmpty()) "index.html" else "$slug/index.html"
        } else {
            if (slug.isEmpty()) "index.html" else "$slug.html"
        }
    }

    private fun parseDate(value: Any?): LocalDate? {
        if (value == null) return null
        val str = value.toString()
        val formatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
        )
        for (fmt in formatters) {
            try {
                return LocalDate.parse(str, fmt)
            } catch (_: DateTimeParseException) {
                continue
            }
        }
        return null
    }

    private fun parseStringList(value: Any?): List<String> {
        return when (value) {
            is List<*> -> value.mapNotNull { it?.toString() }
            is String -> value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
    }
}
