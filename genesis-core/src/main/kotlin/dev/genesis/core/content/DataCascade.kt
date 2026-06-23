package dev.genesis.core.content

import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlElement
import net.mamoe.yamlkt.YamlMap
import net.mamoe.yamlkt.YamlList
import net.mamoe.yamlkt.YamlLiteral
import net.mamoe.yamlkt.YamlNull
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Implements Eleventy-style data cascade.
 * Data merges from multiple sources in priority order:
 * 1. Page frontmatter (highest priority)
 * 2. Per-directory _data.yaml files
 * 3. Global data/ directory files
 * 4. Site config defaults (lowest priority)
 *
 * Child directories inherit and can override parent data.
 */
class DataCascade(
    private val dataDir: Path,
    private val contentDir: Path
) {
    private val globalData = mutableMapOf<String, Any?>()
    private val directoryData = mutableMapOf<String, Map<String, Any?>>()

    /**
     * Load all data sources. Call this once before rendering.
     */
    fun load() {
        loadGlobalData()
        loadDirectoryData(contentDir, "")
    }

    /**
     * Resolve the merged data for a specific content file path.
     * Merges global data → directory data (from root to leaf) → page frontmatter.
     */
    fun resolveFor(relativePath: Path, frontmatter: Map<String, Any?>): Map<String, Any?> {
        val merged = mutableMapOf<String, Any?>()

        // Start with global data
        merged.putAll(globalData)

        // Apply directory data from root to the file's directory
        val pathParts = mutableListOf<String>()
        val parent = relativePath.parent
        if (parent != null) {
            for (i in 0 until parent.nameCount) {
                pathParts.add(parent.getName(i).toString())
                val dirKey = pathParts.joinToString("/")
                directoryData[dirKey]?.let { merged.putAll(it) }
            }
        }

        // Frontmatter has highest priority
        merged.putAll(frontmatter)

        return merged
    }

    /**
     * Get all global data (from data/ directory).
     */
    fun getGlobalData(): Map<String, Any?> = globalData.toMap()

    private fun loadGlobalData() {
        if (!dataDir.exists()) return

        Files_walkDataDir(dataDir).forEach { file ->
            val key = file.nameWithoutExtension
            val content = file.readText()
            val data = parseDataFile(content, file.extension)
            if (data != null) {
                globalData[key] = data
            }
        }
    }

    private fun loadDirectoryData(dir: Path, prefix: String) {
        if (!dir.exists()) return

        // Look for _data.yaml or _data.yml in this directory
        val dataFiles = listOf("_data.yaml", "_data.yml", "_data.json")
        for (dataFileName in dataFiles) {
            val dataFile = dir.resolve(dataFileName)
            if (dataFile.exists()) {
                val content = dataFile.readText()
                val data = parseDataFile(content, dataFile.extension)
                if (data is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    directoryData[prefix] = data as Map<String, Any?>
                }
                break
            }
        }

        // Recurse into subdirectories
        dir.listDirectoryEntries().filter { it.isDirectory() && !it.name.startsWith("_") }.forEach { subDir ->
            val subPrefix = if (prefix.isEmpty()) subDir.name else "$prefix/${subDir.name}"
            loadDirectoryData(subDir, subPrefix)
        }
    }

    private fun parseDataFile(content: String, extension: String): Any? {
        return when (extension) {
            "yaml", "yml" -> parseYamlData(content)
            "json" -> parseJsonData(content)
            else -> null
        }
    }

    private fun parseYamlData(content: String): Any? {
        return try {
            val element = Yaml.decodeYamlFromString(content)
            yamlToNative(element)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseJsonData(content: String): Any? {
        // Basic JSON parsing — for now, delegate to yamlkt which handles JSON too
        return parseYamlData(content)
    }

    private fun yamlToNative(element: YamlElement): Any? {
        return when (element) {
            is YamlNull -> null
            is YamlLiteral -> {
                val c = element.content
                when {
                    c.equals("true", true) -> true
                    c.equals("false", true) -> false
                    c.toIntOrNull() != null -> c.toInt()
                    c.toLongOrNull() != null -> c.toLong()
                    c.toDoubleOrNull() != null -> c.toDouble()
                    else -> c
                }
            }
            is YamlMap -> element.entries.associate { (k, v) -> k.toString() to yamlToNative(v) }
            is YamlList -> element.map { yamlToNative(it) }
            else -> element.toString()
        }
    }

    private fun Files_walkDataDir(dir: Path): List<Path> {
        return dir.listDirectoryEntries()
            .filter { it.isRegularFile() && it.extension in setOf("yaml", "yml", "json") }
    }
}
