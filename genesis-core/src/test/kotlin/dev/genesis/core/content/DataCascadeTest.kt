package dev.genesis.core.content

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

class DataCascadeTest : FunSpec({

    test("loads global data from data directory") {
        val tempDir = Files.createTempDirectory("genesis-test")
        try {
            val dataDir = tempDir.resolve("data")
            dataDir.createDirectories()
            dataDir.resolve("site.yaml").writeText("""
                author: "John Doe"
                email: "john@example.com"
            """.trimIndent())

            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()

            val cascade = DataCascade(dataDir, contentDir)
            cascade.load()

            val globalData = cascade.getGlobalData()
            globalData.containsKey("site") shouldBe true
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("merges directory _data.yaml into page data") {
        val tempDir = Files.createTempDirectory("genesis-test")
        try {
            val dataDir = tempDir.resolve("data")
            dataDir.createDirectories()

            val contentDir = tempDir.resolve("content")
            val blogDir = contentDir.resolve("blog")
            blogDir.createDirectories()

            blogDir.resolve("_data.yaml").writeText("""
                layout: "post"
                author: "Blog Author"
            """.trimIndent())

            val cascade = DataCascade(dataDir, contentDir)
            cascade.load()

            val pageData = cascade.resolveFor(
                Path.of("blog/my-post.md"),
                mapOf("title" to "My Post")
            )

            pageData["layout"] shouldBe "post"
            pageData["author"] shouldBe "Blog Author"
            pageData["title"] shouldBe "My Post"
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("page frontmatter overrides directory data") {
        val tempDir = Files.createTempDirectory("genesis-test")
        try {
            val dataDir = tempDir.resolve("data")
            dataDir.createDirectories()

            val contentDir = tempDir.resolve("content")
            val blogDir = contentDir.resolve("blog")
            blogDir.createDirectories()

            blogDir.resolve("_data.yaml").writeText("""
                layout: "post"
                author: "Default Author"
            """.trimIndent())

            val cascade = DataCascade(dataDir, contentDir)
            cascade.load()

            val pageData = cascade.resolveFor(
                Path.of("blog/my-post.md"),
                mapOf("title" to "Custom", "author" to "Custom Author")
            )

            pageData["author"] shouldBe "Custom Author"
            pageData["layout"] shouldBe "post"
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("handles missing data directory gracefully") {
        val tempDir = Files.createTempDirectory("genesis-test")
        try {
            val dataDir = tempDir.resolve("nonexistent-data")
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()

            val cascade = DataCascade(dataDir, contentDir)
            cascade.load()

            val data = cascade.resolveFor(Path.of("page.md"), mapOf("title" to "Test"))
            data["title"] shouldBe "Test"
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})
