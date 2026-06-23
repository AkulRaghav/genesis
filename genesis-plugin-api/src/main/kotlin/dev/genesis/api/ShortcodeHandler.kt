package dev.genesis.api

/**
 * Handles a custom shortcode in Markdown content.
 * Shortcodes use Hugo-style syntax: {{< name param="value" >}} or {{< name >}}body{{< /name >}}
 */
interface ShortcodeHandler {
    /** The shortcode name (e.g. "youtube", "figure", "callout") */
    val name: String

    /**
     * Render this shortcode to HTML.
     * @param params Named parameters from the shortcode invocation
     * @param body Inner content between opening/closing tags (null for self-closing shortcodes)
     * @param page Context about the page containing this shortcode
     * @return The HTML to replace the shortcode with
     */
    fun render(params: Map<String, String>, body: String?, page: PageMetadata): String
}
