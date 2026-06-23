package dev.genesis.core.build

import dev.genesis.api.SiteConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.io.path.*

class ParallelismTest : FunSpec({

    test("parallel vs sequential build comparison on 50-page site") {
        val tempDir = Files.createTempDirectory("genesis-parallel-test")
        try {
            BenchmarkSiteGenerator.generate(tempDir, 50)

            // Sequential build (force clean to ignore cache)
            val configFile = tempDir.resolve("genesis.yaml")
            configFile.writeText(configFile.readText().replace("parallel: true", "parallel: false"))
            val sequentialBuilder = SiteBuilder(tempDir)
            val seqResult = runBlocking { sequentialBuilder.build(forceClean = true) }

            // Clean output for parallel run
            tempDir.resolve("dist").toFile().deleteRecursively()
            tempDir.resolve(".genesis").toFile().deleteRecursively()

            // Parallel build (force clean to ignore cache)
            configFile.writeText(configFile.readText().replace("parallel: false", "parallel: true"))
            val parallelBuilder = SiteBuilder(tempDir)
            val parResult = runBlocking { parallelBuilder.build(forceClean = true) }

            println("=== PARALLEL VS SEQUENTIAL (50 pages) ===")
            println("Sequential: ${seqResult.buildTimeMs}ms")
            println("Parallel:   ${parResult.buildTimeMs}ms")
            println("Speedup:    ${"%.2f".format(seqResult.buildTimeMs.toDouble() / parResult.buildTimeMs)}x")
            println("Both built: ${seqResult.pageCount} pages")
            println("==========================================")

            (seqResult.pageCount > 50) shouldBe true
            (parResult.pageCount > 50) shouldBe true
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
})


