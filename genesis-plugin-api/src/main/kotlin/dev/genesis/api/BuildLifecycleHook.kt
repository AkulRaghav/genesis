package dev.genesis.api

/**
 * Hook into the build lifecycle at various stages.
 */
interface BuildLifecycleHook {
    /** Unique identifier for this hook */
    val id: String

    /** Called at the start of a build, before any content is processed */
    suspend fun onBuildStart(context: BuildContext) {}

    /** Called after all pages have been rendered */
    suspend fun onBuildComplete(context: BuildContext) {}

    /** Called for each page after it's rendered */
    suspend fun onPageRender(page: PageMetadata, html: String, context: BuildContext) {}
}

/**
 * Context available during the build lifecycle.
 */
interface BuildContext {
    val config: SiteConfig
    val pages: List<PageMetadata>
    val outputDir: java.nio.file.Path
    val isIncremental: Boolean
}
