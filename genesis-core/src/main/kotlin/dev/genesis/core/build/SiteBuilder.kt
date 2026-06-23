package dev.genesis.core.build

import dev.genesis.api.*
import dev.genesis.core.content.*
import dev.genesis.core.config.ConfigLoader
import dev.genesis.core.plugin.PluginManager
import dev.genesis.core.plugin.builtin.registerBuiltinPlugins
import dev.genesis.islands.IslandProcessor
import dev.genesis.markdown.MarkdownRenderer
import dev.genesis.search.SearchIndexBuilder
import dev.genesis.templates.PebbleTemplateEngine
import dev.genesis.templates.TemplateContext
import dev.genesis.templates.TemplateEngine
import kotlinx.coroutines.*
import java.nio.file.Path
import kotlin.io.path.*

class SiteBuilder(
    private val siteRoot: Path,
    private val config: SiteConfig = ConfigLoader.load(siteRoot)
) {
    private val contentDir = siteRoot.resolve(config.contentDir)
    private val outputDir = siteRoot.resolve(config.outputDir)
    private val layoutsDir = siteRoot.resolve(config.layoutDir)
    private val staticDir = siteRoot.resolve(config.staticDir)
    private val dataDir = siteRoot.resolve(config.dataDir)

    private val markdownRenderer = MarkdownRenderer()
    private val frontmatterParser = FrontmatterParser()
    private val dataCascade = DataCascade(dataDir, contentDir)
    private val pageBuilder = PageBuilder(markdownRenderer, frontmatterParser, dataCascade, config.prettyUrls)
    private val islandProcessor = IslandProcessor()

    data class BuildResult(
        val pageCount: Int,
        val buildTimeMs: Long,
        val outputSize: Long,
        val errors: List<BuildError> = emptyList()
    )

    data class BuildError(val file: String, val message: String)

    suspend fun build(forceClean: Boolean = false): BuildResult {
        val startTime = System.currentTimeMillis()
        val errors = mutableListOf<BuildError>()

        val useIncremental = config.build.incrementalCache && !forceClean
        val cache = IncrementalCache(siteRoot)

        if (config.build.incrementalCache) {
            cache.load()
            cache.computeLayoutHash(layoutsDir)
            cache.computeGlobalDataHash(dataDir)
            if (forceClean) cache.invalidate()
        }

        if (!useIncremental || forceClean) cleanOutput() else outputDir.createDirectories()

        // Initialize plugin system
        val pluginManager = PluginManager(siteRoot, outputDir, config)
        pluginManager.registerBuiltinPlugins()
        pluginManager.discoverPlugins()

        dataCascade.load()

        val scanner = ContentScanner(contentDir)
        val contentFiles = scanner.scan()

        val pages = contentFiles.mapNotNull { file ->
            try { pageBuilder.build(file) }
            catch (e: Exception) { errors.add(BuildError(file.sourcePath.toString(), e.message ?: "Unknown error")); null }
        }.filter { !it.draft }

        // Generate taxonomy pages
        val taxonomyPages = generateTaxonomyPages(pages)
        val allPages = pages + taxonomyPages

        val pagesToRender = if (useIncremental) {
            allPages.filter { page ->
                val contentHash = IncrementalCache.hashString(page.rawContent)
                val dataHash = IncrementalCache.hashString(page.frontmatter.toString())
                val sourcePath = page.sourcePath ?: return@filter true
                cache.needsRebuild(siteRoot.relativize(sourcePath), contentHash, dataHash)
            }
        } else allPages

        // Notify plugins: build start
        val buildContext = GenesisBuildContext(config, allPages.map { it.toPageMetadata() }, outputDir, useIncremental)
        pluginManager.getLifecycleHooks().forEach { it.onBuildStart(buildContext) }

        val templateEngine = createTemplateEngine()
        val renderedPages = renderPages(pagesToRender, templateEngine, allPages)

        // Process islands in rendered HTML
        val processedPages = renderedPages.map { rp ->
            if (rp.html.contains("<island") || rp.html.contains("data-island")) {
                RenderedPage(rp.outputPath, islandProcessor.processHtml(rp.html))
            } else rp
        }

        writeOutput(processedPages)

        // Notify plugins: page render
        for (page in pagesToRender) {
            val meta = page.toPageMetadata()
            pluginManager.getLifecycleHooks().forEach { it.onPageRender(meta, page.renderedContent, buildContext) }
        }

        // Notify plugins: build complete
        pluginManager.getLifecycleHooks().forEach { it.onBuildComplete(buildContext) }

        // Build search index
        buildSearchIndex(allPages)

        // Update cache
        if (config.build.incrementalCache) {
            for (page in pagesToRender) {
                val contentHash = IncrementalCache.hashString(page.rawContent)
                val dataHash = IncrementalCache.hashString(page.frontmatter.toString())
                val sourcePath = page.sourcePath ?: continue
                cache.recordBuild(siteRoot.relativize(sourcePath), contentHash, dataHash, page.outputPath)
            }
            cache.save()
        }

        copyStaticAssets()
        pluginManager.shutdown()

        val buildTime = System.currentTimeMillis() - startTime
        return BuildResult(processedPages.size, buildTime, calculateOutputSize(), errors)
    }

    suspend fun buildPages(): List<Page> {
        dataCascade.load()
        val scanner = ContentScanner(contentDir)
        return scanner.scan().mapNotNull { file ->
            try { pageBuilder.build(file) } catch (_: Exception) { null }
        }.filter { !it.draft }
    }

    private fun generateTaxonomyPages(pages: List<Page>): List<Page> {
        val taxonomyPages = mutableListOf<Page>()
        val pageSize = 10 // posts per page

        // Generate tag listing pages with pagination
        val tagGroups = mutableMapOf<String, MutableList<Page>>()
        for (page in pages) {
            for (tag in page.tags) {
                tagGroups.getOrPut(tag) { mutableListOf() }.add(page)
            }
        }
        for ((tag, tagPages) in tagGroups) {
            val baseSlug = "tags/${MarkdownRenderer.slugify(tag)}"
            taxonomyPages.addAll(paginateListingPages("Tag: $tag", baseSlug, "tags", tagPages, pageSize))
        }

        // Generate category listing pages with pagination
        val catGroups = mutableMapOf<String, MutableList<Page>>()
        for (page in pages) {
            for (cat in page.categories) {
                catGroups.getOrPut(cat) { mutableListOf() }.add(page)
            }
        }
        for ((cat, catPages) in catGroups) {
            val baseSlug = "categories/${MarkdownRenderer.slugify(cat)}"
            taxonomyPages.addAll(paginateListingPages("Category: $cat", baseSlug, "categories", catPages, pageSize))
        }

        return taxonomyPages
    }

    private fun paginateListingPages(title: String, baseSlug: String, section: String, pages: List<Page>, pageSize: Int): List<Page> {
        val sorted = pages.sortedByDescending { it.date?.toString() ?: "" }
        val totalPages = ((sorted.size + pageSize - 1) / pageSize).coerceAtLeast(1)
        val result = mutableListOf<Page>()

        for (pageNum in 1..totalPages) {
            val startIdx = (pageNum - 1) * pageSize
            val endIdx = minOf(startIdx + pageSize, sorted.size)
            val pageItems = sorted.subList(startIdx, endIdx)

            val slug = if (pageNum == 1) baseSlug else "$baseSlug/page/$pageNum"
            val outputPath = "$slug/index.html"
            val content = buildPaginatedListingHtml(title, pageItems, pageNum, totalPages, baseSlug, sorted.size)

            result.add(Page(
                slug = slug, title = if (pageNum == 1) title else "$title - Page $pageNum",
                section = section, contentType = "taxonomy", rawContent = "",
                renderedContent = content, outputPath = outputPath
            ))
        }
        return result
    }

    private fun buildPaginatedListingHtml(title: String, pages: List<Page>, currentPage: Int, totalPages: Int, baseSlug: String, totalItems: Int): String {
        return buildString {
            append("<h1>$title</h1>\n<p>$totalItems posts</p>\n<ul>\n")
            for (page in pages) {
                append("""<li><a href="${page.url}">${page.title}</a>""")
                if (page.date != null) append(" <time>${page.date}</time>")
                append("</li>\n")
            }
            append("</ul>\n")
            // Pagination nav
            if (totalPages > 1) {
                append("""<nav class="pagination">""")
                if (currentPage > 1) {
                    val prevUrl = if (currentPage == 2) "/$baseSlug/" else "/$baseSlug/page/${currentPage - 1}/"
                    append("""<a href="$prevUrl" class="prev">← Previous</a> """)
                }
                append("""<span>Page $currentPage of $totalPages</span> """)
                if (currentPage < totalPages) {
                    append("""<a href="/$baseSlug/page/${currentPage + 1}/" class="next">Next →</a>""")
                }
                append("</nav>")
            }
        }
    }

    private fun buildSearchIndex(pages: List<Page>) {
        val searchBuilder = SearchIndexBuilder()
        for (page in pages) {
            searchBuilder.addDocument(SearchIndexBuilder.SearchDocument(
                slug = page.slug, title = page.title, description = page.description,
                content = page.rawContent, section = page.section, tags = page.tags
            ))
        }
        searchBuilder.build(outputDir)
    }

    private fun cleanOutput() {
        if (outputDir.exists()) outputDir.toFile().deleteRecursively()
        outputDir.createDirectories()
    }

    private fun createTemplateEngine(): TemplateEngine {
        return if (layoutsDir.exists()) PebbleTemplateEngine(layoutsDir)
        else DefaultTemplateEngine(config)
    }

    private suspend fun renderPages(pages: List<Page>, templateEngine: TemplateEngine, allPages: List<Page>): List<RenderedPage> = coroutineScope {
        val siteContext = buildSiteContext(allPages)
        if (config.build.parallel) {
            pages.map { page -> async(Dispatchers.Default) { renderSinglePage(page, templateEngine, siteContext, allPages) } }.awaitAll()
        } else {
            pages.map { page -> renderSinglePage(page, templateEngine, siteContext, allPages) }
        }
    }

    private fun renderSinglePage(page: Page, templateEngine: TemplateEngine, siteContext: Map<String, Any?>, allPages: List<Page>): RenderedPage {
        val templateName = resolveTemplate(page, templateEngine)
        val context = TemplateContext(
            content = page.renderedContent, page = page.toMap(), site = siteContext,
            data = dataCascade.getGlobalData(), pages = allPages.map { it.toMap() }
        )
        val html = if (templateName != null) templateEngine.render(templateName, context) else wrapInDefaultHtml(page)
        return RenderedPage(page.outputPath, html)
    }

    private fun resolveTemplate(page: Page, engine: TemplateEngine): String? {
        val candidates = listOfNotNull(
            page.layout?.let { "$it.peb" },
            if (page.section.isNotEmpty()) "${page.section}/single.peb" else null,
            "single.peb", "base.peb"
        )
        return candidates.firstOrNull { engine.hasTemplate(it) }
    }

    private fun wrapInDefaultHtml(page: Page): String = """<!DOCTYPE html>
<html lang="${config.language}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${page.title} - ${config.title}</title>
    ${if (page.description.isNotEmpty()) """<meta name="description" content="${page.description}">""" else ""}
</head>
<body>
    <main><article><h1>${page.title}</h1>${page.renderedContent}</article></main>
</body>
</html>"""

    private fun buildSiteContext(pages: List<Page>): Map<String, Any?> = buildMap {
        put("title", config.title); put("baseUrl", config.baseUrl)
        put("description", config.description); put("language", config.language)
        put("params", config.params); put("pageCount", pages.size)
    }

    private fun writeOutput(pages: List<RenderedPage>) {
        for (page in pages) {
            val outputFile = outputDir.resolve(page.outputPath)
            outputFile.parent.createDirectories()
            outputFile.writeText(page.html)
        }
    }

    private fun copyStaticAssets() {
        if (!staticDir.exists()) return
        staticDir.toFile().walkTopDown().filter { it.isFile }.forEach { file ->
            val relativePath = staticDir.toFile().toPath().relativize(file.toPath())
            val dest = outputDir.resolve(relativePath)
            dest.parent.createDirectories()
            file.copyTo(dest.toFile(), overwrite = true)
        }
    }

    private fun calculateOutputSize(): Long {
        if (!outputDir.exists()) return 0
        return outputDir.toFile().walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }
}

data class RenderedPage(val outputPath: String, val html: String)

private class DefaultTemplateEngine(private val config: SiteConfig) : TemplateEngine {
    override fun render(templateName: String, context: TemplateContext) = context.content
    override fun hasTemplate(templateName: String) = false
}

private class GenesisBuildContext(
    override val config: SiteConfig,
    override val pages: List<PageMetadata>,
    override val outputDir: Path,
    override val isIncremental: Boolean
) : BuildContext

private fun Page.toPageMetadata() = PageMetadata(
    slug = slug, title = title, sourcePath = sourcePath?.toString() ?: "",
    outputPath = outputPath, frontmatter = frontmatter, contentType = contentType
)
