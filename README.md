# Genesis

**A modern, production-grade static site generator for the JVM ecosystem.**

Genesis delivers Hugo's build speed and single-binary distribution, Astro's islands architecture for partial hydration, and Eleventy's configuration-light philosophy with data cascade — purpose-built for teams who live on the JVM and don't want to touch Node.js or Go.

---

## Why Genesis?

Static site generators have exploded in popularity, but most force you into a specific ecosystem: Hugo requires Go knowledge for templates, Astro and Eleventy require Node.js and its dependency hell, and Zola lacks extensibility. Genesis fills the gap for JVM developers who want a fast, extensible, modern SSG without leaving Kotlin.

**The pitch:** Write your content in Markdown, your templates in Kotlin or Pebble, your interactive islands in JS (or Kotlin/JS), and ship a zero-JavaScript static site by default — all from a single 15.6 MB JAR with no runtime dependencies.

---

## Feature Comparison

| Feature | Genesis | Hugo | Astro | Eleventy | Zola |
|---------|---------|------|-------|----------|------|
| **Language** | Kotlin/JVM | Go | JS/TS | JS | Rust |
| **Single binary** | ✓ (Fat JAR / GraalVM) | ✓ | ✗ | ✗ | ✓ |
| **Islands architecture** | ✓ (4 hydration modes) | ✗ | ✓ | ✗ | ✗ |
| **Zero JS by default** | ✓ | ✓ | ✓ | ✓ | ✓ |
| **Typed content collections** | ✓ (via frontmatter schemas) | Partial | ✓ | ✗ | ✗ |
| **Type-safe templates** | ✓ (Kotlin DSL) | ✗ | ✗ | ✗ | ✗ |
| **Data cascade** | ✓ (Eleventy-style) | ✗ | ✗ | ✓ | ✗ |
| **Built-in search** | ✓ (inverted index + JS widget) | ✗ | ✗ | ✗ | ✓ |
| **Live reload** | ✓ (WebSocket) | ✓ | ✓ | ✓ | ✓ |
| **Plugin system** | ✓ (ServiceLoader + API) | ✗ | ✓ | ✓ | ✗ |
| **i18n** | ✓ | ✓ | ✓ | ✓ | ✓ |
| **No Node.js required** | ✓ | ✓ | ✗ | ✗ | ✓ |
| **Incremental builds** | ✓ (SHA-256 hashing) | ✓ | ✓ | Partial | ✗ |
| **Parallel rendering** | ✓ (coroutines) | ✓ | ✗ | ✗ | ✗ |
| **Taxonomy pages** | ✓ (with pagination) | ✓ | ✗ | ✗ | ✓ |
| **RSS/Atom feeds** | ✓ (built-in) | ✓ | Plugin | Plugin | ✓ |
| **OG image generation** | ✓ (1200×630 PNG) | ✗ | Plugin | ✗ | ✗ |
| **Link checker** | ✓ (built-in) | ✗ | ✗ | Plugin | ✗ |
| **Footnotes** | ✓ | ✓ | Plugin | Plugin | ✗ |
| **Callouts/admonitions** | ✓ | ✗ | Plugin | ✗ | ✗ |

---

## Quick Start

```bash
# Create a new site (blog, docs, or portfolio template)
genesis new my-blog

# Enter the project
cd my-blog

# Start the development server with live reload
genesis serve

# Build for production (generates dist/)
genesis build

# Validate content (broken links, missing alt text)
genesis check
```

**Single-command onboarding:** `genesis new my-site && cd my-site && genesis serve` works with zero additional configuration.

---

## Performance

All benchmarks measured on Windows 11, 16 logical cores (AMD), 512MB JVM heap, JDK 17:

### Build Speed

| Site Size | Cold Build (parallel) | Incremental (1 file changed) | Per-page cost |
|-----------|----------------------|------------------------------|---------------|
| 4 pages | 187ms | — | 47ms |
| 50 pages | 136ms | 75ms | 2.7ms |
| 1,000 pages | 2,083ms | 576ms | 2.1ms |

