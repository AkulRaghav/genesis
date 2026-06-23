package dev.genesis.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import dev.genesis.core.build.SiteBuilder
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Starts a development server with live reload.
 * Usage: genesis serve [--port 8080] [--host localhost]
 */
class ServeCommand : CliktCommand(name = "serve") {
    override fun help(context: com.github.ajalt.clikt.core.Context) = "Start a development server with live reload."
    private val dir by option("--dir", "-d", help = "Site root directory")
        .default(".")
    private val port by option("--port", "-p", help = "Server port")
        .int().default(8080)
    private val host by option("--host", help = "Server host")
        .default("localhost")

    override fun run() = runBlocking {
        val siteRoot = Path(dir).toAbsolutePath()

        // First, build the site
        echo("Building site...")
        val builder = SiteBuilder(siteRoot)
        val result = builder.build()
        echo("✓ Built ${result.pageCount} pages in ${result.buildTimeMs}ms")
        echo("")
        echo("Starting dev server at http://$host:$port")
        echo("Press Ctrl+C to stop")
        echo("")

        // Start the dev server (simplified — full implementation in genesis-server module)
        val server = dev.genesis.server.DevServer(siteRoot, port, host)
        server.start()
    }
}
