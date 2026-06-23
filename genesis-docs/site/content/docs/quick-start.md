---
title: "Quick Start"
description: "Create your first Genesis site in under a minute"
weight: 2
---

# Quick Start

## Create a New Site

```bash
genesis new my-blog
cd my-blog
```

This creates a complete site with:
- Sample content in `content/`
- Pebble templates in `layouts/`
- Default CSS in `static/css/`
- Site configuration in `genesis.yaml`

## Build

```bash
genesis build
```

Output goes to `dist/` — clean, static HTML ready for any hosting platform.

## Development Server

```bash
genesis serve
```

Starts a dev server at http://localhost:8080 with live reload via WebSocket. Edit any content, layout, or CSS file and the browser refreshes automatically.

## Validate

```bash
genesis check
```

Checks for broken internal links, images missing alt text, and other common issues.