### Parallelism

| Mode | 50 pages | Speedup |
|------|----------|---------|
| Sequential | 268ms | 1.0x |
| Parallel (16 cores) | 136ms | 1.97x |

### Incremental Builds

| Scenario | 50 pages | 1,000 pages |
|----------|----------|-------------|
| Cold build | 360ms | 2,083ms |
| Warm (no changes) | 94ms | 646ms |
| 1 file changed | 75ms | 576ms |
| Speedup over cold | 4.8x | 3.6x |

### How Genesis Compares

| SSG | 1,000 pages | Language | Notes |
|-----|-------------|----------|-------|
| Hugo | ~100ms | Go | Highly optimized C-based template engine |
| Zola | ~200ms | Rust | Single-threaded, minimal features |
| **Genesis** | **2,083ms cold / 576ms incremental** | Kotlin/JVM | Full plugin system, OG images, search |
| Eleventy | ~2,000–5,000ms | Node.js | Depends on template engine |
| Astro | ~3,000–8,000ms | Node.js | Full framework with many features |

Genesis is competitive with Node-based SSGs while offering significantly more built-in features. The JVM cold-start penalty (~500ms) makes sub-second full builds difficult at 1,000+ pages, but incremental builds hit "instant feedback" for development. GraalVM native image would eliminate startup overhead.

---

## Architecture

Genesis is a multi-module Gradle project with clean separation of concerns:

```
genesis/
├── genesis-plugin-api/    # Public extension interfaces (stable API contract)
├── genesis-markdown/      # Markdown→HTML: footnotes, callouts, anchors, TOC
├── genesis-templates/     # Kotlin HTML DSL (Tier 1) + Pebble adapter (Tier 2)
├── genesis-islands/       # Islands architecture: partial hydration system
├── genesis-core/          # Build pipeline, data cascade, plugin integration
├── genesis-cli/           # Clikt-based CLI: new, build, serve, check
├── genesis-server/        # Ktor CIO dev server + WebSocket live reload
├── genesis-assets/        # CSS/JS minification + SHA-256 fingerprinting
├── genesis-search/        # Build-time inverted index + vanilla JS widget
├── genesis-i18n/          # Multi-language content path routing
├── genesis-native/        # GraalVM native-image reflection config
└── genesis-docs/          # Genesis's own docs, built by Genesis (dogfooding)
```

### Module Dependency Graph

```
genesis-plugin-api (interfaces only, no dependencies)
    ↑
    ├── genesis-markdown (JetBrains/markdown parser)
    ├── genesis-templates (Pebble + Kotlin DSL)
    ├── genesis-islands (hydration processor)
    ├── genesis-search (inverted index builder)
    ├── genesis-assets (minification + fingerprinting)
    └── genesis-i18n (language routing)
            ↑
        genesis-core (build pipeline, plugins, taxonomies)
            ↑
        genesis-server (Ktor dev server)
            ↑
        genesis-cli (Clikt commands, Shadow JAR entry point)
```

---

## Project Structure

A Genesis site follows this layout:

```
my-site/
├── genesis.yaml            # Site configuration
├── content/                # Markdown content files
│   ├── index.md            # Home page
│   ├── about.md            # Static page
│   ├── blog/               # Blog section
│   │   ├── _data.yaml      # Per-directory data (inherits down)
│   │   ├── first-post.md
│   │   └── second-post.md
│   └── docs/               # Another section
│       └── getting-started.md
├── layouts/                # Template files
│   ├── base.peb            # Base layout (Pebble)
│   ├── single.peb          # Single page template
│   ├── home.peb            # Home page template
│   └── partials/           # Reusable template fragments
├── static/                 # Static assets (copied as-is)
│   ├── css/style.css
│   ├── js/
│   └── images/
├── data/                   # Global data files (YAML/JSON)
│   └── site.yaml           # Available as {{ data.site.* }}
└── dist/                   # Generated output
    ├── index.html
    ├── about/index.html    # Pretty URLs by default
    ├── blog/first-post/index.html
    ├── tags/kotlin/index.html      # Auto-generated taxonomy
    ├── categories/tutorial/index.html
    ├── sitemap.xml         # Plugin: sitemap
    ├── feed.xml            # Plugin: RSS 2.0
    ├── atom.xml            # Plugin: Atom feed
    ├── robots.txt          # Plugin: robots
    ├── og/                 # Plugin: social images
    │   ├── index.png
    │   └── blog-first-post.png
    └── search/             # Built-in search
        ├── index.json      # Inverted index
        └── search.js       # Client widget
```

