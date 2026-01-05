package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.models.Path
import org.cfl_idr_with_gll.models.SppfEdge
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.sppf.node.*

/**
 * Extracts all terminal-labeled edges from an SPPF (Shared Packed Parse Forest) structure.
 *
 * This function traverses the SPPF graph and extracts edges that represent terminal symbols
 * in the parse forest. It can optionally filter edges to only those that belong
 * to a specific source-target path.
 *
 * @param sppf a set of root nodes of the SPPF structure
 * @param targetPath an optional path constraint; if specified, only edges within this
 *                   source-target range are extracted
 * @return a set of [SppfEdge] objects representing all terminal-labeled edges
 *         (or those within the target path if specified)
 */
internal fun <VertexType> extractEdgesFromSppfResults(
	sppf: Set<RangeSppfNode<VertexType>>,
	targetPath: Path<VertexType>? = null
): Set<SppfEdge<VertexType>> {
	val edges = mutableSetOf<SppfEdge<VertexType>>()
	val visited = mutableSetOf<RangeSppfNode<VertexType>>()
	val stack = ArrayDeque<RangeSppfNode<VertexType>>()

	for (root in sppf) {
		if (root.inputRange?.from == root.inputRange?.to) continue

		if (targetPath == null ||
			(root.inputRange?.from == targetPath.source && root.inputRange?.to == targetPath.target)
		) {
			stack.add(root)
		}
	}

	while (stack.isNotEmpty()) {
		val node = stack.removeLast()

		if (!visited.add(node)) continue

		when (val nodeType = node.type) {
			is TerminalType<*> -> {
				edges.add(SppfEdge(node.inputRange!!.from, nodeType.terminal, node.inputRange!!.to))
			}

			is IntermediateType<*>,
			is Range,
			is NonterminalType,
			is EpsilonNonterminalType,
			is EmptyType -> {
				for (child in node.children) {
					stack.add(child)
				}
			}
		}
	}

	return edges
}

/**
 * Extracts non-trivial paths from SPPF parse results.
 *
 * A non-trivial path is defined as a path where the source and target vertices
 * are different.
 *
 * @param sppf a set of SPPF nodes containing input range information
 * @return a set of [Path] objects representing non-trivial paths found in the SPPF
 */
internal fun <V> extractNonTrivialPaths(sppf: Set<RangeSppfNode<V>>): Set<Path<V>> {
	return buildSet {
		for (node in sppf) {
			node.inputRange?.let { range ->
				val path = Path(range.from, range.to)
				if (path.source != path.target) {
					add(path)
				}
			}
		}
	}
}

/**
 * Creates an [InputGraph] from a set of SPPF edges.
 *
 * This function converts a collection of [SppfEdge] objects into a directed graph
 * where each edge's label is wrapped in a [TerminalInputLabel]. All vertices
 * referenced in the edges are automatically added to the graph.
 *
 * @param edges a set of SPPF edges to convert
 * @return an [InputGraph] containing all vertices and edges from the input
 */
internal fun <V> createGraphFromEdges(edges: Set<SppfEdge<V>>): InputGraph<V, TerminalInputLabel> {
	val graph = InputGraph<V, TerminalInputLabel>()

	for (edge in edges) {
		graph.addVertex(edge.from)
		graph.addVertex(edge.to)
		graph.addEdge(edge.from, TerminalInputLabel(edge.label), edge.to)
	}

	return graph
}
