package dev.genesis.i18n

import java.nio.file.Path
import kotlin.io.path.*

/**
 * Manages multi-language content and translations.
 * Supports content/en/, content/es/ style language directories
 * and per-language configuration.
 */
class I18nManager(
    private val contentDir: Path,
    private val defaultLanguage: String = "en",
    private val languages: List<String> = listOf("en")
) {
    /**
     * Determine the language of a content file from its path.
     */
    fun detectLanguage(relativePath: Path): String {
        val firstDir = if (relativePath.nameCount > 1) {
            relativePath.getName(0).toString()
        } else ""

        return if (firstDir in languages) firstDir else defaultLanguage
    }

    /**
     * Get the content-relative path without the language prefix.
     */
    fun stripLanguagePrefix(relativePath: Path): Path {
        val firstDir = if (relativePath.nameCount > 1) {
            relativePath.getName(0).toString()
        } else ""

        return if (firstDir in languages && relativePath.nameCount > 1) {
            relativePath.subpath(1, relativePath.nameCount)
        } else {
            relativePath
        }
    }

    /**
     * Check if multi-language mode is active.
     */
    fun isMultiLingual(): Boolean = languages.size > 1
}