---

## Content Authoring

### Frontmatter

Every content file begins with YAML (default) or TOML frontmatter:

```markdown
---
title: "Building Production Systems with Kotlin"
date: "2024-03-15"
description: "A deep dive into Kotlin for backend services"
tags: ["kotlin", "architecture", "jvm"]
categories: ["engineering"]
author: "Jane Developer"
draft: false
layout: "post"
weight: 10
---

Your markdown content here...
```

### Supported Frontmatter Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `title` | string | Derived from filename | Page title |
| `date` | string | — | Publication date (YYYY-MM-DD) |
| `description` | string | `""` | Meta description for SEO |
| `tags` | list | `[]` | Tag taxonomy terms |
| `categories` | list | `[]` | Category taxonomy terms |
| `author` | string | — | Author name (cascades from _data.yaml) |
| `draft` | boolean | `false` | Exclude from production builds |
| `layout` | string | Auto-resolved | Template to use for rendering |
| `weight` | int | `0` | Manual sort order (lower = first) |
| `type` | string | `"page"` | Content type for typed collections |

### Markdown Extensions

Genesis extends standard GitHub Flavored Markdown with:

#### Callouts / Admonitions

```markdown
> [!NOTE] Good to know
> This renders as a styled info callout.

> [!WARNING] Breaking change
> This renders as a warning callout with amber styling.
```

**Rendered output:**
```html
<blockquote>
  <div class="callout callout-note">
    <p class="callout-title">Good to know</p>
    This renders as a styled info callout.
  </div>
</blockquote>
```

#### Footnotes

```markdown
Genesis supports footnotes[^1] for citations and references[^2].

[^1]: First footnote with detailed explanation.
[^2]: Second footnote with a link to source material.
```

Renders as clickable superscript numbers with a footnotes section at the bottom, including back-reference links (↩).

#### Auto-Generated Heading Anchors

Every heading automatically receives:
- A slugified `id` attribute for deep linking
- A clickable `#` anchor link

```markdown
## Getting Started
```
Becomes:
```html
<h2 id="getting-started"><a class="anchor" href="#getting-started">#</a>Getting Started</h2>
```

#### Table of Contents

Headings are automatically extracted into a `tableOfContents` list available in templates:

```html
{% for entry in page.tableOfContents %}
  <a href="#{{ entry.id }}" class="toc-{{ entry.level }}">{{ entry.text }}</a>
{% endfor %}
```

#### Additional GFM Features

- **Tables** — standard pipe-delimited tables
- **Task lists** — `- [x] Done` / `- [ ] Todo`
- **Fenced code blocks** — with language class for syntax highlighting
- **Strikethrough** — `~~deleted text~~`

---

## Data Cascade

Genesis implements Eleventy-style data cascade where data merges from multiple sources with clear priority:

### Priority Order (lowest → highest)

| Priority | Source | Scope | Example |
|----------|--------|-------|---------|
| 1 (lowest) | `data/` directory | All pages | `data/site.yaml` → `{{ data.site.author }}` |
| 2 | `_data.yaml` in content dirs | Pages in that directory tree | `content/blog/_data.yaml` |
| 3 (highest) | Page frontmatter | Single page only | `author: "Override"` in frontmatter |

