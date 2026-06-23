package dev.genesis.core.plugin.builtin

import dev.genesis.api.*
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class OgImagePlugin : Plugin {
    override val id = "genesis-og-image"
    override val name = "OG Image Generator"
    override val version = "1.0.0"

    override fun initialize(context: PluginContext) {
        context.registerLifecycleHook(OgImageHook(context.config))
    }
}

class OgImageHook(private val config: SiteConfig) : BuildLifecycleHook {
    override val id = "og-image-generator"

    override suspend fun onPageRender(page: PageMetadata, html: String, context: BuildContext) {
        val ogDir = context.outputDir.resolve("og")
        ogDir.toFile().mkdirs()
        
        val image = generateOgImage(page.title, config.title)
        val outputFile = ogDir.resolve("${page.slug.replace("/", "-").ifEmpty { "index" }}.png").toFile()
        outputFile.parentFile.mkdirs()
        ImageIO.write(image, "PNG", outputFile)
    }

    private fun generateOgImage(title: String, siteTitle: String): BufferedImage {
        val width = 1200
        val height = 630
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()

        // Background
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.color = Color(15, 23, 42) // Dark blue background
        g2d.fillRect(0, 0, width, height)

        // Accent bar
        g2d.color = Color(37, 99, 235) // Blue accent
        g2d.fillRect(0, 0, 8, height)

        // Title
        g2d.color = Color.WHITE
        val titleFont = Font("SansSerif", Font.BOLD, 56)
        g2d.font = titleFont
        
        // Word wrap title
        val fm = g2d.fontMetrics
        val maxWidth = width - 120
        val words = title.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val test = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (fm.stringWidth(test) > maxWidth) {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = test
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        var y = 200
        for (line in lines.take(3)) {
            g2d.drawString(line, 60, y)
            y += 72
        }

        // Site title at bottom
        g2d.color = Color(148, 163, 184) // Muted color
        g2d.font = Font("SansSerif", Font.PLAIN, 28)
        g2d.drawString(siteTitle, 60, height - 60)

        g2d.dispose()
        return image
    }
}
