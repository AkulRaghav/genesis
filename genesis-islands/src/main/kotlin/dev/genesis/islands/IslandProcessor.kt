package dev.genesis.islands

/**
 * Processes island components in rendered HTML.
 * Islands are independently-hydrated interactive components that ship zero JS
 * by default — only loading client code when needed.
 *
 * Hydration strategies:
 * - load: Hydrate immediately on page load
 * - idle: Hydrate when browser is idle (requestIdleCallback)
 * - visible: Hydrate when scrolled into view (IntersectionObserver)
 * - client:only: Skip SSR, render only on client
 */
class IslandProcessor {

    data class Island(
        val id: String,
        val src: String,
        val hydration: HydrationStrategy,
        val props: Map<String, String> = emptyMap(),
        val ssrHtml: String = ""
    )

    enum class HydrationStrategy {
        LOAD, IDLE, VISIBLE, CLIENT_ONLY
    }

    /**
     * Extract island declarations from HTML content.
     */
    fun extractIslands(html: String): List<Island> {
        val regex = Regex(
            """<island\s+src="([^"]+)"(?:\s+hydrate="([^"]+)")?([^>]*)>(.*?)</island>""",
            RegexOption.DOT_MATCHES_ALL
        )

        return regex.findAll(html).mapIndexed { index, match ->
            val src = match.groupValues[1]
            val hydration = parseHydration(match.groupValues[2])
            val propsStr = match.groupValues[3]
            val ssrHtml = match.groupValues[4].trim()
            val props = parseProps(propsStr)

            Island(
                id = "island-$index-${src.substringAfterLast('/').substringBefore('.')}",
                src = src,
                hydration = hydration,
                props = props,
                ssrHtml = ssrHtml
            )
        }.toList()
    }

    /**
     * Replace island tags with hydration-ready containers.
     */
    fun processHtml(html: String): String {
        val islands = extractIslands(html)
        var result = html

        val regex = Regex(
            """<island\s+src="([^"]+)"(?:\s+hydrate="([^"]+)")?([^>]*)>(.*?)</island>""",
            RegexOption.DOT_MATCHES_ALL
        )

        var index = 0
        result = regex.replace(html) { match ->
            val island = islands[index++]
            buildIslandContainer(island)
        }

        // Append island loader script if there are islands
        if (islands.isNotEmpty()) {
            result = result.replace("</body>", "${buildIslandLoader(islands)}\n</body>")
        }

        return result
    }

    private fun buildIslandContainer(island: Island): String {
        val propsAttr = if (island.props.isNotEmpty()) {
            """ data-props='${island.props.entries.joinToString(",") { """"${it.key}":"${it.value}"""" }.let { "{$it}" }}'"""
        } else ""

        return """<div id="${island.id}" data-island data-src="${island.src}" data-hydrate="${island.hydration.name.lowercase()}"$propsAttr>${island.ssrHtml}</div>"""
    }

    private fun buildIslandLoader(islands: List<Island>): String {
        return """
<script>
(function() {
    function hydrateIsland(el) {
        const src = el.dataset.src;
        const script = document.createElement('script');
        script.src = src;
        script.type = 'module';
        document.head.appendChild(script);
    }
    
    document.querySelectorAll('[data-island]').forEach(function(el) {
        const strategy = el.dataset.hydrate;
        
        if (strategy === 'load') {
            hydrateIsland(el);
        } else if (strategy === 'idle') {
            if ('requestIdleCallback' in window) {
                requestIdleCallback(function() { hydrateIsland(el); });
            } else {
                setTimeout(function() { hydrateIsland(el); }, 200);
            }
        } else if (strategy === 'visible') {
            const observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        hydrateIsland(el);
                        observer.unobserve(el);
                    }
                });
            });
            observer.observe(el);
        }
    });
})();
</script>""".trimIndent()
    }

    private fun parseHydration(value: String): HydrationStrategy {
        return when (value.lowercase()) {
            "load" -> HydrationStrategy.LOAD
            "idle" -> HydrationStrategy.IDLE
            "visible" -> HydrationStrategy.VISIBLE
            "client:only", "client-only" -> HydrationStrategy.CLIENT_ONLY
            else -> HydrationStrategy.VISIBLE // Default to visible
        }
    }

    private fun parseProps(propsStr: String): Map<String, String> {
        val propRegex = Regex("""(\w+)="([^"]*)" """.trimEnd())
        return propRegex.findAll(propsStr)
            .filter { it.groupValues[1] !in setOf("src", "hydrate") }
            .associate { it.groupValues[1] to it.groupValues[2] }
    }
}
