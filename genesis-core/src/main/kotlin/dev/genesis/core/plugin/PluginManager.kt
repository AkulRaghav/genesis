package dev.genesis.core.plugin

import dev.genesis.api.*
import java.nio.file.Path
import java.util.ServiceLoader

class PluginManager(
    override val siteRoot: Path,
    override val outputDir: Path,
    override val config: SiteConfig
) : PluginContext {
    private val plugins = mutableListOf<Plugin>()
    private val transformers = mutableListOf<ContentTransformer>()
    private val shortcodes = mutableMapOf<String, ShortcodeHandler>()
    private val assetProcessors = mutableListOf<AssetProcessor>()
    private val lifecycleHooks = mutableListOf<BuildLifecycleHook>()

    override fun registerTransformer(transformer: ContentTransformer) { transformers.add(transformer) }
    override fun registerShortcode(handler: ShortcodeHandler) { shortcodes[handler.name] = handler }
    override fun registerAssetProcessor(processor: AssetProcessor) { assetProcessors.add(processor) }
    override fun registerLifecycleHook(hook: BuildLifecycleHook) { lifecycleHooks.add(hook) }

    fun discoverPlugins() {
        val loader = ServiceLoader.load(Plugin::class.java)
        for (plugin in loader) {
            plugins.add(plugin)
            plugin.initialize(this)
        }
    }

    fun registerPlugin(plugin: Plugin) {
        plugins.add(plugin)
        plugin.initialize(this)
    }

    fun getTransformers(phase: TransformPhase): List<ContentTransformer> =
        transformers.filter { it.phase == phase }.sortedBy { it.priority }

    fun getShortcodeHandler(name: String): ShortcodeHandler? = shortcodes[name]
    fun getAssetProcessors(): List<AssetProcessor> = assetProcessors.sortedBy { it.priority }
    fun getLifecycleHooks(): List<BuildLifecycleHook> = lifecycleHooks

    fun shutdown() {
        plugins.forEach { it.shutdown() }
    }
}
