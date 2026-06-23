package dev.genesis.core.build

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.*

/**
 * Manages incremental build caching.
 * Tracks content hashes of source files + their upstream dependencies (data cascade, layouts)
 * to skip re-rendering unchanged pages.
 *
 * Cache is persisted in .genesis/cache/manifest.json
 */
class IncrementalCache(private val siteRoot: Path) {

    private val cacheDir = siteRoot.resolve(".genesis").resolve("cache")
    private val manifestFile = cacheDir.resolve("manifest.json")
    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    @Serializable
    data class CacheManifest(
        val entries: MutableMap<String, CacheEntry> = mutableMapOf(),
        var layoutHash: String = "",
        var globalDataHash: String = ""
    )

    @Serializable
    data class CacheEntry(
        val contentHash: String,
        val dataHash: String,
        val outputPath: String,
        val lastBuildTimestamp: Long = 0
    )

    private var manifest: CacheManifest = CacheManifest()
    private var currentLayoutHash: String = ""
    private var currentGlobalDataHash: String = ""

    /**
     * Load the cache manifest from disk.
     */
    fun load() {
        if (manifestFile.exists()) {
            try {
                manifest = json.decodeFromString(manifestFile.readText())
            } catch (e: Exception) {
                manifest = CacheManifest()
            }
        }
    }

    /**
     * Compute the current hash of all layout/partial files.
     * If this changes, ALL pages must be re-rendered.
     */
    fun computeLayoutHash(layoutDir: Path): String {
        if (!layoutDir.exists()) return ""
        val combined = layoutDir.toFile().walkTopDown()
            .filter { it.isFile }
            .sortedBy { it.path }
            .joinToString("|") { "${it.path}:${hashBytes(it.readBytes())}" }
        currentLayoutHash = hashString(combined)
        return currentLayoutHash
    }

    /**
     * Compute hash of global data directory.
     * If this changes, ALL pages must be re-rendered.
     */
    fun computeGlobalDataHash(dataDir: Path): String {
        if (!dataDir.exists()) return ""
        val combined = dataDir.toFile().walkTopDown()
            .filter { it.isFile }
            .sortedBy { it.path }
            .joinToString("|") { "${it.path}:${hashBytes(it.readBytes())}" }
        currentGlobalDataHash = hashString(combined)
        return currentGlobalDataHash
    }

    /**
     * Check if a page needs to be re-rendered.
     * Returns true if the page has changed or its dependencies have changed.
     */
    fun needsRebuild(
        sourcePath: Path,
        contentHash: String,
        dataHash: String
    ): Boolean {
        // If layouts or global data changed, everything needs rebuild
        if (currentLayoutHash != manifest.layoutHash) return true
        if (currentGlobalDataHash != manifest.globalDataHash) return true

        val key = sourcePath.toString().replace('\\', '/')
        val entry = manifest.entries[key] ?: return true

        return entry.contentHash != contentHash || entry.dataHash != dataHash
    }

    /**
     * Record that a page was successfully built.
     */
    fun recordBuild(sourcePath: Path, contentHash: String, dataHash: String, outputPath: String) {
        val key = sourcePath.toString().replace('\\', '/')
        manifest.entries[key] = CacheEntry(
            contentHash = contentHash,
            dataHash = dataHash,
            outputPath = outputPath,
            lastBuildTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Persist the cache manifest to disk.
     */
    fun save() {
        manifest.layoutHash = currentLayoutHash
        manifest.globalDataHash = currentGlobalDataHash
        cacheDir.createDirectories()
        manifestFile.writeText(json.encodeToString(manifest))
    }

    /**
     * Invalidate the entire cache (used on clean builds).
     */
    fun invalidate() {
        manifest = CacheManifest()
        if (manifestFile.exists()) {
            manifestFile.deleteExisting()
        }
    }

    /**
     * Get the number of cached entries.
     */
    fun size(): Int = manifest.entries.size

    companion object {
        fun hashFile(path: Path): String {
            return if (path.exists()) hashBytes(path.readBytes()) else ""
        }

        fun hashString(content: String): String {
            return hashBytes(content.toByteArray())
        }

        fun hashBytes(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(bytes).joinToString("") { "%02x".format(it) }
        }
    }
}
