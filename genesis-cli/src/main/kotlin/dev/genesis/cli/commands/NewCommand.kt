package dev.genesis.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Scaffolds a new Genesis site project.
 * Usage: genesis new my-site [--template blog|docs|portfolio]
 */
class NewCommand : CliktCommand(name = "new") {
    override fun help(context: com.github.ajalt.clikt.core.Context) = "Create a new Genesis site project."
    private val projectName by argument(help = "Name of the new project")
    private val template by option("--template", "-t", help = "Starter template: blog, docs, portfolio")
        .default("blog")

    override fun run() {
        val projectDir = Path(projectName)

        if (projectDir.exists()) {
            echo("Error: Directory '$projectName' already exists.", err = true)
            return
        }

        echo("Creating new Genesis site: $projectName (template: $template)")

        // Create directory structure
        createDirectoryStructure(projectDir)

        // Create config file
        createConfig(projectDir)

        // Create starter content
        when (template) {
            "blog" -> createBlogStarter(projectDir)
            "docs" -> createDocsStarter(projectDir)
            "portfolio" -> createPortfolioStarter(projectDir)
            else -> createBlogStarter(projectDir)
        }

        // Create default layout
        createDefaultLayouts(projectDir)

        echo("")
        echo("✓ Site created successfully!")
        echo("")
        echo("  cd $projectName")
        echo("  genesis build    # Build the site")
        echo("  genesis serve    # Start dev server with live reload")
        echo("")
    }

    private fun createDirectoryStructure(root: Path) {
        listOf(
            "content",
            "content/blog",
            "layouts",
            "layouts/partials",
            "static",
            "static/css",
            "static/js",
            "data",
        ).forEach { dir ->
            root.resolve(dir).createDirectories()
        }
    }

    private fun createConfig(root: Path) {
        root.resolve("genesis.yaml").writeText("""
title: "$projectName"
baseUrl: "http://localhost:8080"
description: "A site built with Genesis"
language: "en"
prettyUrls: true

contentDir: "content"
outputDir: "dist"
layoutDir: "layouts"
staticDir: "static"
dataDir: "data"

taxonomies:
  tags:
    singular: "tag"
    plural: "tags"
  categories:
    singular: "category"
    plural: "categories"

build:
  minify: false
  fingerprint: true
  parallel: true

server:
  port: 8080
  host: "localhost"
  liveReload: true
""".trimIndent())
    }

    private fun createBlogStarter(root: Path) {
        root.resolve("content/index.md").writeText("""
---
title: "Welcome to $projectName"
description: "A blog built with Genesis"
layout: "home"
---

# Welcome to $projectName

This site is built with [Genesis](https://github.com/genesis-ssg/genesis), a modern static site generator for the JVM.

## Recent Posts

Check out the [blog](/blog/) for the latest posts.
""".trimIndent())

        root.resolve("content/blog/first-post.md").writeText("""
---
title: "My First Post"
date: "2024-01-15"
description: "This is my first blog post using Genesis."
tags: ["genesis", "getting-started"]
categories: ["tutorial"]
---

# My First Post

Welcome to my blog! This is my first post written with Genesis.

## Features

Genesis supports:

- **Markdown** with extensions (tables, task lists, callouts)
- **YAML frontmatter** for typed metadata
- **Pebble templates** for layouts
- **Pretty URLs** by default
- **Fast builds** with parallel rendering

## Code Example

```kotlin
fun main() {
    println("Hello from Genesis!")
}
```

## What's Next

Stay tuned for more posts about Genesis and static site generation!
""".trimIndent())

        root.resolve("content/blog/second-post.md").writeText("""
---
title: "Getting Started with Genesis"
date: "2024-01-20"
description: "A guide to getting started with Genesis static site generator."
tags: ["genesis", "tutorial"]
categories: ["tutorial"]
draft: false
---

# Getting Started with Genesis

Genesis is a modern static site generator built for the JVM ecosystem.

## Installation

Download the latest release and add it to your PATH:

```bash
genesis new my-site
cd my-site
genesis serve
```

## Project Structure

A Genesis site has the following structure:

- `content/` — Your Markdown content files
- `layouts/` — Pebble template files
- `static/` — Static assets (CSS, JS, images)
- `data/` — Global data files (YAML, JSON)
- `genesis.yaml` — Site configuration

## Data Cascade

Genesis supports Eleventy-style data cascade. Data merges from:

1. Global `data/` files
2. Per-directory `_data.yaml` files
3. Page frontmatter (highest priority)

> [!NOTE]
> This is a callout! Genesis supports GitHub-style admonitions.
""".trimIndent())

        root.resolve("content/about.md").writeText("""
---
title: "About"
description: "About this site"
layout: "page"
---

# About

This is a site built with Genesis, a modern static site generator for the JVM.

Genesis combines:
- Hugo's speed and single-binary simplicity
- Astro's islands architecture
- Eleventy's template flexibility
""".trimIndent())
    }

    private fun createDocsStarter(root: Path) {
        root.resolve("content/index.md").writeText("""
---
title: "Documentation"
description: "Project documentation"
layout: "home"
---

# Documentation

Welcome to the project documentation.
""".trimIndent())

        root.resolve("content/docs").createDirectories()
        root.resolve("content/docs/getting-started.md").writeText("""
---
title: "Getting Started"
weight: 1
---

# Getting Started

Follow these steps to get started with the project.
""".trimIndent())
    }

