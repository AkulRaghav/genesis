package dev.genesis.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.genesis.core.build.SiteBuilder
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Builds the static site.
 * Usage: genesis build [--dir <path>] [--minify]
 */
class BuildCommand : CliktCommand(name = "build") {
    override fun help(context: com.github.ajalt.clikt.core.Context) = "Build the static site for production."
    private val dir by option("--dir", "-d", help = "Site root directory")
        .default(".")
    private val minify by option("--minify", "-m", help = "Minify output HTML/CSS/JS")
        .flag(default = false)

    override fun run() = runBlocking {
        val siteRoot = Path(dir).toAbsolutePath()
        echo("Building site from: $siteRoot")
        echo("")

        val builder = SiteBuilder(siteRoot)
        val result = builder.build()

        if (result.errors.isNotEmpty()) {
            echo("⚠ Build completed with ${result.errors.size} error(s):")
            result.errors.forEach { error ->
                echo("  ✗ ${error.file}: ${error.message}")
            }
            echo("")
        }

        echo("✓ Build complete!")
        echo("  Pages:      ${result.pageCount}")
        echo("  Time:       ${result.buildTimeMs}ms")
        echo("  Output:     ${formatSize(result.outputSize)}")
        echo("  Directory:  ${siteRoot.resolve("dist")}")
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${"%.2f".format(bytes.toDouble() / 1024 / 1024)} MB"
        }
    }
}
