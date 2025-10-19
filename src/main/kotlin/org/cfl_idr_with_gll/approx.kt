package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.graph.parseDyckComponent
import org.cfl_idr_with_gll.graph.parseDyckComponentNaive
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph
import org.ucfs.parser.Gll

/**
 * Represents a directed path between two vertices in a graph.
 *
 * @property start the source vertex of the path
 * @property end the target vertex of the path
 */
data class Path<V>(val start: V, val end: V)

/**
 * Computes an under-approximation of Dyck-reachable vertex pairs in the input graph.
 *
 * The algorithm assumes edge labels follow the format:
 * - `op--N` / `cp--N` for parentheses
 * - `ob--N` / `cb--N` for square brackets
 *
 * @param graph input graph with Dyck-style edge labels
 * @return list of Dyck-valid paths (start -> end) with start != end
 */
fun <V, L : ILabel> getUnderApprox(
	graph: InputGraph<V, L>
): List<Path<V>> {
	val (_, _, gCopy) = parseDyckComponentNaive(graph)

	val (parList, braList, filteredGraph) = parseDyckComponent(gCopy)

	val grammar = DyckGrammar(parList, braList)

	for (v in filteredGraph.vertices) {
		filteredGraph.addStartVertex(v)
	}

	val gll = Gll.gll(grammar.rsm, filteredGraph)
	val sppf = gll.parse()

	val paths = sppf.mapNotNull { node ->
		node.inputRange?.let { range ->
			Path(range.from, range.to)
		}
	}.filter { it.start != it.end }

	return paths
}
