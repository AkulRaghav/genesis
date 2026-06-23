package dev.genesis.api

import kotlinx.serialization.Serializable

/**
 * Top-level site configuration, typically loaded from genesis.yaml or genesis.toml.
 */
@Serializable
data class SiteConfig(
    val title: String = "My Genesis Site",
    val baseUrl: String = "http://localhost:8080",
    val description: String = "",
    val language: String = "en",
    val theme: String? = null,
    val prettyUrls: Boolean = true,
    val contentDir: String = "content",
    val outputDir: String = "dist",
    val dataDir: String = "data",
    val layoutDir: String = "layouts",
    val staticDir: String = "static",
    val taxonomies: Map<String, TaxonomyConfig> = mapOf(
        "tags" to TaxonomyConfig("tag", "tags"),
        "categories" to TaxonomyConfig("category", "categories")
    ),
    val params: Map<String, String> = emptyMap(),
    val build: BuildConfig = BuildConfig(),
    val server: ServerConfig = ServerConfig(),
    val i18n: I18nConfig = I18nConfig()
)

@Serializable
data class TaxonomyConfig(
    val singular: String,
    val plural: String
)

@Serializable
data class BuildConfig(
    val minify: Boolean = false,
    val fingerprint: Boolean = true,
    val parallel: Boolean = true,
    val incrementalCache: Boolean = true
)

@Serializable
data class ServerConfig(
    val port: Int = 8080,
    val host: String = "localhost",
    val liveReload: Boolean = true
)

@Serializable
data class I18nConfig(
    val defaultLanguage: String = "en",
    val languages: List<String> = listOf("en")
)
