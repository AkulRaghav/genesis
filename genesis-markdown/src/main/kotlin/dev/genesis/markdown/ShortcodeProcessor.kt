package dev.genesis.markdown

import dev.genesis.api.PageMetadata
import dev.genesis.api.ShortcodeHandler

/**
 * Processes Hugo-style shortcodes in Markdown content.
 * Syntax: {{< name param="value" >}} or {{< name >}}body{{< /name >}}
 */
class ShortcodeProcessor {
    private val handlers = mutableMapOf<String, ShortcodeHandler>()

    fun register(handler: ShortcodeHandler) {
        handlers[handler.name] = handler
    }

    /**
     * Process all shortcodes in the given content.
     */
    fun process(content: String, page: PageMetadata): String {
        var result = content

        // Process paired shortcodes: {{< name >}}body{{< /name >}}
        val pairedRegex = Regex("""\{\{<\s*(\w+)((?:\s+\w+="[^"]*")*)\s*>\}\}(.*?)\{\{<\s*/\1\s*>\}\}""", RegexOption.DOT_MATCHES_ALL)
        result = pairedRegex.replace(result) { match ->
            val name = match.groupValues[1]
            val paramsStr = match.groupValues[2]
            val body = match.groupValues[3]
            val params = parseParams(paramsStr)
            handlers[name]?.render(params, body.trim(), page) ?: match.value
        }

        // Process self-closing shortcodes: {{< name param="value" >}}
        val selfClosingRegex = Regex("""\{\{<\s*(\w+)((?:\s+\w+="[^"]*")*)\s*>\}\}""")
        result = selfClosingRegex.replace(result) { match ->
            val name = match.groupValues[1]
            val paramsStr = match.groupValues[2]
            val params = parseParams(paramsStr)
            handlers[name]?.render(params, null, page) ?: match.value
        }

        return result
    }

    private fun parseParams(paramsStr: String): Map<String, String> {
        val paramRegex = Regex("""(\w+)="([^"]*)" """.trimEnd())
        return paramRegex.findAll(paramsStr).associate {
            it.groupValues[1] to it.groupValues[2]
        }
    }
}
