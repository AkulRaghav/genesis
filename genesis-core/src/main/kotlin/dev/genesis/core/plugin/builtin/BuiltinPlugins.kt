package dev.genesis.core.plugin.builtin

import dev.genesis.core.plugin.PluginManager

/**
 * Registers all built-in Genesis plugins with the plugin manager.
 */
fun PluginManager.registerBuiltinPlugins() {
    registerPlugin(SitemapPlugin())
    registerPlugin(RssFeedPlugin())
    registerPlugin(RobotsPlugin())
    registerPlugin(ReadingTimePlugin())
    registerPlugin(OgImagePlugin())
}
