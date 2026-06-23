package dev.genesis.core.build

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.*

class BenchmarkTest : FunSpec({

    test("benchmark 50-page site build") {
        val tempDir = Files.createTempDirectory("genesis-bench-50")
        try {
            BenchmarkSiteGenerator.generate(tempDir, 50)

            val builder = SiteBuilder(tempDir)
            val result = runBlocking { builder.build() }

            println("=== 50-PAGE BENCHMARK ===")
            println("Pages built: ${result.pageCount}")
            println("Build time:  ${result.buildTimeMs}ms")
            println("Output size: ${result.outputSize} bytes")
            println("Errors:      ${result.errors.size}")
            println("=========================")

            (result.pageCount > 50) shouldBe true // 50 posts + 1 index
            result.errors.size shouldBe 0
            result.buildTimeMs shouldBeGreaterThan 0
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})