### Example

```yaml
# data/site.yaml (global — priority 1)
author: "Default Corp Author"
company: "Acme Inc"
theme: "light"
```

```yaml
# content/blog/_data.yaml (directory — priority 2)
author: "Blog Team"
layout: "post"
```

```markdown
---
# content/blog/special-post.md (page — priority 3)
title: "Special Post"
author: "Guest Writer"   # ← This wins!
---
```

Result: The page renders with `author = "Guest Writer"`, `layout = "post"` (inherited from directory), and `company = "Acme Inc"` (inherited from global).

---

## Templating

Genesis supports two template tiers that can coexist in the same project:

### Tier 1: Kotlin DSL Templates (`.genesis.kts`)

Type-safe HTML builders with full IDE autocomplete and compile-time safety. This is Genesis's signature differentiator — no other SSG offers this on the JVM.

```kotlin
import dev.genesis.templates.*

html {
    doctype()
    html("en") {
        head {
            title("${page.title} - ${site.title}")
            metaCharset()
            metaViewport()
            meta("description", page.description)
            link("stylesheet", "/css/style.css")
        }
        body {
            header {
                nav {
                    a("/") { text(site.title) }
                    a("/blog/") { text("Blog") }
                    a("/about/") { text("About") }
                }
            }
            main {
                article {
                    h1 { text(page.title) }
                    raw(content) // Rendered markdown HTML
                }
            }
            footer {
                p { text("Built with Genesis") }
            }
        }
    }
}
```

### Tier 2: Pebble Templates (`.peb`)

Jinja2-like syntax for content authors who prefer logic-light markup:

```html
{% extends "base.peb" %}

{% block content %}
<article>
    <header>
        <h1>{{ page.title }}</h1>
        {% if page.date %}<time datetime="{{ page.date }}">{{ page.date }}</time>{% endif %}
        {% if page.tags is not empty %}
        <div class="tags">
            {% for tag in page.tags %}<span class="tag">{{ tag }}</span>{% endfor %}
        </div>
        {% endif %}
        <span class="reading-time">{{ page.readingTime }} min read</span>
    </header>
    <div class="content">
        {{ content | raw }}
    </div>
</article>
{% endblock %}
```

### Template Variables

| Variable | Type | Description |
|----------|------|-------------|
| `content` | string | Rendered HTML of the page's markdown body |
| `page.title` | string | Page title |
| `page.date` | string | Publication date |
| `page.description` | string | Meta description |
| `page.tags` | list | Tag terms |
| `page.categories` | list | Category terms |
| `page.url` | string | Canonical URL path |
| `page.readingTime` | int | Estimated reading time in minutes |
| `page.wordCount` | int | Total word count |
| `page.tableOfContents` | list | Heading entries with `level`, `text`, `id` |
| `page.author` | string | Author (from data cascade) |
| `site.title` | string | Site title from config |
| `site.baseUrl` | string | Base URL |
| `site.description` | string | Site description |
| `site.language` | string | Language code |
| `site.pageCount` | int | Total number of pages |
| `data` | map | All global data from `data/` directory |
| `pages` | list | All pages in the site |

### Template Resolution Order

When rendering a page, Genesis resolves templates in this priority:

| Priority | Pattern | Example |
|----------|---------|---------|
| 1 (highest) | `{page.layout}.peb` | `layout: "post"` → `post.peb` |
| 2 | `{section}/single.peb` | `content/blog/...` → `blog/single.peb` |
| 3 | `single.peb` | Default single-page template |
| 4 (lowest) | `base.peb` | Fallback base template |

If no template is found, Genesis wraps content in a minimal HTML document.

---

## Islands Architecture

Genesis ships zero JavaScript by default. When you need interactivity, use islands — independently-hydrated components that load only when needed.

### Usage

In any markdown file or template:

```html
<island src="/js/counter.js" hydrate="visible">
    <span>0</span>  <!-- SSR fallback content -->
</island>
```

