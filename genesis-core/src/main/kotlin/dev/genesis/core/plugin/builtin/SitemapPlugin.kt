package dev.genesis.core.plugin.builtin

import dev.genesis.api.*
import java.time.LocalDate

class SitemapPlugin : Plugin {
    override val id = "genesis-sitemap"
    override val name = "Sitemap Generator"
    override val version = "1.0.0"

    override fun initialize(context: PluginContext) {
        context.registerLifecycleHook(SitemapHook(context.config.baseUrl))
    }
}

class SitemapHook(private val baseUrl: String) : BuildLifecycleHook {
    override val id = "sitemap-generator"

    override suspend fun onBuildComplete(context: BuildContext) {
        val sitemap = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")
            for (page in context.pages) {
                val url = "$baseUrl/${page.slug}/".replace("//", "/").replace(":/", "://")
                val lastmod = LocalDate.now().toString()
                appendLine("  <url>")
                appendLine("    <loc>$url</loc>")
                appendLine("    <lastmod>$lastmod</lastmod>")
                appendLine("  </url>")
            }
            appendLine("</urlset>")
        }
        context.outputDir.resolve("sitemap.xml").toFile().writeText(sitemap)
    }
}
