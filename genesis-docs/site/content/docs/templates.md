---
title: "Templating"
description: "Genesis template system with Pebble and Kotlin DSL"
weight: 11
---

# Templating

Genesis supports two template tiers:

## Tier 2: Pebble Templates (.peb)

Logic-light templates using Jinja2-like syntax:

```html
{% extends "base.peb" %}

{% block content %}
<article>
    <h1>{{ page.title }}</h1>
    {% if page.date %}<time>{{ page.date }}</time>{% endif %}
    {{ content | raw }}
</article>
{% endblock %}
```

### Available Variables

| Variable | Description |
|----------|-------------|
| `content` | Rendered HTML content |
| `page.title` | Page title |
| `page.date` | Publication date |
| `page.tags` | List of tags |
| `page.url` | Page URL |
| `page.readingTime` | Estimated reading time |
| `site.title` | Site title |
| `site.baseUrl` | Base URL |
| `data` | Global data from data/ directory |

### Template Resolution

Templates are resolved in this order:
1. `page.layout` field (e.g., `layout: "post"` → `post.peb`)
2. `{section}/single.peb` (e.g., `blog/single.peb`)
3. `single.peb`
4. `base.peb`

## Tier 1: Kotlin DSL (.genesis.kts)

Type-safe HTML builders for power users:

```kotlin
html {
    doctype()
    html("en") {
        head { title(page.title); metaCharset() }
        body { main { raw(content) } }
    }
}
```
