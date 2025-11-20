package org.cfl_idr_with_gll

import org.ucfs.rsm.symbol.ITerminal
import org.ucfs.sppf.node.*
import java.io.File
import kotlin.collections.filter
import kotlin.text.isNotBlank
import kotlin.text.split
import kotlin.text.substringBefore
import kotlin.text.trim

data class SppfEdge<VertexType>(val from: VertexType, val label: ITerminal, val to: VertexType)

fun <VertexType> extractEdgesFromSppfResults(
	sppf: Set<RangeSppfNode<VertexType>>
): Set<SppfEdge<VertexType>> {
	val edges = mutableSetOf<SppfEdge<VertexType>>()
	val visited = mutableSetOf<RangeSppfNode<VertexType>>()

	fun dfs(node: RangeSppfNode<VertexType>) {
		if (!visited.add(node)) return

		when (val t = node.type) {
			is TerminalType<*> -> {
				edges.add(SppfEdge(node.inputRange!!.from, t.terminal, node.inputRange!!.to))
			}

			is IntermediateType<*> -> {
				for (child in node.children) {
					dfs(child)
				}
			}

			is Range, is NonterminalType, is EpsilonNonterminalType, is EmptyType -> {
				for (child in node.children) {
					dfs(child)
				}
			}
		}
	}

	for (root in sppf) {
		dfs(root)
	}

	return edges
}

fun isGraphvizFormat(file: File): Boolean {
	val firstLine = file.useLines { lines ->
		lines.firstOrNull { it.isNotBlank() }
	}
	return firstLine?.trim()?.startsWith("digraph") == true
}

fun convertEdgesToGraphvizText(inputText: String): String {
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
