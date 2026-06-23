package dev.genesis.server

import dev.genesis.core.build.SiteBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.*

/**
 * Development server with live reload via WebSocket.
 * Watches for file changes and triggers rebuild + browser refresh.
 */
class DevServer(
    private val siteRoot: Path,
    private val port: Int = 8080,
    private val host: String = "localhost"
) {
    private val outputDir = siteRoot.resolve("dist")
    private val reloadChannel = Channel<Unit>(Channel.CONFLATED)

    /**
     * Start the dev server (blocking).
     */
    suspend fun start() {
        coroutineScope {
            // Start file watcher
            launch { watchForChanges() }

            // Start HTTP server
            val server = embeddedServer(CIO, port = port, host = host) {
                install(WebSockets)
                routing {
                    // WebSocket endpoint for live reload
                    webSocket("/ws") {
                        for (unit in reloadChannel) {
                            send(Frame.Text("reload"))
                        }
                    }

                    // Serve static files from dist/
                    get("/{path...}") {
                        val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
                        val file = resolveFile(path)

                        if (file != null && file.exists()) {
                            val contentType = contentTypeForExtension(file.extension)
                            val content = if (file.extension == "html") {
                                injectLiveReloadScript(file.readText())
                            } else {
                                file.readText()
                            }
                            call.respondText(content, contentType)
                        } else {
                            // Try pretty URL: /about → /about/index.html
                            val indexFile = outputDir.resolve(path).resolve("index.html")
                            if (indexFile.exists()) {
                                val content = injectLiveReloadScript(indexFile.readText())
                                call.respondText(content, ContentType.Text.Html)
                            } else {
                                call.respondText("Not Found", ContentType.Text.Plain, HttpStatusCode.NotFound)
                            }
                        }
                    }
                }
            }

            server.start(wait = true)
        }
    }

    private fun resolveFile(path: String): Path? {
        val resolved = when {
            path.isEmpty() || path == "/" -> outputDir.resolve("index.html")
            else -> {
                val direct = outputDir.resolve(path)
                if (direct.exists() && direct.isRegularFile()) direct
                else outputDir.resolve("$path/index.html")
            }
        }
        return if (resolved.exists()) resolved else null
    }

    private fun injectLiveReloadScript(html: String): String {
        val script = """
            <script>
            (function() {
                const ws = new WebSocket('ws://${host}:${port}/ws');
                ws.onmessage = function(event) {
                    if (event.data === 'reload') {
                        window.location.reload();
                    }
                };
                ws.onclose = function() {
                    setTimeout(function() { window.location.reload(); }, 1000);
                };
            })();
            </script>
        """.trimIndent()
        return html.replace("</body>", "$script\n</body>")
    }

    private suspend fun watchForChanges() {
        val watchService = FileSystems.getDefault().newWatchService()

        // Register all directories under siteRoot (excluding dist/ and hidden dirs)
        val dirsToWatch = listOf("content", "layouts", "static", "data")
        for (dirName in dirsToWatch) {
            val dir = siteRoot.resolve(dirName)
            if (dir.exists()) {
                registerRecursive(dir, watchService)
            }
        }

        while (true) {
            val key = withContext(Dispatchers.IO) {
                watchService.take()
            }

            var rebuild = false
            for (event in key.pollEvents()) {
                if (event.kind() != StandardWatchEventKinds.OVERFLOW) {
                    rebuild = true
                }
            }

            if (rebuild) {
                // Debounce: wait a bit for batch changes
                delay(100)
                // Drain any remaining events
                key.pollEvents()

                try {
                    val builder = SiteBuilder(siteRoot)
                    builder.build()
                    reloadChannel.trySend(Unit)
                } catch (e: Exception) {
                    // Log build error but don't crash the server
                    println("Build error: ${e.message}")
                }
            }

            key.reset()
        }
    }

    private fun registerRecursive(dir: Path, watchService: WatchService) {
        Files.walkFileTree(dir, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
                )
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun contentTypeForExtension(ext: String): ContentType {
        return when (ext) {
            "html" -> ContentType.Text.Html
            "css" -> ContentType.Text.CSS
            "js" -> ContentType.Application.JavaScript
            "json" -> ContentType.Application.Json
            "xml" -> ContentType.Application.Xml
            "svg" -> ContentType("image", "svg+xml")
            "png" -> ContentType.Image.PNG
            "jpg", "jpeg" -> ContentType.Image.JPEG
            "gif" -> ContentType.Image.GIF
            "ico" -> ContentType("image", "x-icon")
            "woff2" -> ContentType("font", "woff2")
            "woff" -> ContentType("font", "woff")
            else -> ContentType.Application.OctetStream
        }
    }
}
