package dev.genesis.core.plugin.builtin

import dev.genesis.api.*

class RobotsPlugin : Plugin {
    override val id = "genesis-robots"
    override val name = "Robots.txt Generator"
    override val version = "1.0.0"

    override fun initialize(context: PluginContext) {
        context.registerLifecycleHook(RobotsHook(context.config.baseUrl))
    }
}

class RobotsHook(private val baseUrl: String) : BuildLifecycleHook {
    override val id = "robots-txt-generator"

    override suspend fun onBuildComplete(context: BuildContext) {
        val robots = buildString {
            appendLine("User-agent: *")
            appendLine("Allow: /")
            appendLine()
            appendLine("Sitemap: ${baseUrl.trimEnd('/')}/sitemap.xml")
        }
        context.outputDir.resolve("robots.txt").toFile().writeText(robots)
    }
}
