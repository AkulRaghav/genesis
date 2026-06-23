---
title: "Configuration"
description: "Genesis site configuration reference"
weight: 5
---

# Configuration

Genesis is configured via `genesis.yaml` (or `genesis.toml`) at the project root.

## Full Reference

```yaml
# Site metadata
title: "My Site"
baseUrl: "https://example.com"
description: "Site description"
language: "en"

# URL style
prettyUrls: true  # /about/ instead of /about.html

# Directories
contentDir: "content"
outputDir: "dist"
layoutDir: "layouts"
staticDir: "static"
dataDir: "data"

# Taxonomies
taxonomies:
  tags:
    singular: "tag"
    plural: "tags"
  categories:
    singular: "category"
    plural: "categories"

# Build options
build:
  minify: false        # Minify HTML/CSS/JS output
  fingerprint: true    # Content-hash filenames for cache busting
  parallel: true       # Parallel page rendering
  incrementalCache: true  # SHA-256 based incremental builds

# Dev server
server:
  port: 8080
  host: "localhost"
  liveReload: true     # WebSocket-based live reload

# i18n
i18n:
  defaultLanguage: "en"
  languages: ["en"]
```
