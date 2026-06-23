package dev.genesis.markdown

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.collections.shouldHaveSize

class MarkdownRendererTest : FunSpec({
    val renderer = MarkdownRenderer()

    test("renders basic markdown to HTML") {
        val result = renderer.render("# Hello World\n\nThis is a paragraph.")
        result.html shouldContain "Hello World"
        result.html shouldContain "<p>This is a paragraph.</p>"
    }

    test("renders headings with anchors") {
        val result = renderer.render("## Getting Started")
        result.html shouldContain """id="getting-started""""
        result.html shouldContain """href="#getting-started""""
    }

    test("extracts headings for TOC") {
        val source = """
            # First
            ## Second
            ### Third
        """.trimIndent()
        val result = renderer.render(source)
        result.headings shouldHaveSize 3
        result.headings[0].level shouldBe 1
        result.headings[0].text shouldBe "First"
        result.headings[1].level shouldBe 2
        result.headings[2].level shouldBe 3
    }

    test("renders code blocks") {
        val source = """
            ```kotlin
            fun main() {
                println("Hello")
            }
            ```
        """.trimIndent()
        val result = renderer.render(source)
        result.html shouldContain "<code"
        result.html shouldContain "println"
    }

    test("renders inline code") {
        val result = renderer.render("Use `genesis build` to build.")
        result.html shouldContain "<code>genesis build</code>"
    }

    test("renders links") {
        val result = renderer.render("[Genesis](https://genesis.dev)")
        result.html shouldContain """<a href="https://genesis.dev">Genesis</a>"""
    }

    test("renders bold and italic") {
        val result = renderer.render("**bold** and *italic*")
        result.html shouldContain "<strong>bold</strong>"
        result.html shouldContain "<em>italic</em>"
    }

    test("renders unordered lists") {
        val source = """
            - Item 1
            - Item 2
            - Item 3
        """.trimIndent()
        val result = renderer.render(source)
        result.html shouldContain "<ul>"
        result.html shouldContain "<li>"
    }

    test("renders callout syntax") {
        val source = "> [!NOTE] Important info"
        val result = renderer.render(source)
        result.html shouldContain "callout"
        result.html shouldContain "callout-note"
    }

    test("extracts footnotes") {
        val source = """
            Some text[^1].
            
            [^1]: This is a footnote.
        """.trimIndent()
        val result = renderer.render(source)
        result.footnotes shouldHaveSize 1
        result.footnotes[0].id shouldBe "1"
        result.footnotes[0].content shouldBe "This is a footnote."
    }

    test("slugify produces correct slugs") {
        MarkdownRenderer.slugify("Hello World") shouldBe "hello-world"
        MarkdownRenderer.slugify("Getting Started!") shouldBe "getting-started"
        MarkdownRenderer.slugify("Foo & Bar") shouldBe "foo-bar"
        MarkdownRenderer.slugify("Multiple   Spaces") shouldBe "multiple-spaces"
    }

    test("renders without anchors when disabled") {
        val config = MarkdownConfig(autoHeadingAnchors = false)
        val renderer = MarkdownRenderer(config)
        val result = renderer.render("## Test Heading")
        result.html shouldNotContain """class="anchor""""
    }
})
