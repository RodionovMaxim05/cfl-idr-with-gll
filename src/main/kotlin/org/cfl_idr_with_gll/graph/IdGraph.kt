package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.input.ILabel
import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType
import org.ucfs.input.InputGraph

/**
 * Represents a complete edge with explicit source, target, and label.
 *
 * @property from the source vertex of the edge
 * @property to the target vertex of the edge
 * @property label the label associated with the edge
 */
private data class FullEdge<V, L>(val from: V, val to: V, val label: L)

/**
 * Parses a graph to extract Dyck language components using bracket matching.
 *
 * This function processes an [InputGraph] to identify and extract bracket-labeled edges
 * that form valid Dyck language pairs (matching opening and closing brackets).
 *
 * @param graph the input graph to parse
 * @param terminalFormat the format parser for bracket labels (defaults to [DefaultTerminalFormat])
 * @return a [Triple] containing:
 *   - [List] of parenthesis identifiers
 *   - [List] of bracket identifiers
 *   - Filtered [InputGraph] with valid Dyck edges
 */
fun <V, L : ILabel> parseDyckComponent(
	graph: InputGraph<V, L>,
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
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

/**
 * Parses a graph to extract Dyck language components using a naive approach.
 *
 * This function provides a simpler algorithm for extracting bracket pairs
 * from a graph. Unlike [parseDyckComponent], it processes each edge independently
 * without requiring matching pairs to exist in the graph.
 *
 * @param graph the input graph to parse
 * @param terminalFormat the format parser for bracket labels
 * @return a [Triple] containing:
 *   - [List] of all parenthesis identifiers
 *   - [List] of all bracket identifiers
 *   - Filtered [InputGraph] with all labeled edges
 */
fun <V, L : ILabel> parseDyckComponentNaive(
	graph: InputGraph<V, L>,
	terminalFormat: ITerminalFormat
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