    private fun createPortfolioStarter(root: Path) {
        root.resolve("content/index.md").writeText("""
---
title: "Portfolio"
description: "My portfolio"
layout: "home"
---

# Welcome

This is my portfolio site built with Genesis.
""".trimIndent())

        root.resolve("content/projects").createDirectories()
        root.resolve("content/projects/project-one.md").writeText("""
---
title: "Project One"
description: "My first project"
date: "2024-01-01"
tags: ["kotlin", "web"]
---

# Project One

Description of my first project.
""".trimIndent())
    }

    private fun createDefaultLayouts(root: Path) {
        root.resolve("layouts/base.peb").writeText("""
<!DOCTYPE html>
<html lang="{{ site.language | default('en') }}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{ page.title }} - {{ site.title }}</title>
    {% if page.description %}
    <meta name="description" content="{{ page.description }}">
    {% endif %}
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <header>
        <nav>
            <a href="/">{{ site.title }}</a>
            <a href="/blog/">Blog</a>
            <a href="/about/">About</a>
        </nav>
    </header>
    <main>
        {% block content %}
        {{ content | raw }}
        {% endblock %}
    </main>
    <footer>
        <p>Built with <a href="https://github.com/genesis-ssg/genesis">Genesis</a></p>
    </footer>
</body>
</html>
""".trimIndent())

        root.resolve("layouts/single.peb").writeText("""
{% extends "base.peb" %}

{% block content %}
<article>
    <header>
        <h1>{{ page.title }}</h1>
        {% if page.date %}
        <time>{{ page.date }}</time>
        {% endif %}
        {% if page.tags is not empty %}
        <div class="tags">
            {% for tag in page.tags %}
            <span class="tag">{{ tag }}</span>
            {% endfor %}
        </div>
        {% endif %}
        {% if page.readingTime %}
        <span class="reading-time">{{ page.readingTime }} min read</span>
        {% endif %}
    </header>
    <div class="content">
        {{ content | raw }}
    </div>
</article>
{% endblock %}
""".trimIndent())

        root.resolve("layouts/home.peb").writeText("""
{% extends "base.peb" %}

{% block content %}
<div class="home">
    {{ content | raw }}
</div>
{% endblock %}
""".trimIndent())

        root.resolve("layouts/page.peb").writeText("""
{% extends "base.peb" %}

{% block content %}
<article class="page">
    <h1>{{ page.title }}</h1>
    <div class="content">
        {{ content | raw }}
    </div>
</article>
{% endblock %}
""".trimIndent())

        // Default CSS
        root.resolve("static/css/style.css").writeText("""
:root {
    --font-sans: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    --font-mono: "JetBrains Mono", "Fira Code", monospace;
    --color-bg: #ffffff;
    --color-text: #1a1a1a;
    --color-accent: #2563eb;
    --color-muted: #6b7280;
    --color-border: #e5e7eb;
    --max-width: 720px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
    font-family: var(--font-sans);
    color: var(--color-text);
    background: var(--color-bg);
    line-height: 1.7;
    padding: 2rem 1rem;
    max-width: var(--max-width);
    margin: 0 auto;
}

header nav {
    display: flex;
    gap: 1.5rem;
    padding-bottom: 2rem;
    border-bottom: 1px solid var(--color-border);
    margin-bottom: 2rem;
}

header nav a {
    color: var(--color-text);
    text-decoration: none;
    font-weight: 500;
}

header nav a:first-child {
    font-weight: 700;
    margin-right: auto;
}

main { min-height: 60vh; }

article header { margin-bottom: 2rem; }
article header h1 { margin-bottom: 0.5rem; }
article header time { color: var(--color-muted); font-size: 0.9rem; }
article header .tags { margin-top: 0.5rem; }
article header .tag {
    display: inline-block;
    background: var(--color-border);
    padding: 0.2rem 0.6rem;
    border-radius: 3px;
    font-size: 0.8rem;
    margin-right: 0.3rem;
}

h1, h2, h3, h4, h5, h6 { margin-top: 2rem; margin-bottom: 0.75rem; line-height: 1.3; }
h1 { font-size: 2rem; }
h2 { font-size: 1.5rem; }
h3 { font-size: 1.25rem; }

p { margin-bottom: 1rem; }
a { color: var(--color-accent); }
code { font-family: var(--font-mono); font-size: 0.9em; background: #f3f4f6; padding: 0.15rem 0.4rem; border-radius: 3px; }
pre { background: #1e293b; color: #e2e8f0; padding: 1.25rem; border-radius: 6px; overflow-x: auto; margin: 1.5rem 0; }
pre code { background: none; padding: 0; color: inherit; }
blockquote { border-left: 3px solid var(--color-accent); padding-left: 1rem; margin: 1.5rem 0; color: var(--color-muted); }
ul, ol { margin: 1rem 0; padding-left: 1.5rem; }
li { margin-bottom: 0.3rem; }

.callout { border-left: 4px solid var(--color-accent); background: #eff6ff; padding: 1rem; margin: 1.5rem 0; border-radius: 0 6px 6px 0; }
.callout-warning { border-color: #f59e0b; background: #fffbeb; }
.callout-title { font-weight: 600; margin-bottom: 0.5rem; }

footer { margin-top: 4rem; padding-top: 2rem; border-top: 1px solid var(--color-border); color: var(--color-muted); font-size: 0.9rem; }

.reading-time { color: var(--color-muted); font-size: 0.85rem; }

@media (prefers-color-scheme: dark) {
    :root {
        --color-bg: #0f172a;
        --color-text: #e2e8f0;
        --color-border: #334155;
        --color-muted: #94a3b8;
    }
    code { background: #1e293b; }
    .callout { background: #1e293b; }
    .callout-warning { background: #1c1917; }
}
""".trimIndent())
    }
}
