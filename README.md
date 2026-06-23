# Genesis

**A modern static site generator for the JVM.**

Hugo's speed and single-binary simplicity. Astro's islands architecture. Eleventy's template flexibility — for teams who live on the JVM and don't want to touch Node.js or Go.

---

## Features

| Feature | Genesis | Hugo | Astro | Eleventy | Zola |
|---------|---------|------|-------|----------|------|
| Language | Kotlin/JVM | Go | JS/TS | JS | Rust |
| Single binary | ✓ (GraalVM) | ✓ | ✗ | ✗ | ✓ |
| Islands architecture | ✓ | ✗ | ✓ | ✗ | ✗ |
| Zero JS by default | ✓ | ✓ | ✓ | ✓ | ✓ |
| Typed content collections | ✓ | Partial | ✓ | ✗ | ✗ |
| Kotlin DSL templates | ✓ | ✗ | ✗ | ✗ | ✗ |
| Data cascade | ✓ | ✗ | ✗ | ✓ | ✗ |
| Built-in search | ✓ | ✗ | ✗ | ✗ | ✓ |
| Live reload | ✓ | ✓ | ✓ | ✓ | ✓ |
| Plugin system | ✓ | ✗ | ✓ | ✓ | ✗ |
| i18n | ✓ | ✓ | ✓ | ✓ | ✓ |
| No Node.js required | ✓ | ✓ | ✗ | ✗ | ✓ |
| Incremental builds | ✓ | ✓ | ✓ | Partial | ✗ |
| Parallel rendering | ✓ | ✓ | ✗ | ✗ | ✗ |
| Taxonomy pages | ✓ | ✓ | ✗ | ✗ | ✓ |
| RSS/Atom feeds | ✓ | ✓ | Plugin | Plugin | ✓ |
| OG image generation | ✓ | ✗ | Plugin | ✗ | ✗ |
| Link checker | ✓ | ✗ | ✗ | Plugin | ✗ |

## Quick Start

```bash
# Create a new site
genesis new my-blog

# Start developing
cd my-blog
genesis serve

# Build for production
genesis build

# Validate content
genesis check
```

## Performance

Benchmarked on Windows 11, 16-core CPU, JDK 17:

| Site Size | Cold Build | Incremental (1 file) |
|-----------|-----------|---------------------|
| 50 pages | 136ms | 75ms |
| 1,000 pages | 2,083ms | 576ms |

See [BENCHMARKS.md](BENCHMARKS.md) for full details.

## Architecture

```
genesis/
├── genesis-core/          # Content model, build pipeline, plugin integration
├── genesis-markdown/      # Markdown→HTML pipeline, footnotes, callouts, anchors
├── genesis-templates/     # Kotlin DSL template engine + Pebble adapter
├── genesis-islands/       # Islands architecture: partial hydration system
├── genesis-cli/           # Clikt CLI: new, build, serve, check commands
├── genesis-server/        # Ktor dev server + WebSocket live reload
├── genesis-assets/        # CSS/JS minification, fingerprinting
├── genesis-search/        # Build-time inverted search index + JS widget
├── genesis-i18n/          # Multi-language content management
├── genesis-plugin-api/    # Public extension interfaces
├── genesis-native/        # GraalVM native-image config
└── genesis-docs/          # Genesis docs (built with Genesis)
```

## Project Structure

```
my-site/
├── genesis.yaml          # Site configuration
├── content/              # Markdown content
│   ├── index.md
│   ├── blog/
│   │   ├── first-post.md
│   │   └── _data.yaml   # Per-directory data cascade
│   └── about.md
├── layouts/              # Templates (.peb or .genesis.kts)
│   ├── base.peb
│   ├── single.peb
│   └── partials/
├── static/               # Static assets
├── data/                 # Global data (YAML/JSON)
└── dist/                 # Built output (generated)
    ├── sitemap.xml       # Auto-generated
    ├── feed.xml          # RSS feed
    ├── atom.xml          # Atom feed
    ├── robots.txt        # Auto-generated
    ├── search/           # Search index + widget
    ├── og/               # Generated OG images
    ├── tags/             # Taxonomy listing pages
    └── categories/       # Taxonomy listing pages
```

## Templating

### Tier 1: Kotlin DSL (`.genesis.kts`)

Type-safe HTML builders with IDE support:

```kotlin
html {
    doctype()
    html("en") {
        head { title("${page.title}"); metaCharset(); metaViewport() }
        body {
            main { raw(content) }
        }
    }
}
```

### Tier 2: Pebble Templates (`.peb`)

Jinja2-like syntax:

```html
{% extends "base.peb" %}
{% block content %}
<article>
    <h1>{{ page.title }}</h1>
    {{ content | raw }}
</article>
{% endblock %}
```

## Content Features

- **YAML/TOML frontmatter** with typed data
- **Data cascade** (global → directory → page)
- **Footnotes** with clickable references
- **Callouts/admonitions** (`> [!WARNING]`, `> [!NOTE]`)
- **Auto heading anchors** + table of contents
- **Taxonomy pages** (tags, categories) with auto-generated listings
- **Reading time** calculation

## Islands Architecture

Ship zero JavaScript by default. Hydrate only interactive components:

```markdown
<island src="/js/counter.js" hydrate="visible">
    <span>0</span>
</island>
```

Hydration strategies: `load`, `idle`, `visible`, `client:only`

## Plugin System

5 built-in plugins ship with Genesis:
- **Sitemap** — sitemap.xml generation
- **RSS/Atom** — feed.xml + atom.xml
- **Robots.txt** — robots.txt with sitemap ref
- **Reading Time** — word count estimator
- **OG Images** — 1200×630 PNG per page

Extend Genesis with custom plugins:

```kotlin
class MyPlugin : Plugin {
    override val id = "my-plugin"
    override val name = "My Plugin"
    override val version = "1.0.0"
    override fun initialize(context: PluginContext) {
        context.registerShortcode(YouTubeShortcode())
    }
}
```

Extension points: `ContentTransformer`, `ShortcodeHandler`, `AssetProcessor`, `BuildLifecycleHook`

## CLI Commands

| Command | Description |
|---------|-------------|
| `genesis new <name>` | Scaffold a new site (blog/docs/portfolio templates) |
| `genesis build` | Production build with plugins, search, taxonomies |
| `genesis serve` | Dev server with WebSocket live reload |
| `genesis check` | Validate links, images, accessibility |

## Configuration

```yaml
title: "My Site"
baseUrl: "https://example.com"
description: "Built with Genesis"
language: "en"
prettyUrls: true

taxonomies:
  tags: { singular: "tag", plural: "tags" }
  categories: { singular: "category", plural: "categories" }

build:
  minify: true
  fingerprint: true
  parallel: true
  incrementalCache: true

server:
  port: 8080
  liveReload: true
```

## Building from Source

```bash
git clone https://github.com/genesis-ssg/genesis.git
cd genesis
./gradlew build
./gradlew :genesis-cli:run --args="new my-test-site"
./gradlew :genesis-cli:run --args="build --dir my-test-site"
```

Requires JDK 17+.

## License

Apache 2.0
