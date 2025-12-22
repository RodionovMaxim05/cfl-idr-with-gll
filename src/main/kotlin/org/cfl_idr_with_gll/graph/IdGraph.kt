package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.input.ILabel
import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType
import org.ucfs.input.InputGraph

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
	val processedLabels = HashSet<String>()
	val parIds = mutableListOf<String>()
	val braIds = mutableListOf<String>()

	for (sourceV in graph.vertices) {
		for (edge in graph.getEdges(sourceV)) {
			val label = edge.label.toString()

			if (label.isEmpty() || label == "normal") continue

			if (label !in processedLabels) {
				if (terminalFormat.matchingLabel(label) in processedLabels) {
					val idString = terminalFormat.extractId(label)

					when (terminalFormat.getType(label)) {
						BracketType.Parentheses -> parIds.add(idString)
						BracketType.Brackets -> braIds.add(idString)

						else -> {}
					}
				}
				processedLabels.add(label)
			}
		}
	}

	val parsedDyckGraph = InputGraph<V, L>()

	for (sourceV in graph.vertices) {
		val edges = graph.getEdges(sourceV)

		for (edge in edges) {
			val label = edge.label.toString()

			if (label == "normal" || (label.isNotEmpty() && label in processedLabels && terminalFormat.matchingLabel(
					label
				) in processedLabels)
			) {
				parsedDyckGraph.addVertex(sourceV)
				parsedDyckGraph.addVertex(edge.targetVertex)
				parsedDyckGraph.addEdge(sourceV, edge.label, edge.targetVertex)
			}
		}
	}

	return Triple(parIds, braIds, parsedDyckGraph)
}
