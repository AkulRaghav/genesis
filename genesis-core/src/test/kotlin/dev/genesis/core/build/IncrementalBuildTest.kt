package dev.genesis.core.build

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.*

class IncrementalBuildTest : FunSpec({

    test("incremental build skips unchanged pages") {
        val tempDir = Files.createTempDirectory("genesis-incremental-test")
        try {
            BenchmarkSiteGenerator.generate(tempDir, 50)

            // First (cold) build
            val builder1 = SiteBuilder(tempDir)
            val cold = runBlocking { builder1.build() }
            println("Cold build: ${cold.pageCount} pages in ${cold.buildTimeMs}ms")

            // Second (warm/incremental) build — nothing changed
            val builder2 = SiteBuilder(tempDir)
            val warm = runBlocking { builder2.build() }
            println("Warm build (no changes): ${warm.pageCount} pages in ${warm.buildTimeMs}ms")

            // Warm build should render only taxonomy pages (content pages all cached)
            // Taxonomy pages have no sourcePath so they always rebuild
            println("Warm pageCount: ${warm.pageCount} (taxonomy pages)")
            (warm.pageCount < cold.pageCount) shouldBe true // Fewer than cold build

            // Now change ONE file
            val postFile = tempDir.resolve("content/blog/post-1.md")
            val original = postFile.readText()
            postFile.writeText(original + "\n\n## Updated Section\n\nNew content added.")

            // Third build — only the changed file + taxonomy pages should re-render
            val builder3 = SiteBuilder(tempDir)
            val incremental = runBlocking { builder3.build() }
            println("Incremental build (1 file changed): ${incremental.pageCount} pages in ${incremental.buildTimeMs}ms")

            // Should rebuild the changed page + taxonomy pages
            (incremental.pageCount >= 1) shouldBe true
            (incremental.pageCount < cold.pageCount) shouldBe true

            // Incremental should be much faster than cold
            incremental.buildTimeMs shouldBeLessThan cold.buildTimeMs

            println("=== INCREMENTAL BUILD RESULTS ===")
            println("Cold (51 pages):     ${cold.buildTimeMs}ms")
            println("Warm (0 changes):    ${warm.buildTimeMs}ms")
            println("Incremental (1 change): ${incremental.buildTimeMs}ms")
            println("Speedup over cold:   ${"%.1f".format(cold.buildTimeMs.toDouble() / incremental.buildTimeMs)}x")
            println("=================================")
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})