### Hydration Strategies

| Strategy | Behavior | Use Case |
|----------|----------|----------|
| `load` | Hydrate immediately on page load | Critical interactive elements (nav menus) |
| `idle` | Hydrate when browser is idle (`requestIdleCallback`) | Non-critical enhancements |
| `visible` | Hydrate when scrolled into view (`IntersectionObserver`) | Below-the-fold components |
| `client:only` | Skip SSR, render only on client | Components that can't be server-rendered |

### How It Works

1. During build, `<island>` tags are replaced with `<div data-island data-src="..." data-hydrate="...">` containers
2. A small (~500 byte) loader script is injected before `</body>`
3. The loader observes each island and loads its JS module at the right time
4. Non-island content ships with **zero JavaScript** — pure static HTML

### Generated Output

```html
<div id="island-0-counter" data-island data-src="/js/counter.js" data-hydrate="visible">
    <span>0</span>
</div>

<script>
(function() {
    document.querySelectorAll('[data-island]').forEach(function(el) {
        const strategy = el.dataset.hydrate;
        if (strategy === 'visible') {
            new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) { /* load module */ }
                });
            }).observe(el);
        }
    });
})();
</script>
```

---

## Plugin System

Genesis is designed for extensibility. The `genesis-plugin-api` module provides stable interfaces that plugins implement.

### Built-in Plugins

| Plugin | ID | Output Files | Description |
|--------|-----|-------------|-------------|
| **Sitemap** | `genesis-sitemap` | `sitemap.xml` | XML sitemap with all page URLs and lastmod dates |
| **RSS Feed** | `genesis-rss` | `feed.xml`, `atom.xml` | RSS 2.0 and Atom feeds (latest 20 posts) |
| **Robots.txt** | `genesis-robots` | `robots.txt` | Crawler directives with sitemap reference |
| **Reading Time** | `genesis-reading-time` | — | Calculates `readingTime` field (200 WPM) |
| **OG Images** | `genesis-og-image` | `og/*.png` | Generates 1200×630 social preview images |

### Writing a Custom Plugin

```kotlin
class YouTubePlugin : Plugin {
    override val id = "youtube-embeds"
    override val name = "YouTube Embed Shortcode"
    override val version = "1.0.0"

    override fun initialize(context: PluginContext) {
        context.registerShortcode(YouTubeShortcode())
    }
}

class YouTubeShortcode : ShortcodeHandler {
    override val name = "youtube"

    override fun render(params: Map<String, String>, body: String?, page: PageMetadata): String {
        val id = params["id"] ?: return "<!-- missing youtube id -->"
        return """
            <div class="video-container" style="position:relative;padding-bottom:56.25%;height:0">
                <iframe src="https://www.youtube.com/embed/$id"
                        style="position:absolute;top:0;left:0;width:100%;height:100%"
                        frameborder="0" allowfullscreen></iframe>
            </div>
        """.trimIndent()
    }
}
```

Usage in markdown: `{{< youtube id="dQw4w9WgXcQ" >}}`

### Extension Points

| Interface | Purpose | Phase |
|-----------|---------|-------|
| `ContentTransformer` | Modify raw markdown or rendered HTML | PRE_PARSE, POST_PARSE, POST_RENDER |
| `ShortcodeHandler` | Custom `{{< name >}}` shortcodes | During markdown preprocessing |
| `AssetProcessor` | Process CSS/JS/image files | During asset pipeline |
| `BuildLifecycleHook` | Hook into build start/complete/page render | Build lifecycle |

### Plugin Discovery

Plugins are discovered via two mechanisms:

1. **Java ServiceLoader** — For JAR-distributed plugins (add to classpath)
2. **Programmatic registration** — Built-in plugins registered automatically

---

## Taxonomies

Genesis automatically generates listing pages for configured taxonomies.

### Configuration

```yaml
taxonomies:
  tags:
    singular: "tag"
    plural: "tags"
  categories:
    singular: "category"
    plural: "categories"
```

