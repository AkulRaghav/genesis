package dev.genesis.native

/**
 * GraalVM Native Image configuration hints.
 * This module provides reflection configuration and resource registration
 * needed for native compilation.
 */
object NativeImageConfig {
    /**
     * Classes that need reflection access in native image.
     */
    val reflectionClasses = listOf(
        "dev.genesis.api.SiteConfig",
        "dev.genesis.api.BuildConfig",
        "dev.genesis.api.ServerConfig",
        "dev.genesis.api.I18nConfig",
        "dev.genesis.api.TaxonomyConfig"
    )

    /**
     * Resource patterns to include in native image.
     */
    val resourcePatterns = listOf(
        ".*\\.peb",
        ".*\\.yaml",
        ".*\\.yml",
        ".*\\.toml"
    )
}
