package dev.genesis.core.build

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.*

class LargeBenchmarkTest : FunSpec({

    // Tag as slow test - can be excluded from normal test runs
    tags(io.kotest.core.Tag("benchmark"))

    test("benchmark 1000-page site - cold build") {
        val tempDir = Files.createTempDirectory("genesis-bench-1000")
        try {
            BenchmarkSiteGenerator.generate(tempDir, 1000)

            val builder = SiteBuilder(tempDir)
            val cold = runBlocking { builder.build(forceClean = true) }

            // Incremental build (no changes)
            val builder2 = SiteBuilder(tempDir)
            val warm = runBlocking { builder2.build() }

            // Change 1 file
            val postFile = tempDir.resolve("content/blog/post-500.md")
            postFile.writeText(postFile.readText() + "\n\n## Updated\nNew content.")
            val builder3 = SiteBuilder(tempDir)
            val incremental = runBlocking { builder3.build() }

            println("=== 1,000-PAGE BENCHMARK ===")
            println("Machine: ${System.getProperty("os.name")} ${System.getProperty("os.arch")}")
            println("CPUs:    ${Runtime.getRuntime().availableProcessors()}")
            println("Memory:  ${Runtime.getRuntime().maxMemory() / 1024 / 1024}MB max heap")
            println("")
            println("Cold build:        ${cold.pageCount} pages in ${cold.buildTimeMs}ms")
            println("Warm build:        ${warm.pageCount} pages in ${warm.buildTimeMs}ms")
            println("Incremental (1):   ${incremental.pageCount} pages in ${incremental.buildTimeMs}ms")
            println("Output size:       ${cold.outputSize / 1024}KB")
            println("============================")

            (cold.pageCount > 1000) shouldBe true
            cold.errors.size shouldBe 0
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})