### Generated Pages

For a site with posts tagged "kotlin", "web", "architecture":

| Generated Path | Content |
|---------------|---------|
| `/tags/kotlin/index.html` | List of all posts tagged "kotlin" |
| `/tags/web/index.html` | List of all posts tagged "web" |
| `/tags/kotlin/page/2/index.html` | Page 2 (if >10 posts) |
| `/categories/tutorial/index.html` | All tutorial posts |

### Pagination

Taxonomy listing pages are paginated at 10 posts per page with navigation:

```html
<nav class="pagination">
    <a href="/tags/kotlin/" class="prev">← Previous</a>
    <span>Page 2 of 3</span>
    <a href="/tags/kotlin/page/3/" class="next">Next →</a>
</nav>
```

---

## Search

Genesis builds a static search index at build time — no external service required.

### How It Works

1. During build, all pages are tokenized and indexed into an inverted index
2. The index is serialized to `search/index.json`
3. A lightweight vanilla JS widget (`search/search.js`, ~1KB) provides client-side search
4. Supports prefix matching across title, description, content, and tags

### Integration

```html
<script src="/search/search.js"></script>
<script>
    GenesisSearch.loadIndex().then(() => {
        const results = GenesisSearch.search("kotlin coroutines");
        results.forEach(r => console.log(r.title, r.slug));
    });
</script>
```

---

## CLI Commands

| Command | Description | Key Options |
|---------|-------------|-------------|
| `genesis new <name>` | Scaffold a new site | `--template blog\|docs\|portfolio` |
| `genesis build` | Production build | `--dir <path>`, `--minify` |
| `genesis serve` | Dev server with live reload | `--port`, `--host`, `--dir` |
| `genesis check` | Content validation | `--dir <path>` |

### `genesis new`

Creates a complete site with content, templates, CSS, and configuration:

```bash
$ genesis new my-blog --template blog
Creating new Genesis site: my-blog (template: blog)

✓ Site created successfully!

  cd my-blog
  genesis build    # Build the site
  genesis serve    # Start dev server with live reload
```

**Available templates:**

| Template | Includes |
|----------|----------|
| `blog` | Blog posts, about page, tag/category examples, full CSS |
| `docs` | Documentation structure with weighted ordering |
| `portfolio` | Project showcase with date-based sorting |

### `genesis build`

```bash
$ genesis build
Building site from: /path/to/my-site

✓ Build complete!
  Pages:      8
  Time:       479ms
  Output:     116 KB
  Directory:  /path/to/my-site/dist
```

### `genesis serve`

Starts a development server that:
- Serves built files from `dist/`
- Injects a WebSocket client for live reload
- Watches `content/`, `layouts/`, `static/`, `data/` for changes
- Rebuilds and refreshes the browser automatically (100ms debounce)

### `genesis check`

Validates your site content:

| Check | Description |
|-------|-------------|
| Missing titles | Pages without a `title` field |
| Broken internal links | `href="/..."` pointing to non-existent pages |
| Images without alt text | `<img>` tags missing accessibility attributes |
| Broken anchor links | `href="#..."` pointing to non-existent IDs |

```bash
$ genesis check
Checking site: /path/to/my-site

  ✗ Broken link: '/nonexistent/' in content/index.md
  ⚠ Image without alt text: '/images/photo.jpg' in content/blog/post.md

✗ Found 2 issue(s) across 8 pages
```

---

## Configuration Reference

`genesis.yaml` (also supports `genesis.toml`):

