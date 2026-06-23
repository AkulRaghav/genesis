package dev.genesis.core.plugin.builtin

import dev.genesis.api.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RssFeedPlugin : Plugin {
    override val id = "genesis-rss"
    override val name = "RSS Feed Generator"
    override val version = "1.0.0"

    override fun initialize(context: PluginContext) {
        context.registerLifecycleHook(RssHook(context.config))
    }
}

class RssHook(private val config: SiteConfig) : BuildLifecycleHook {
    override val id = "rss-feed-generator"

    override suspend fun onBuildComplete(context: BuildContext) {
        val baseUrl = config.baseUrl.trimEnd('/')
        val pages = context.pages.sortedByDescending { it.frontmatter["date"]?.toString() ?: "" }.take(20)
        
        val rss = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">""")
            appendLine("<channel>")
            appendLine("  <title>${escapeXml(config.title)}</title>")
            appendLine("  <link>$baseUrl</link>")
            appendLine("  <description>${escapeXml(config.description)}</description>")
            appendLine("  <language>${config.language}</language>")
            appendLine("""  <atom:link href="$baseUrl/feed.xml" rel="self" type="application/rss+xml"/>""")
            for (page in pages) {
                val url = "$baseUrl/${page.slug}/"
                appendLine("  <item>")
                appendLine("    <title>${escapeXml(page.title)}</title>")
                appendLine("    <link>$url</link>")
                appendLine("    <guid>$url</guid>")
                val date = page.frontmatter["date"]?.toString()
                if (date != null) {
                    appendLine("    <pubDate>$date</pubDate>")
                }
                appendLine("  </item>")
            }
            appendLine("</channel>")
            appendLine("</rss>")
        }
        context.outputDir.resolve("feed.xml").toFile().writeText(rss)
        
        // Also generate Atom feed
        val atom = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<feed xmlns="http://www.w3.org/2005/Atom">""")
            appendLine("  <title>${escapeXml(config.title)}</title>")
            appendLine("""  <link href="$baseUrl"/>""")
            appendLine("""  <link href="$baseUrl/atom.xml" rel="self"/>""")
            appendLine("  <id>$baseUrl/</id>")
            appendLine("  <updated>${LocalDate.now()}T00:00:00Z</updated>")
            for (page in pages) {
                val url = "$baseUrl/${page.slug}/"
                appendLine("  <entry>")
                appendLine("    <title>${escapeXml(page.title)}</title>")
                appendLine("""    <link href="$url"/>""")
                appendLine("    <id>$url</id>")
                val date = page.frontmatter["date"]?.toString() ?: LocalDate.now().toString()
                appendLine("    <updated>${date}T00:00:00Z</updated>")
                appendLine("  </entry>")
            }
            appendLine("</feed>")
        }
        context.outputDir.resolve("atom.xml").toFile().writeText(atom)
    }
    
    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
