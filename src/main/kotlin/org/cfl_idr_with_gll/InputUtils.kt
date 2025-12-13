package org.cfl_idr_with_gll

import java.io.File
import kotlin.collections.filter
import kotlin.text.isNotBlank
import kotlin.text.split
import kotlin.text.substringBefore
import kotlin.text.trim

/**
 * Determines whether a file contains Graphviz DOT format content.
 *
 * This function checks if the first non-blank line of a file starts with "digraph".
 *
 * @param file the file to check
 * @return `true` if the file is in Graphviz DOT format, `false` otherwise
 */
internal fun isGraphvizFormat(file: File): Boolean {
	val firstLine = file.useLines { lines ->
		lines.firstOrNull { it.isNotBlank() }
	}
	return firstLine?.trim()?.startsWith("digraph") == true
}

/**
 * Converts a text representation of edges into a valid Graphviz DOT format string.
 *
 * This function takes a text input where each line represents an edge in the format
 * `source -> target[label]` and converts it to a complete Graphviz directed graph
 * specification. It automatically adds the `digraph G { ... }` wrapper and ensures
 * proper formatting.
 *
 * Input format example:
 * ```
 * a -> b[label1]
 * b -> c[label2]
 * ```
 *
 * Output format:
 * ```
 * digraph G {
 *     a -> b[label1];
 *     b -> c[label2];
 * }
 * ```
 *
 * @param inputText a string containing edge definitions, one per line
 * @return a complete Graphviz DOT format string
 */
internal fun convertEdgesToGraphvizText(inputText: String): String {
	val edges = inputText.lines().filter { it.isNotBlank() }
	val vertices = mutableSetOf<String>()

	if (edges.isEmpty()) {
		return "digraph G {\n}"
	}

	for (line in edges) {
		val parts = line.split("->")
		if (parts.size < 2) continue
		val from = parts[0].trim()
		val to = parts[1].substringBefore("[").trim()
		vertices.add(from)
		vertices.add(to)
	}

	return buildString {
		appendLine("digraph G {")

		for (line in edges) {
			val trimmedLine = line.trim()
			val lineWithSemicolon = if (trimmedLine.endsWith(";")) trimmedLine else "$trimmedLine;"
			appendLine("    $lineWithSemicolon")
		}
		appendLine("}")
	}
}
