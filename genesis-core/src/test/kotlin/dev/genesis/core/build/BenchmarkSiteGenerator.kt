package dev.genesis.core.build

import java.nio.file.Path
import kotlin.io.path.*

/**
 * Generates synthetic test sites for benchmarking.
 */
object BenchmarkSiteGenerator {

    private val tags = listOf("kotlin", "java", "web", "performance", "architecture", "testing", "devops", "cloud", "frontend", "backend")
    private val categories = listOf("tutorial", "guide", "opinion", "deep-dive", "announcement")

    /**
     * Generate a synthetic site with the given number of pages.
     */
    fun generate(siteRoot: Path, pageCount: Int) {
        val contentDir = siteRoot.resolve("content")
        val blogDir = contentDir.resolve("blog")
        blogDir.createDirectories()

        // Config
        siteRoot.resolve("genesis.yaml").writeText("""
title: "Benchmark Site"
baseUrl: "http://localhost:8080"
description: "A synthetic site for benchmarking"
language: "en"
prettyUrls: true
build:
  parallel: true
""".trimIndent())

        // Generate pages
        for (i in 1..pageCount) {
            val pageTags = tags.shuffled().take(3)
            val pageCategory = categories[i % categories.size]
            val date = "2024-%02d-%02d".format((i % 12) + 1, (i % 28) + 1)

            val content = buildString {
                appendLine("---")
                appendLine("title: \"Benchmark Post $i: ${generateTitle(i)}\"")
                appendLine("date: \"$date\"")
                appendLine("description: \"This is benchmark post number $i for testing build performance.\"")
                appendLine("tags: [${pageTags.joinToString(", ") { "\"$it\"" }}]")
                appendLine("categories: [\"$pageCategory\"]")
                appendLine("author: \"Author ${i % 5}\"")
                appendLine("---")
                appendLine()
                appendLine("# ${generateTitle(i)}")
                appendLine()
                appendLine(generateParagraph(i, 1))
                appendLine()
                appendLine("## Section One")
                appendLine()
                appendLine(generateParagraph(i, 2))
                appendLine()
                appendLine("### Subsection")
                appendLine()
                appendLine(generateParagraph(i, 3))
                appendLine()
                appendLine("## Code Example")
                appendLine()
                appendLine("```kotlin")
                appendLine("fun example$i() {")
                appendLine("    val items = listOf(${(1..5).joinToString(", ") { "\"item$it\"" }})")
                appendLine("    items.forEach { println(it) }")
                appendLine("}")
                appendLine("```")
                appendLine()
                appendLine("## Conclusion")
                appendLine()
                appendLine(generateParagraph(i, 4))
                appendLine()
                appendLine("- First point about topic $i")
                appendLine("- Second point with **bold emphasis**")
                appendLine("- Third point with [a link](https://example.com/$i)")
                appendLine()
                appendLine("> This is a blockquote for post $i.")
            }

            blogDir.resolve("post-$i.md").writeText(content)
        }

        // Home page
        contentDir.resolve("index.md").writeText("""
---
title: "Benchmark Site Home"
---

# Welcome to the Benchmark Site

This site has $pageCount posts for testing build performance.
""".trimIndent())
    }

    private fun generateTitle(seed: Int): String {
        val adjectives = listOf("Advanced", "Modern", "Essential", "Complete", "Practical", "Efficient", "Scalable", "Robust", "Elegant", "Fast")
        val nouns = listOf("Architecture", "Patterns", "Techniques", "Strategies", "Solutions", "Approaches", "Methods", "Practices", "Principles", "Frameworks")
        return "${adjectives[seed % adjectives.size]} ${nouns[(seed * 7) % nouns.size]}"
    }

    private fun generateParagraph(seed: Int, section: Int): String {
        val sentences = listOf(
            "This is an important consideration when building production systems.",
            "Performance matters especially in high-throughput environments.",
            "The key insight is that simplicity often beats complexity in practice.",
            "Teams that adopt this approach report significant productivity gains.",
            "It's worth noting that the tradeoffs vary depending on your specific context.",
            "Research shows that this technique reduces overall system complexity.",
            "In practice, the benefits compound over time as the codebase grows.",
            "The fundamental principle here is separation of concerns.",
            "Measurement is critical — never optimize without benchmarking first.",
            "This approach has been battle-tested in production at scale."
        )
        val start = (seed * section) % sentences.size
        return (0..3).joinToString(" ") { sentences[(start + it) % sentences.size] }
    }
}
