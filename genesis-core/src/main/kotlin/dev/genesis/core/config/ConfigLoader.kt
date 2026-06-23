package dev.genesis.core.config

import dev.genesis.api.SiteConfig
import dev.genesis.api.BuildConfig
import dev.genesis.api.I18nConfig
import dev.genesis.api.ServerConfig
import dev.genesis.api.TaxonomyConfig
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlElement
import net.mamoe.yamlkt.YamlMap
import net.mamoe.yamlkt.YamlLiteral
import net.mamoe.yamlkt.YamlList
import net.mamoe.yamlkt.YamlNull
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Loads site configuration from genesis.yaml, genesis.yml, or genesis.toml.
 */
object ConfigLoader {

    private val CONFIG_FILES = listOf("genesis.yaml", "genesis.yml", "genesis.toml")

    /**
     * Load configuration from the site root directory.
     * Falls back to defaults if no config file is found.
     */
    fun load(siteRoot: Path): SiteConfig {
        val configFile = CONFIG_FILES
            .map { siteRoot.resolve(it) }
            .firstOrNull { it.exists() }
            ?: return SiteConfig()

        val content = configFile.readText()
        return when (configFile.extension) {
            "yaml", "yml" -> parseYamlConfig(content)
            "toml" -> parseTomlConfig(content)
            else -> SiteConfig()
        }
    }

    private fun parseYamlConfig(content: String): SiteConfig {
        return try {
            val element = Yaml.decodeYamlFromString(content)
            if (element is YamlMap) {
                buildConfigFromMap(element)
            } else {
                SiteConfig()
            }
        } catch (e: Exception) {
            SiteConfig()
        }
    }

    private fun parseTomlConfig(content: String): SiteConfig {
        // Simple TOML parser for top-level keys
        val map = mutableMapOf<String, String>()
        for (line in content.lines()) {
            val eq = line.indexOf('=')
            if (eq > 0 && !line.trimStart().startsWith('#') && !line.trimStart().startsWith('[')) {
                val key = line.substring(0, eq).trim()
                val value = line.substring(eq + 1).trim().removeSurrounding("\"")
                map[key] = value
            }
        }
        return SiteConfig(
            title = map["title"] ?: "My Genesis Site",
            baseUrl = map["baseUrl"] ?: map["base_url"] ?: "http://localhost:8080",
            description = map["description"] ?: "",
            language = map["language"] ?: "en",
            contentDir = map["contentDir"] ?: map["content_dir"] ?: "content",
            outputDir = map["outputDir"] ?: map["output_dir"] ?: "dist",
            prettyUrls = map["prettyUrls"]?.toBooleanStrictOrNull() ?: true
        )
    }

    /**
     * Look up a value in a YamlMap by string key.
     * YamlMap keys are YamlElements, so we find by matching key.toString().
     */
    private fun YamlMap.lookup(key: String): YamlElement? {
        return this.entries.firstOrNull { (k, _) ->
            val keyStr = when (k) {
                is YamlLiteral -> k.content
                else -> k.toString()
            }
            keyStr == key
        }?.value
    }

    private fun YamlMap.str(key: String, default: String = ""): String {
        val value = lookup(key)
        return if (value is YamlLiteral) value.content else default
    }

    private fun YamlMap.bool(key: String, default: Boolean): Boolean {
        val value = lookup(key)
        return if (value is YamlLiteral) value.content.toBooleanStrictOrNull() ?: default else default
    }

    private fun YamlMap.int(key: String, default: Int): Int {
        val value = lookup(key)
        return if (value is YamlLiteral) value.content.toIntOrNull() ?: default else default
    }

    private fun buildConfigFromMap(map: YamlMap): SiteConfig {
        val buildElement = map.lookup("build")
        val build = if (buildElement is YamlMap) {
            BuildConfig(
                minify = buildElement.bool("minify", false),
                fingerprint = buildElement.bool("fingerprint", true),
                parallel = buildElement.bool("parallel", true),
                incrementalCache = buildElement.bool("incrementalCache", true)
            )
        } else BuildConfig()

        val serverElement = map.lookup("server")
        val server = if (serverElement is YamlMap) {
            ServerConfig(
                port = serverElement.int("port", 8080),
                host = serverElement.str("host", "localhost"),
                liveReload = serverElement.bool("liveReload", true)
            )
        } else ServerConfig()

        return SiteConfig(
            title = map.str("title", "My Genesis Site"),
            baseUrl = map.str("baseUrl", map.str("base_url", "http://localhost:8080")),
            description = map.str("description"),
            language = map.str("language", "en"),
            prettyUrls = map.bool("prettyUrls", true),
            contentDir = map.str("contentDir", map.str("content_dir", "content")),
            outputDir = map.str("outputDir", map.str("output_dir", "dist")),
            dataDir = map.str("dataDir", map.str("data_dir", "data")),
            layoutDir = map.str("layoutDir", map.str("layout_dir", "layouts")),
            staticDir = map.str("staticDir", map.str("static_dir", "static")),
            build = build,
            server = server
        )
    }
}
