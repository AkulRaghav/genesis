---
title: "Plugin System"
description: "Extending Genesis with plugins"
weight: 20
---

# Plugin System

Genesis is extensible via plugins that hook into the build lifecycle.

## Built-in Plugins

| Plugin | Output | Description |
|--------|--------|-------------|
| Sitemap | sitemap.xml | XML sitemap for search engines |
| RSS Feed | feed.xml, atom.xml | RSS 2.0 and Atom feeds |
| Robots | robots.txt | Crawler directives |
| Reading Time | — | Word count estimation |
| OG Images | og/*.png | Social media preview images |

## Writing a Plugin

```kotlin
class MyPlugin : Plugin {
    override val id = "my-plugin"
    override val name = "My Plugin"
    override val version = "1.0.0"

    override fun initialize(context: PluginContext) {
        // Register extensions
        context.registerShortcode(MyShortcode())
        context.registerLifecycleHook(MyHook())
    }
}
```

## Extension Points

### ContentTransformer

Modify content before or after rendering:

```kotlin
class MyTransformer : ContentTransformer {
    override val id = "my-transform"
    override val phase = TransformPhase.POST_RENDER
    override val priority = 1000

    override fun transform(content: String, page: PageMetadata): String {
        return content.replace("foo", "bar")
    }
}
```

### ShortcodeHandler

Custom shortcodes in Markdown:

```kotlin
class YouTubeShortcode : ShortcodeHandler {
    override val name = "youtube"

    override fun render(params: Map<String, String>, body: String?, page: PageMetadata): String {
        val id = params["id"] ?: return ""
        return """<iframe src="https://youtube.com/embed/$id"></iframe>"""
    }
}
```

Usage: `{{< youtube id="dQw4w9WgXcQ" >}}`

### BuildLifecycleHook

Hook into build events:

```kotlin
class MyHook : BuildLifecycleHook {
    override val id = "my-hook"

    override suspend fun onBuildComplete(context: BuildContext) {
        // Generate custom output files
    }
}
```

## Plugin Discovery

Plugins are discovered via:
1. Java ServiceLoader (JAR-distributed plugins)
2. Built-in registration (ships with Genesis)
