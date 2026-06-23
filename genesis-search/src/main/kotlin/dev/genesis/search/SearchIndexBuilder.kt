package dev.genesis.search

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * Builds a static search index at build time.
 * Creates an inverted index serialized to JSON, paired with a lightweight
 * vanilla JS search widget.
 */
class SearchIndexBuilder {

    @Serializable
    data class SearchDocument(
        val slug: String,
        val title: String,
        val description: String = "",
        val content: String,
        val section: String = "",
        val tags: List<String> = emptyList()
    )

    @Serializable
    data class SearchIndex(
        val documents: List<IndexedDocument>,
        val invertedIndex: Map<String, List<Int>>
    )

    @Serializable
    data class IndexedDocument(
        val id: Int,
        val slug: String,
        val title: String,
        val description: String,
        val section: String
    )

    private val documents = mutableListOf<SearchDocument>()

    fun addDocument(doc: SearchDocument) {
        documents.add(doc)
    }

    /**
     * Build the search index and write to the output directory.
     */
    fun build(outputDir: Path) {
        val invertedIndex = mutableMapOf<String, MutableList<Int>>()
        val indexedDocs = documents.mapIndexed { index, doc ->
            // Tokenize and index
            val tokens = tokenize("${doc.title} ${doc.description} ${doc.content}")
            for (token in tokens) {
                invertedIndex.getOrPut(token) { mutableListOf() }.add(index)
            }

            IndexedDocument(
                id = index,
                slug = doc.slug,
                title = doc.title,
                description = doc.description,
                section = doc.section
            )
        }

        val index = SearchIndex(
            documents = indexedDocs,
            invertedIndex = invertedIndex
        )

        val searchDir = outputDir.resolve("search")
        searchDir.createDirectories()

        // Write index JSON
        val json = Json { prettyPrint = false }
        searchDir.resolve("index.json").writeText(json.encodeToString(index))

        // Write search widget JS
        searchDir.resolve("search.js").writeText(SEARCH_WIDGET_JS)
    }

    private fun tokenize(text: String): Set<String> {
        return text.lowercase()
            .replace(Regex("<[^>]*>"), " ") // strip HTML
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }
            .toSet()
    }

    companion object {
        private val SEARCH_WIDGET_JS = """
            (function() {
              let index = null;
              
              async function loadIndex() {
                if (index) return index;
                const resp = await fetch('/search/index.json');
                index = await resp.json();
                return index;
              }
              
              function search(query) {
                if (!index) return [];
                const tokens = query.toLowerCase().split(/\s+/).filter(t => t.length > 2);
                const scores = {};
                
                for (const token of tokens) {
                  for (const [term, docIds] of Object.entries(index.invertedIndex)) {
                    if (term.startsWith(token)) {
                      for (const docId of docIds) {
                        scores[docId] = (scores[docId] || 0) + 1;
                      }
                    }
                  }
                }
                
                return Object.entries(scores)
                  .sort((a, b) => b[1] - a[1])
                  .slice(0, 10)
                  .map(([id]) => index.documents[parseInt(id)]);
              }
              
              window.GenesisSearch = { loadIndex, search };
            })();
        """.trimIndent()
    }
}
