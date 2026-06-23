package dev.genesis.assets

import java.nio.file.Path
import kotlin.io.path.*

/**
 * Asset processing pipeline.
 * Handles CSS/JS bundling, image optimization, and fingerprinting.
 */
class AssetPipeline(
    private val staticDir: Path,
    private val outputDir: Path,
    private val fingerprint: Boolean = true,
    private val minify: Boolean = false
) {
    data class ProcessedAsset(
        val originalPath: String,
        val outputPath: String,
        val hash: String?
    )

    /**
     * Process all static assets.
     */
    fun process(): List<ProcessedAsset> {
        if (!staticDir.exists()) return emptyList()

        val assets = mutableListOf<ProcessedAsset>()
        staticDir.toFile().walkTopDown().filter { it.isFile }.forEach { file ->
            val relativePath = staticDir.toFile().toPath().relativize(file.toPath()).toString().replace('\\', '/')
            val content = file.readBytes()

            val hash = if (fingerprint) {
                computeHash(content)
            } else null

            val outputPath = if (hash != null && shouldFingerprint(file.extension)) {
                val name = file.nameWithoutExtension
                val ext = file.extension
                "$name.$hash.$ext"
            } else {
                relativePath
            }

            val dest = outputDir.resolve(outputPath)
            dest.parent.createDirectories()

            val processedContent = when {
                minify && file.extension == "css" -> minifyCss(content.decodeToString()).toByteArray()
                minify && file.extension == "js" -> minifyJs(content.decodeToString()).toByteArray()
                else -> content
            }
            dest.writeBytes(processedContent)

            assets.add(ProcessedAsset(relativePath, outputPath, hash))
        }
        return assets
    }

    private fun shouldFingerprint(extension: String): Boolean {
        return extension in setOf("css", "js", "woff2", "woff", "ttf")
    }

    private fun computeHash(content: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(content).take(8).joinToString("") { "%02x".format(it) }
    }

    /**
     * Basic CSS minification: remove comments, collapse whitespace.
     */
    private fun minifyCss(css: String): String {
        return css
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\s*([{}:;,])\\s*"), "$1")
            .trim()
    }

    /**
     * Basic JS minification: remove single-line comments, collapse whitespace.
     */
    private fun minifyJs(js: String): String {
        return js
            .replace(Regex("//[^\n]*"), "")
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
