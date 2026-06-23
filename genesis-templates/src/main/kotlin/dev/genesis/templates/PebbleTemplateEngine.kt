package dev.genesis.templates

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.FileLoader
import java.io.StringWriter
import java.nio.file.Path

/**
 * Pebble-based template engine (Tier 2 — logic-light templates).
 * Supports Jinja2-like {{ variable }} and {% for %} syntax.
 */
class PebbleTemplateEngine(
    private val layoutsDir: Path
) : TemplateEngine {

    private val engine: PebbleEngine = PebbleEngine.Builder()
        .loader(FileLoader().apply {
            prefix = layoutsDir.toAbsolutePath().toString()
        })
        .autoEscaping(true)
        .cacheActive(true)
        .build()

    override fun render(templateName: String, context: TemplateContext): String {
        val template = engine.getTemplate(templateName)
        val writer = StringWriter()

        val contextMap = buildMap<String, Any?> {
            put("content", context.content)
            put("page", context.page)
            put("site", context.site)
            put("data", context.data)
            put("pages", context.pages)
            put("taxonomies", context.taxonomies)
            put("params", context.params)
        }

        template.evaluate(writer, contextMap)
        return writer.toString()
    }

    override fun hasTemplate(templateName: String): Boolean {
        return layoutsDir.resolve(templateName).toFile().exists()
    }
}
