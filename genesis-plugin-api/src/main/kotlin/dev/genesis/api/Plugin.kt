package dev.genesis.api

/**
 * Base interface for all Genesis plugins.
 * Plugins are discovered via ServiceLoader or plugins.genesis.kts configuration.
 */
interface Plugin {
    /** Unique identifier for the plugin */
    val id: String

    /** Human-readable plugin name */
    val name: String

    /** Plugin version string */
    val version: String

    /** Called once when the plugin is loaded */
    fun initialize(context: PluginContext) {}

    /** Called when the plugin is being unloaded */
    fun shutdown() {}
}
