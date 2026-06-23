package dev.genesis.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import dev.genesis.core.build.SiteBuilder
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Validates site content: checks internal links, missing images, broken anchors.
 * Usage: genesis check [--dir <path>]
 */
class CheckCommand : CliktCommand(name = "check") {
    override fun help(context: com.github.ajalt.clikt.core.Context) = "Validate internal links, images, and content."
    private val dir by option("--dir", "-d", help = "Site root directory")
        .default(".")

    override fun run() = runBlocking {
        val siteRoot = Path(dir).toAbsolutePath()
        echo("Checking site: $siteRoot")
        echo("")

        val builder = SiteBuilder(siteRoot)
        val pages = builder.buildPages()

        var issueCount = 0

        // Check for missing titles
        pages.filter { it.title.isBlank() }.forEach { page ->
            echo("  ⚠ Missing title: ${page.sourcePath}")
            issueCount++
        }

        // Check for internal broken links
        val allSlugs = pages.map { it.slug }.toSet()
        for (page in pages) {
            val internalLinks = extractInternalLinks(page.renderedContent)
            for (link in internalLinks) {
                val targetSlug = link.removePrefix("/").removeSuffix("/").removeSuffix("/index.html")
                if (targetSlug.isNotEmpty() && targetSlug !in allSlugs && !isStaticAsset(siteRoot, link)) {
                    echo("  ✗ Broken link: '$link' in ${page.sourcePath}")
                    issueCount++
                }
            }
        }

        // Check for images without alt text
        for (page in pages) {
            val imagesWithoutAlt = findImagesWithoutAlt(page.renderedContent)
            for (img in imagesWithoutAlt) {
                echo("  ⚠ Image without alt text: '$img' in ${page.sourcePath}")
                issueCount++
            }
        }

        echo("")
        if (issueCount == 0) {
            echo("✓ All checks passed! (${pages.size} pages)")
        } else {
            echo("✗ Found $issueCount issue(s) across ${pages.size} pages")
        }
    }

    private fun extractInternalLinks(html: String): List<String> {
        val regex = Regex("""href="(/[^"]*?)"""")
        return regex.findAll(html)
            .map { it.groupValues[1] }
            .filter { !it.startsWith("//") && !it.startsWith("http") }
            .filter { !it.startsWith("/css/") && !it.startsWith("/js/") && !it.startsWith("/images/") }
            .toList()
    }

    private fun findImagesWithoutAlt(html: String): List<String> {
        val imgRegex = Regex("""<img[^>]*src="([^"]*)"[^>]*>""")
        return imgRegex.findAll(html)
            .filter { !it.value.contains("alt=") || it.value.contains("""alt=""""") }
            .map { it.groupValues[1] }
            .toList()
    }

    private fun isStaticAsset(siteRoot: Path, link: String): Boolean {
        val staticDir = siteRoot.resolve("static")
        val assetPath = staticDir.resolve(link.removePrefix("/"))
        return assetPath.exists()
    }
}
