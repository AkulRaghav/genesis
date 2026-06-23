package dev.genesis.api

import java.nio.file.Path

/**
 * Processes assets (CSS, JS, images) during the build pipeline.
 */
interface AssetProcessor {
    /** Unique identifier for this processor */
    val id: String

    /** File extensions this processor handles (e.g. ["css", "scss"]) */
    val extensions: Set<String>

    /** Execution priority (lower = runs earlier) */
    val priority: Int get() = 1000

    /**
     * Process a single asset file.
     * @param input The source file path
     * @param output The target output path
     * @param context Build context for accessing configuration
     * @return The processed content as bytes
     */
    suspend fun process(input: Path, output: Path, context: AssetContext): ByteArray
}

/**
 * Context available to asset processors during build.
 */
interface AssetContext {
    val config: SiteConfig
    val outputDir: Path
    val isProduction: Boolean
}
