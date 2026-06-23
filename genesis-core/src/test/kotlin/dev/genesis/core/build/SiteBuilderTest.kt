package dev.genesis.core.build

import dev.genesis.api.SiteConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.comparables.shouldBeGreaterThan
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.*

class SiteBuilderTest : FunSpec({

    test("builds a simple site with markdown content") {
        val tempDir = Files.createTempDirectory("genesis-build-test")
        try {
            // Setup minimal site
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()
            contentDir.resolve("index.md").writeText("""
                ---
                title: "Home"
                ---
                
                # Welcome
                
                This is the home page.
            """.trimIndent())

            contentDir.resolve("about.md").writeText("""
                ---
                title: "About"
                description: "About page"
                ---
                
                # About
                
                About this site.
            """.trimIndent())

            // Config
            tempDir.resolve("genesis.yaml").writeText("""
                title: "Test Site"
                baseUrl: "http://localhost:8080"
            """.trimIndent())

            val builder = SiteBuilder(tempDir)
            val result = runBlocking { builder.build(forceClean = true) }

            result.pageCount shouldBe 2
            result.buildTimeMs shouldBeGreaterThan 0

            // Check output files exist
            val outputDir = tempDir.resolve("dist")
            outputDir.resolve("index.html").exists() shouldBe true
            outputDir.resolve("about/index.html").exists() shouldBe true

            // Check content rendered
            val homeHtml = outputDir.resolve("index.html").readText()
            homeHtml.contains("Welcome") shouldBe true
            homeHtml.contains("<h1") shouldBe true
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("filters out draft pages") {
        val tempDir = Files.createTempDirectory("genesis-build-test")
        try {
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()

            contentDir.resolve("published.md").writeText("""
                ---
                title: "Published"
                draft: false
                ---
                
                Published content.
            """.trimIndent())

            contentDir.resolve("draft.md").writeText("""
                ---
                title: "Draft"
                draft: true
                ---
                
                Draft content.
            """.trimIndent())

            val builder = SiteBuilder(tempDir)
            val result = runBlocking { builder.build(forceClean = true) }

            result.pageCount shouldBe 1
            tempDir.resolve("dist/published/index.html").exists() shouldBe true
            tempDir.resolve("dist/draft/index.html").exists() shouldBe false
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("generates pretty URLs by default") {
        val tempDir = Files.createTempDirectory("genesis-build-test")
        try {
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()
            contentDir.resolve("about.md").writeText("""
                ---
                title: "About"
                ---
                
                About page.
            """.trimIndent())

            val builder = SiteBuilder(tempDir)
            runBlocking { builder.build() }

            // Pretty URL: /about/ → about/index.html
            tempDir.resolve("dist/about/index.html").exists() shouldBe true
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("copies static assets to output") {
        val tempDir = Files.createTempDirectory("genesis-build-test")
        try {
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()
            contentDir.resolve("index.md").writeText("---\ntitle: Home\n---\n# Home")

            val staticDir = tempDir.resolve("static")
            val cssDir = staticDir.resolve("css")
            cssDir.createDirectories()
            cssDir.resolve("style.css").writeText("body { color: red; }")

            val builder = SiteBuilder(tempDir)
            runBlocking { builder.build() }

            tempDir.resolve("dist/css/style.css").exists() shouldBe true
            tempDir.resolve("dist/css/style.css").readText() shouldBe "body { color: red; }"
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("handles empty content directory") {
        val tempDir = Files.createTempDirectory("genesis-build-test")
        try {
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()

            val builder = SiteBuilder(tempDir)
            val result = runBlocking { builder.build(forceClean = true) }

            result.pageCount shouldBe 0
            result.errors.size shouldBe 0
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})

