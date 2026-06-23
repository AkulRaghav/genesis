package dev.genesis.core.content

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FrontmatterParserTest : FunSpec({
    val parser = FrontmatterParser()

    test("parses YAML frontmatter") {
        val content = """
            ---
            title: "My Post"
            date: "2024-01-15"
            draft: false
            ---
            
            # Hello World
        """.trimIndent()

        val result = parser.parse(content)
        result.frontmatter["title"] shouldBe "My Post"
        result.frontmatter["date"] shouldBe "2024-01-15"
        result.frontmatter["draft"] shouldBe false
        result.content shouldBe "# Hello World"
    }

    test("parses TOML frontmatter") {
        val content = """
            +++
            title = "My Post"
            date = "2024-01-15"
            draft = false
            +++
            
            # Hello World
        """.trimIndent()

        val result = parser.parse(content)
        result.frontmatter["title"] shouldBe "My Post"
        result.frontmatter["date"] shouldBe "2024-01-15"
        result.frontmatter["draft"] shouldBe false
        result.content shouldBe "# Hello World"
    }

    test("handles content without frontmatter") {
        val content = "# Just Content\n\nNo frontmatter here."
        val result = parser.parse(content)
        result.frontmatter.size shouldBe 0
        result.content shouldBe content
    }

    test("parses tags as list") {
        val content = """
            ---
            title: "Tagged Post"
            tags: [kotlin, web, genesis]
            ---
            
            Content here.
        """.trimIndent()

        val result = parser.parse(content)
        result.frontmatter.containsKey("tags") shouldBe true
        val tags = result.frontmatter["tags"]
        (tags is List<*>) shouldBe true
    }

    test("parses numeric values") {
        val content = """
            ---
            weight: 10
            rating: 4.5
            ---
            
            Content.
        """.trimIndent()

        val result = parser.parse(content)
        result.frontmatter["weight"] shouldBe 10
        result.frontmatter["rating"] shouldBe 4.5
    }

    test("parses boolean values") {
        val content = """
            ---
            draft: true
            published: false
            ---
            
            Content.
        """.trimIndent()

        val result = parser.parse(content)
        result.frontmatter["draft"] shouldBe true
        result.frontmatter["published"] shouldBe false
    }

    test("handles empty frontmatter") {
        val content = """
            ---
            ---
            
            # Just Content
        """.trimIndent()

        val result = parser.parse(content)
        result.frontmatter.size shouldBe 0
        result.content shouldBe "# Just Content"
    }

    test("handles unclosed frontmatter gracefully") {
        val content = """
            ---
            title: "Unclosed"
            
            # Content without closing delimiter
        """.trimIndent()

        val result = parser.parse(content)
        // Should return empty frontmatter and original content
        result.frontmatter.size shouldBe 0
    }
})

