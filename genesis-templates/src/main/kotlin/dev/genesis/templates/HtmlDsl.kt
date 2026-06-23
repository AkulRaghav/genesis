package dev.genesis.templates

/**
 * Lightweight HTML DSL for Kotlin-based templates (Tier 1).
 * Provides type-safe HTML building without external dependencies.
 */
@DslMarker
annotation class HtmlDsl

@HtmlDsl
class HtmlBuilder {
    private val content = StringBuilder()

    fun doctype() {
        content.append("<!DOCTYPE html>\n")
    }

    fun html(lang: String = "en", block: TagBuilder.() -> Unit) {
        val tag = TagBuilder("html")
        tag.attr("lang", lang)
        tag.block()
        content.append(tag.build())
    }

    fun raw(text: String) {
        content.append(text)
    }

    fun build(): String = content.toString()
}

@HtmlDsl
class TagBuilder(private val tagName: String) {
    private val attributes = mutableMapOf<String, String>()
    private val children = StringBuilder()

    fun attr(name: String, value: String) {
        attributes[name] = value
    }

    fun id(value: String) = attr("id", value)
    fun className(value: String) = attr("class", value)

    fun head(block: TagBuilder.() -> Unit) = tag("head", block)
    fun body(block: TagBuilder.() -> Unit) = tag("body", block)
    fun div(block: TagBuilder.() -> Unit) = tag("div", block)
    fun span(block: TagBuilder.() -> Unit) = tag("span", block)
    fun p(block: TagBuilder.() -> Unit) = tag("p", block)
    fun a(href: String, block: TagBuilder.() -> Unit) {
        val t = TagBuilder("a")
        t.attr("href", href)
        t.block()
        children.append(t.build())
    }
    fun h1(block: TagBuilder.() -> Unit) = tag("h1", block)
    fun h2(block: TagBuilder.() -> Unit) = tag("h2", block)
    fun h3(block: TagBuilder.() -> Unit) = tag("h3", block)
    fun nav(block: TagBuilder.() -> Unit) = tag("nav", block)
    fun main(block: TagBuilder.() -> Unit) = tag("main", block)
    fun header(block: TagBuilder.() -> Unit) = tag("header", block)
    fun footer(block: TagBuilder.() -> Unit) = tag("footer", block)
    fun article(block: TagBuilder.() -> Unit) = tag("article", block)
    fun section(block: TagBuilder.() -> Unit) = tag("section", block)
    fun ul(block: TagBuilder.() -> Unit) = tag("ul", block)
    fun ol(block: TagBuilder.() -> Unit) = tag("ol", block)
    fun li(block: TagBuilder.() -> Unit) = tag("li", block)

    fun title(text: String) {
        children.append("<title>$text</title>")
    }

    fun meta(name: String, content: String) {
        children.append("""<meta name="$name" content="$content">""")
    }

    fun metaCharset(charset: String = "utf-8") {
        children.append("""<meta charset="$charset">""")
    }

    fun metaViewport() {
        children.append("""<meta name="viewport" content="width=device-width, initial-scale=1.0">""")
    }

    fun link(rel: String, href: String, type: String? = null) {
        val typeAttr = if (type != null) """ type="$type"""" else ""
        children.append("""<link rel="$rel" href="$href"$typeAttr>""")
    }

    fun script(src: String? = null, type: String? = null, content: String? = null) {
        val srcAttr = if (src != null) """ src="$src"""" else ""
        val typeAttr = if (type != null) """ type="$type"""" else ""
        children.append("""<script$srcAttr$typeAttr>${content ?: ""}</script>""")
    }

    fun text(value: String) {
        children.append(escapeHtml(value))
    }

    fun raw(html: String) {
        children.append(html)
    }

    private fun tag(name: String, block: TagBuilder.() -> Unit) {
        val t = TagBuilder(name)
        t.block()
        children.append(t.build())
    }

    fun build(): String {
        val attrs = if (attributes.isNotEmpty()) {
            " " + attributes.entries.joinToString(" ") { """${it.key}="${escapeAttr(it.value)}"""" }
        } else ""
        return "<$tagName$attrs>$children</$tagName>"
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun escapeAttr(text: String): String = text
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
}

/**
 * Entry point for building an HTML document.
 */
fun html(block: HtmlBuilder.() -> Unit): String {
    val builder = HtmlBuilder()
    builder.block()
    return builder.build()
}