```yaml
# ─── Site Metadata ───────────────────────────────────────────
title: "My Site"                    # Site title (used in templates)
baseUrl: "https://example.com"      # Canonical base URL
description: "Site description"     # Meta description
language: "en"                      # HTML lang attribute

# ─── URL Style ───────────────────────────────────────────────
prettyUrls: true                    # /about/ instead of /about.html

# ─── Directories ─────────────────────────────────────────────
contentDir: "content"               # Markdown source files
outputDir: "dist"                   # Build output
layoutDir: "layouts"                # Template files
staticDir: "static"                 # Static assets (copied as-is)
dataDir: "data"                     # Global data files

# ─── Taxonomies ──────────────────────────────────────────────
taxonomies:
  tags:
    singular: "tag"
    plural: "tags"
  categories:
    singular: "category"
    plural: "categories"

# ─── Build Options ───────────────────────────────────────────
build:
  minify: false                     # Minify HTML/CSS/JS output
  fingerprint: true                 # Content-hash filenames (cache busting)
  parallel: true                    # Parallel page rendering
  incrementalCache: true            # SHA-256 incremental builds

# ─── Dev Server ──────────────────────────────────────────────
server:
  port: 8080                        # HTTP port
  host: "localhost"                 # Bind address
  liveReload: true                  # WebSocket auto-refresh

# ─── Internationalization ────────────────────────────────────
i18n:
  defaultLanguage: "en"
  languages: ["en"]                 # Add ["en", "es", "fr"] for multi-language
```

---

## Incremental Builds

Genesis tracks content changes using SHA-256 hashing to skip re-rendering unchanged pages.

### What's Tracked

| Input | Hash covers | If changed... |
|-------|-------------|---------------|
| Page content | Raw markdown body | Only that page re-renders |
| Page frontmatter | Serialized key-value pairs | Only that page re-renders |
| Layout files | All files in `layouts/` | ALL pages re-render |
| Global data | All files in `data/` | ALL pages re-render |

### Cache Location

```
.genesis/cache/manifest.json    # SHA-256 hashes per page
```

The cache is automatically invalidated on layout or global data changes. Add `.genesis/` to your `.gitignore`.

---

## Building from Source

### Requirements

- JDK 17 or later
- Gradle 8.10+ (wrapper included)

### Build

```bash
git clone https://github.com/AkulRaghav/genesis.git
cd genesis
./gradlew build                    # Compile + test all modules
./gradlew :genesis-cli:shadowJar   # Build fat JAR (genesis.jar)
```

### Run from Source

```bash
./gradlew :genesis-cli:run --args="new my-test-site"
./gradlew :genesis-cli:run --args="build --dir my-test-site"
```

### Run from JAR

```bash
java -jar genesis-cli/build/libs/genesis.jar new my-site
java -jar genesis-cli/build/libs/genesis.jar build --dir my-site
java -jar genesis-cli/build/libs/genesis.jar serve --dir my-site
java -jar genesis-cli/build/libs/genesis.jar check --dir my-site
```

### Test

```bash
./gradlew test                     # Run all 51 tests
./gradlew :genesis-core:test       # Run core module tests only
```

---

## Tech Stack

| Layer | Library | Why |
|-------|---------|-----|
| Language | Kotlin 2.0.21 (JVM 17) | Modern, concise, coroutine-native |
| Build tool | Gradle (Kotlin DSL) | Multi-module, incremental compilation |
| CLI | Clikt 5.0.2 | Type-safe options, subcommands, completions |
| Markdown | JetBrains/markdown 0.7.3 | Pure Kotlin, AST-based, extensible |
| Templates | Pebble 3.2.2 | Jinja2-like, Java-native, well-maintained |
| YAML | yamlkt 0.13.0 | kotlinx.serialization integration |
| Serialization | kotlinx.serialization 1.7.3 | Type-safe, multiplatform |
| Dev server | Ktor 3.0.2 (CIO) | Lightweight, coroutine-native |
| Concurrency | Kotlin Coroutines 1.9.0 | Structured concurrency, Dispatchers.Default |
| Testing | Kotest 5.9.1 + JUnit5 | Kotlin-idiomatic assertions |
| Distribution | Shadow 8.3.5 | Fat JAR packaging |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Plugin API documentation with worked examples
- Code conventions and testing guidelines
- PR workflow

---

## License

Apache 2.0
