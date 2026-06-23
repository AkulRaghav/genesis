package dev.genesis.api

import java.nio.file.Path

/**
 * Context provided to plugins during initialization.
 * Gives access to the site configuration and registration APIs.
 */
interface PluginContext {
    /** Root directory of the site project */
    val siteRoot: Path

    /** Output directory for the built site */
    val outputDir: Path

    /** Site configuration */
    val config: SiteConfig

    /** Register a content transformer */
    fun registerTransformer(transformer: ContentTransformer)

    /** Register a shortcode handler */
    fun registerShortcode(handler: ShortcodeHandler)

    /** Register an asset processor */
    fun registerAssetProcessor(processor: AssetProcessor)

    /** Register a build lifecycle hook */
    fun registerLifecycleHook(hook: BuildLifecycleHook)
}
