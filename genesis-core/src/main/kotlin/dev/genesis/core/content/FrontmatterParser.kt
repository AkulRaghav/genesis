package dev.genesis.core.content

import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlMap
import net.mamoe.yamlkt.YamlNull
import net.mamoe.yamlkt.YamlElement
import net.mamoe.yamlkt.YamlList
import net.mamoe.yamlkt.YamlLiteral

/**
 * Parses frontmatter from content files.
 * Supports YAML (--- delimiters) and TOML (+++ delimiters) frontmatter.
 */
class FrontmatterParser {

    data class ParseResult(
        /** Parsed frontmatter key-value pairs */
        val frontmatter: Map<String, Any?>,
        /** The content body (everything after frontmatter) */
        val content: String
    )

    /**
     * Parse a content file's raw text, separating frontmatter from body.
     */
    fun parse(rawContent: String): ParseResult {
        val trimmed = rawContent.trimStart()

        return when {
            trimmed.startsWith("---") -> parseYamlFrontmatter(trimmed)
            trimmed.startsWith("+++") -> parseTomlFrontmatter(trimmed)
            else -> ParseResult(emptyMap(), rawContent)
        }
    }

    private fun parseYamlFrontmatter(content: String): ParseResult {
        val lines = content.lines()
        val endIndex = lines.drop(1).indexOfFirst { it.trim() == "---" }

        if (endIndex == -1) {
            return ParseResult(emptyMap(), content)
        }

        val yamlBlock = lines.subList(1, endIndex + 1).joinToString("\n")
        val body = lines.subList(endIndex + 2, lines.size).joinToString("\n")

        val frontmatter = parseYaml(yamlBlock)
        return ParseResult(frontmatter, body.trimStart())
    }

    private fun parseTomlFrontmatter(content: String): ParseResult {
        val lines = content.lines()
        val endIndex = lines.drop(1).indexOfFirst { it.trim() == "+++" }

        if (endIndex == -1) {
            return ParseResult(emptyMap(), content)
        }

        val tomlBlock = lines.subList(1, endIndex + 1).joinToString("\n")
        val body = lines.subList(endIndex + 2, lines.size).joinToString("\n")

        // Simple TOML parsing for key = "value" pairs
        val frontmatter = parseSimpleToml(tomlBlock)
        return ParseResult(frontmatter, body.trimStart())
    }

    private fun parseYaml(yaml: String): Map<String, Any?> {
        return try {
            val element = Yaml.decodeYamlFromString(yaml)
            yamlElementToMap(element)
        } catch (e: Exception) {
            // Fallback to simple key-value parsing
            parseSimpleYaml(yaml)
        }
    }

    private fun yamlElementToMap(element: YamlElement): Map<String, Any?> {
        return when (element) {
            is YamlMap -> element.entries.associate { (key, value) ->
                key.toString() to yamlElementToValue(value)
            }
            else -> emptyMap()
        }
    }

    private fun yamlElementToValue(element: YamlElement): Any? {
        return when (element) {
            is YamlNull -> null
            is YamlLiteral -> {
                val content = element.content
                // Try to parse as boolean or number
                when {
                    content.equals("true", ignoreCase = true) -> true
                    content.equals("false", ignoreCase = true) -> false
                    content.toIntOrNull() != null -> content.toInt()
                    content.toLongOrNull() != null -> content.toLong()
                    content.toDoubleOrNull() != null -> content.toDouble()
                    else -> content
                }
            }
            is YamlMap -> yamlElementToMap(element)
            is YamlList -> element.map { yamlElementToValue(it) }
            else -> element.toString()
        }
    }

    /**
     * Fallback simple YAML parser for basic key: value frontmatter.
     */
    private fun parseSimpleYaml(yaml: String): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        for (line in yaml.lines()) {
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.substring(0, colonIndex).trim()
                val value = line.substring(colonIndex + 1).trim()
                result[key] = parseValue(value)
            }
        }
        return result
    }

    /**
     * Simple TOML parser for key = "value" frontmatter.
     */
    private fun parseSimpleToml(toml: String): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        for (line in toml.lines()) {
            val equalsIndex = line.indexOf('=')
            if (equalsIndex > 0) {
                val key = line.substring(0, equalsIndex).trim()
                val value = line.substring(equalsIndex + 1).trim()
                result[key] = parseValue(value)
            }
        }
        return result
    }

    private fun parseValue(value: String): Any? {
        return when {
            value.isEmpty() -> null
            value == "true" -> true
            value == "false" -> false
            value.startsWith('"') && value.endsWith('"') -> value.removeSurrounding("\"")
            value.startsWith("'") && value.endsWith("'") -> value.removeSurrounding("'")
            value.startsWith('[') && value.endsWith(']') -> {
                // Parse simple list: [item1, item2, item3]
                value.removeSurrounding("[", "]")
                    .split(',')
                    .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                    .filter { it.isNotEmpty() }
            }
            value.toIntOrNull() != null -> value.toInt()
            value.toLongOrNull() != null -> value.toLong()
            value.toDoubleOrNull() != null -> value.toDouble()
            else -> value
        }
    }
}
