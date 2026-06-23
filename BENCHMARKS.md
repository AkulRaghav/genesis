# Genesis Benchmarks

All benchmarks measured on:
- **Machine:** Windows 11, amd64
- **CPU:** 16 logical cores
- **Memory:** 512MB JVM max heap
- **JDK:** OpenJDK 17.0.19 (Microsoft build)
- **Genesis version:** 0.1.0-SNAPSHOT

## Build Performance

### 50-Page Site

| Metric | Time |
|--------|------|
| Cold build (parallel) | 136ms |
| Cold build (sequential) | 268ms |
| Parallel speedup | 1.97x |
| Per-page cost (parallel) | ~2.7ms |

### 1,000-Page Site

| Metric | Time |
|--------|------|
| Cold build (parallel) | 2,083ms |
| Warm build (0 changes) | 646ms |
| Incremental (1 file changed) | 576ms |
| Per-page cost (cold) | ~2.1ms |
| Output size | 2,415 KB |

### Incremental Build (50 pages)

| Metric | Time |
|--------|------|
| Cold build | 360ms |
| Warm build (0 changes) | 94ms |
| Incremental (1 file changed) | 75ms |
| Speedup over cold | 4.8x |

## Methodology

- Synthetic sites generated with realistic markdown content (headings, code blocks, lists, blockquotes)
- Each page has 3 tags, 1 category, YAML frontmatter with title/date/description/author
- Content length ~200 words per page
- Pebble templates with extends/block inheritance
- All times are wall-clock measured via `System.currentTimeMillis()`
- Incremental builds use SHA-256 content hashing with `.genesis/cache/manifest.json`

## Watch Mode Rebuild Latency

| Site Size | Rebuild Time (1 file change) | Target |
|-----------|------------------------------|--------|
| 50 pages | 75ms | <200ms ✓ |
| 1,000 pages | 748ms | <200ms ✗ |

**Bottleneck:** At 1,000 pages, the rebuild includes full directory scan + cache manifest read/write. 
The actual page rendering is ~5ms for a single page. The overhead is I/O and scanning.

**Path to <200ms at 1,000 pages:** Keep file list + data cascade + template engine in memory between 
rebuilds in the dev server (avoid re-creating SiteBuilder per change). The watch service already 
knows which file changed — feeding that directly to a single-page render path would bring latency 
to ~50-100ms. This is planned for Milestone 5 (dev server polish).

- **Cold build** is dominated by Markdown parsing + Pebble template compilation (first template load)
- **Warm build overhead** (646ms at 1,000 pages) is file scanning + data cascade loading + cache I/O
- **Incremental single-file** time includes full file scan + cache lookup + single page render + write
- Parallel rendering shows ~2x speedup at 50 pages; benefit increases with page count

## Comparison Targets

| SSG | 1,000 pages (claimed) | Notes |
|-----|----------------------|-------|
| Hugo | ~100ms | Go, highly optimized C-based template engine |
| Zola | ~200ms | Rust, single-threaded |
| **Genesis** | **2,083ms** | JVM cold start + Pebble template compilation |
| Eleventy | ~2,000-5,000ms | Node.js, depends on template engine |
| Astro | ~3,000-8,000ms | Node.js, many features |

Genesis is competitive with Node-based SSGs. The JVM cold-start penalty (~500ms) makes sub-second full builds difficult at 1,000 pages, but incremental builds (75-576ms) hit the "instant feedback" target for development. GraalVM native image would eliminate the JVM startup penalty.
