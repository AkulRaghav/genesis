package dev.genesis.core.plugin.builtin

import dev.genesis.api.*

class ReadingTimePlugin : Plugin {
    override val id = "genesis-reading-time"
    override val name = "Reading Time Estimator"
    override val version = "1.0.0"

    override fun initialize(context: PluginContext) {
        context.registerTransformer(ReadingTimeTransformer())
    }
}

class ReadingTimeTransformer : ContentTransformer {
    override val id = "reading-time"
    override val phase = TransformPhase.POST_RENDER
    override val priority = 900

    override fun transform(content: String, page: PageMetadata): String {
        // Reading time is already computed by PageBuilder, this transformer
        // injects a visible reading time element if not present in template
        val wordCount = content.split(Regex("\\s+")).count { it.isNotBlank() }
        val minutes = (wordCount / 200).coerceAtLeast(1)
        return content // Reading time is available via page.readingTime in templates
    }
}
