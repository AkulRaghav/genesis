package dev.genesis.islands

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.collections.shouldHaveSize

class IslandProcessorTest : FunSpec({
    val processor = IslandProcessor()

    test("extracts islands from HTML") {
        val html = """
            <div>
                <island src="/js/counter.js" hydrate="visible">
                    <span>0</span>
                </island>
            </div>
        """.trimIndent()

        val islands = processor.extractIslands(html)
        islands shouldHaveSize 1
        islands[0].src shouldBe "/js/counter.js"
        islands[0].hydration shouldBe IslandProcessor.HydrationStrategy.VISIBLE
    }

    test("processes multiple islands") {
        val html = """
            <island src="/js/nav.js" hydrate="load">Nav</island>
            <island src="/js/comments.js" hydrate="idle">Comments</island>
        """.trimIndent()

        val islands = processor.extractIslands(html)
        islands shouldHaveSize 2
        islands[0].hydration shouldBe IslandProcessor.HydrationStrategy.LOAD
        islands[1].hydration shouldBe IslandProcessor.HydrationStrategy.IDLE
    }

    test("replaces island tags with hydration containers") {
        val html = """
            <body>
            <island src="/js/counter.js" hydrate="visible"><span>0</span></island>
            </body>
        """.trimIndent()

        val result = processor.processHtml(html)
        result shouldContain "data-island"
        result shouldContain """data-src="/js/counter.js""""
        result shouldContain """data-hydrate="visible""""
        result shouldContain "IntersectionObserver"
    }

    test("injects loader script") {
        val html = """
            <body>
            <island src="/js/widget.js" hydrate="load">Widget</island>
            </body>
        """.trimIndent()

        val result = processor.processHtml(html)
        result shouldContain "<script>"
        result shouldContain "hydrateIsland"
    }

    test("handles default hydration strategy") {
        val html = """<island src="/js/widget.js" hydrate="">Content</island>"""
        val islands = processor.extractIslands(html)
        islands[0].hydration shouldBe IslandProcessor.HydrationStrategy.VISIBLE
    }
})
