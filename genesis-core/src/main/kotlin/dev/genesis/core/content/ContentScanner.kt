package dev.genesis.core.content

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Recursively scans the content directory to discover all content files.
 * Supports .md, .gmd (Genesis Markdown with components), and template files.
 */
class ContentScanner(private val contentDir: Path) {

    companion object {
        val CONTENT_EXTENSIONS = setOf("md", "gmd", "html", "peb")
    }

    /**
     * Scan the content directory and return all discovered content files.
     */
    fun scan(): List<ContentFile> {
        if (!contentDir.exists()) return emptyList()

        return Files.walk(contentDir)
            .filter { it.isRegularFile() }
            .filter { path ->
                val ext = path.extension
                ext in CONTENT_EXTENSIONS
            }
            .map { path -> toContentFile(path) }
            .toList()
    }

    private fun toContentFile(path: Path): ContentFile {
        val relativePath = contentDir.relativize(path)
        val slug = computeSlug(relativePath)
        val section = computeSection(relativePath)

        return ContentFile(
            sourcePath = path,
            relativePath = relativePath,
            slug = slug,
            section = section,
            extension = path.extension
        )
    }

    /**
     * Compute the URL slug from the relative file path.
     * content/blog/my-post.md → blog/my-post
     * content/about/index.md → about
     */
    private fun computeSlug(relativePath: Path): String {
        val pathStr = relativePath.toString().replace('\\', '/')
        val withoutExt = pathStr.substringBeforeLast('.')

        return if (withoutExt.endsWith("/index") || withoutExt == "index") {
            withoutExt.removeSuffix("/index").removeSuffix("index")
        } else {
            withoutExt
        }
    }

    /**
     * Compute the content section (top-level directory).
     * content/blog/post.md → "blog"
     * content/about.md → "" (root section)
     */
    private fun computeSection(relativePath: Path): String {
        return if (relativePath.nameCount > 1) {
            relativePath.getName(0).toString()
        } else {
            ""
        }
    }
}

/**
 * Represents a discovered content file.
 */
data class ContentFile(
    val sourcePath: Path,
    val relativePath: Path,
    val slug: String,
    val section: String,
    val extension: String
)
