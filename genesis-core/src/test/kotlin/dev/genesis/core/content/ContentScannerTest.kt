package dev.genesis.core.content

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldBeEmpty
import java.nio.file.Files
import kotlin.io.path.*

class ContentScannerTest : FunSpec({

    test("scans markdown files from content directory") {
        val tempDir = Files.createTempDirectory("genesis-test")
        try {
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()
            contentDir.resolve("index.md").writeText("# Home")
            contentDir.resolve("about.md").writeText("# About")

            val blog = contentDir.resolve("blog")
            blog.createDirectories()
            blog.resolve("first-post.md").writeText("# Post")

            val scanner = ContentScanner(contentDir)
            val files = scanner.scan()

            files shouldHaveSize 3
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("returns empty list for non-existent directory") {
        val nonExistent = Path("non-existent-dir-12345")
        val scanner = ContentScanner(nonExistent)
        scanner.scan().shouldBeEmpty()
    }

    test("computes slug from file path") {
        val tempDir = Files.createTempDirectory("genesis-test")
        try {
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()

            contentDir.resolve("about.md").writeText("# About")
            val blog = contentDir.resolve("blog")
            blog.createDirectories()
            blog.resolve("my-post.md").writeText("# Post")
            blog.resolve("index.md").writeText("# Blog")

            val scanner = ContentScanner(contentDir)
            val files = scanner.scan().sortedBy { it.slug }

            val aboutFile = files.find { it.slug == "about" }
            aboutFile?.slug shouldBe "about"

            val postFile = files.find { it.slug == "blog/my-post" }
            postFile?.slug shouldBe "blog/my-post"

            val indexFile = files.find { it.slug == "blog" || it.slug == "" }
            indexFile?.slug shouldBe "blog"
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("computes section from path") {
        val tempDir = Files.createTempDirectory("genesis-test")
        try {
            val contentDir = tempDir.resolve("content")
            val blog = contentDir.resolve("blog")
            blog.createDirectories()
            blog.resolve("post.md").writeText("# Post")

            contentDir.resolve("root-page.md").writeText("# Root")

            val scanner = ContentScanner(contentDir)
            val files = scanner.scan()

            val blogFile = files.find { it.section == "blog" }
            blogFile?.section shouldBe "blog"

            val rootFile = files.find { it.section == "" }
            rootFile?.section shouldBe ""
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    test("ignores non-content extensions") {
        val tempDir = Files.createTempDirectory("genesis-test")
        try {
            val contentDir = tempDir.resolve("content")
            contentDir.createDirectories()
            contentDir.resolve("valid.md").writeText("# Valid")
            contentDir.resolve("image.png").writeBytes(ByteArray(10))
            contentDir.resolve("notes.txt").writeText("Notes")

            val scanner = ContentScanner(contentDir)
            val files = scanner.scan()

            files shouldHaveSize 1
            files[0].extension shouldBe "md"
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})
