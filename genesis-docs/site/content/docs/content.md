---
title: "Content Authoring"
description: "Writing content in Genesis with Markdown and frontmatter"
weight: 10
---

# Content Authoring

## Frontmatter

Every content file starts with YAML or TOML frontmatter:

```markdown
---
title: "My Post"
date: "2024-01-15"
tags: ["kotlin", "web"]
categories: ["tutorial"]
draft: false
---

Your content here...
```

## Supported Fields

| Field | Type | Description |
|-------|------|-------------|
| title | string | Page title |
| date | string | Publication date (YYYY-MM-DD) |
| description | string | Meta description |
| tags | list | Tag taxonomy terms |
| categories | list | Category taxonomy terms |
| layout | string | Template to use |
| draft | boolean | Exclude from production builds |
| weight | int | Manual sort order |

## Markdown Extensions

Genesis supports GitHub Flavored Markdown plus:

### Callouts

```markdown
> [!NOTE] Important information
> Content of the callout.

> [!WARNING] Be careful
> Warning content here.
```

### Footnotes

```markdown
This has a footnote[^1].

[^1]: The footnote content.
```

### Auto-generated heading anchors

All headings automatically get `id` attributes and anchor links for deep linking.

### Code blocks with syntax highlighting

```kotlin
fun main() {
    println("Hello from Genesis!")
}
```
