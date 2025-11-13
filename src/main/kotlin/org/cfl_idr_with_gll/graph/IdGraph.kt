package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.input.ILabel
import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType
import org.ucfs.input.InputGraph

/**
 * Represents a complete edge with explicit source, target, and label.
 */
data class FullEdge<V, L>(val from: V, val to: V, val label: L)

fun <V, L : ILabel> parseDyckComponent(
	graph: InputGraph<V, L>,
	terminalFormat: ITerminalFormat
): Triple<List<String>, List<String>, InputGraph<V, L>> {
	val processedLabels = mutableSetOf<String>()
	val parIds = mutableListOf<String>()
	val braIds = mutableListOf<String>()

	val allEdges = graph.vertices.flatMap { sourceV ->
		graph.getEdges(sourceV).map { edge ->
			FullEdge(sourceV, edge.targetVertex, edge.label)
		}
	}

	for (edge in allEdges) {
		val label = edge.label.toString()
		if (label.isEmpty() || label == "normal") continue

		if (label !in processedLabels && terminalFormat.matchingLabel(label) in processedLabels) {
			val idString = terminalFormat.extractId(label)

			when (terminalFormat.getType(label)) {
				BracketType.Parentheses -> parIds.add(idString)
				BracketType.Brackets -> braIds.add(idString)

				else -> {}
			}
		}
		processedLabels.add(label)
	}

	val parsedDyckGraph = InputGraph<V, L>()
	for (edge in allEdges) {
		val label = edge.label.toString()

		if (label == "normal" || (label.isNotEmpty() && label in processedLabels && terminalFormat.matchingLabel(label) in processedLabels)) {
			parsedDyckGraph.addVertex(edge.from)
			parsedDyckGraph.addVertex(edge.to)
			parsedDyckGraph.addEdge(edge.from, edge.label, edge.to)
		}
	}

	return Triple(parIds, braIds, parsedDyckGraph)
}

fun <V, L : ILabel> parseDyckComponentNaive(
	terminalFormat: ITerminalFormat,
	graph: InputGraph<V, L>
): Triple<List<String>, List<String>, InputGraph<V, L>> {
	val processedLabels = mutableSetOf<String>()
	val parIds = mutableListOf<String>()
	val braIds = mutableListOf<String>()

	val allEdges = graph.vertices.flatMap { from ->
		graph.getEdges(from).map { edge ->
			FullEdge(from, edge.targetVertex, edge.label)
		}
	}

	for (edge in allEdges) {
		val label = edge.label.toString()
		if (label == "normal" || label.isEmpty()) continue

		val terminalType = terminalFormat.getType(label)
		val id = terminalFormat.extractId(label)
		val labelKey = "${terminalType}$id"

		if (labelKey !in processedLabels) {
			val idString = terminalFormat.extractId(label)

			when (terminalType) {
				BracketType.Parentheses -> parIds.add(idString)
				BracketType.Brackets -> braIds.add(idString)

				else -> {}
			}
		}
		processedLabels.add(labelKey)
	}

	val parsedDyck = InputGraph<V, L>()
	for (edge in allEdges) {
		val label = edge.label.toString()
		if (label.isNotEmpty()) {
			parsedDyck.addVertex(edge.from)
			parsedDyck.addVertex(edge.to)
			parsedDyck.addEdge(edge.from, edge.label, edge.to)
		}
	}

	return Triple(parIds, braIds, parsedDyck)
}
