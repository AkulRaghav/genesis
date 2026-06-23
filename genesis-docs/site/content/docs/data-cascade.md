---
title: "Data Cascade"
description: "Eleventy-style data merging in Genesis"
weight: 12
---

# Data Cascade

Genesis implements Eleventy-style data cascade where data merges from multiple sources in priority order.

## Priority Order (lowest to highest)

1. **Global data** — Files in `data/` directory
2. **Directory data** — `_data.yaml` files in content directories
3. **Page frontmatter** — Frontmatter in individual content files

## Example

```yaml
# data/site.yaml
author: "Default Author"
company: "My Corp"
```

```yaml
# content/blog/_data.yaml
author: "Blog Team"
layout: "post"
```

```markdown
---
title: "My Post"
author: "John Doe"  # This wins!
---
```

In this example, blog posts use `layout: "post"` from directory data, and the specific post overrides `author` to "John Doe".

## Use in Templates

All cascaded data is available in templates:

```html
<span>{{ page.author }}</span>
<span>{{ page.company }}</span>
```
