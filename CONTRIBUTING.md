# Contributing to Genesis

Thank you for your interest in contributing to Genesis! This guide covers how to get started, the plugin API, and project conventions.

## Getting Started

### Prerequisites

- JDK 17 or later
- Gradle 8.10+ (wrapper included)

### Building

```bash
git clone https://github.com/genesis-ssg/genesis.git
cd genesis
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Running the CLI

```bash
./gradlew :genesis-cli:run --args="new my-test-site"
./gradlew :genesis-cli:run --args="build --dir my-test-site"
```

## Project Structure

Genesis is a multi-module Gradle project:

- **genesis-plugin-api** — Public interfaces for plugins (stable API)
- **genesis-core** — Content model, build pipeline, data cascade
- **genesis-markdown** — Markdown rendering with extensions
- **genesis-templates** — Kotlin DSL + Pebble template engines
- **genesis-islands** — Islands architecture for partial hydration
- **genesis-cli** — Command-line interface (Clikt)
- **genesis-server** — Dev server with live reload (Ktor)
- **genesis-assets** — Asset pipeline (CSS/JS/images)
- **genesis-search** — Build-time search index
- **genesis-i18n** — Internationalization
- **genesis-native** — GraalVM native image configuration
- **genesis-docs** — Genesis documentation (built with Genesis)

## Plugin API

Genesis is designed to be extended via plugins. The `genesis-plugin-api` module contains all public interfaces.

### Creating a Plugin

1. Add `genesis-plugin-api` as a dependency
2. Implement the `Plugin` interface
3. Register your extensions in `initialize()`

```kotlin
class MySitemapPlugin : Plugin {
    override val id = "sitemap"
    override val name = "Sitemap Generator"
    override val version = "1.0.0"

    override fun initialize(context: PluginContext) {
        context.registerLifecycleHook(SitemapHook())
    }
}

class SitemapHook : BuildLifecycleHook {
    override val id = "sitemap-generator"

    override suspend fun onBuildComplete(context: BuildContext) {
        val sitemap = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")
            for (page in context.pages) {
                appendLine("  <url><loc>${context.config.baseUrl}/${page.slug}/</loc></url>")
            }
            appendLine("</urlset>")
        }
        context.outputDir.resolve("sitemap.xml").toFile().writeText(sitemap)
    }
}
```

### Extension Points

| Interface | Purpose | Phase |
|-----------|---------|-------|
| `ContentTransformer` | Modify content (raw MD or rendered HTML) | PRE_PARSE, POST_PARSE, POST_RENDER |
| `ShortcodeHandler` | Custom `{{< name >}}` shortcodes in Markdown | During markdown preprocessing |
| `AssetProcessor` | Process CSS/JS/images | During asset pipeline |
| `BuildLifecycleHook` | Hook into build start/complete/page render | Build lifecycle |

### Plugin Distribution

Plugins can be distributed as:

1. **JAR files** — Discovered via Java ServiceLoader
2. **plugins.genesis.kts** — Configured in the site's config

### Shortcode Example

```kotlin
class YouTubeShortcode : ShortcodeHandler {
    override val name = "youtube"

    override fun render(params: Map<String, String>, body: String?, page: PageMetadata): String {
        val id = params["id"] ?: return "<!-- missing youtube id -->"
        return """
            <div class="video-container">
                <iframe src="https://www.youtube.com/embed/$id"
                        frameborder="0" allowfullscreen></iframe>
            </div>
        """.trimIndent()
    }
}
```

Usage in Markdown:
```markdown
{{< youtube id="dQw4w9WgXcQ" >}}
```

## Code Conventions

- **Language:** Kotlin, targeting JVM 17
- **Style:** Follow ktlint rules (enforced in CI)
- **Testing:** Kotest with FunSpec style
- **Coroutines:** Use structured concurrency; avoid GlobalScope
- **Serialization:** kotlinx.serialization for all data models
- **Null safety:** Avoid `!!` — use safe calls and `require()`/`check()`

## Testing Guidelines

- Unit tests for all public functions
- Golden file tests for Markdown rendering (input.md → expected.html)
- Integration tests for the build pipeline using temp directories
- Test file naming: `<ClassUnderTest>Test.kt`

## Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Write tests for new functionality
4. Ensure `./gradlew build` passes
5. Submit a PR with a clear description

## Reporting Issues

When reporting bugs, include:
- Genesis version
- JDK version
- Operating system
- Steps to reproduce
- Expected vs actual behavior
