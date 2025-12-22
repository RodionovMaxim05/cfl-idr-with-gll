package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.models.Path
import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph

/**
 * Filters vertices from the graph that cannot participate in value-flow analysis paths.
 *
 * This function implements a value-flow specific optimization that removes vertices
 * which cannot be part of any valid store-to-load path. In value-flow analysis:
 * - **Store operations** are represented by opening brackets `[i`
 * - **Load operations** are represented by closing brackets `]i`
 *
 * A vertex is retained only if there exists:
 * 1. A reachable path from some store operation to the vertex
 * 2. A reachable path from the vertex to some load operation
 *
 * @param terminalFormat the format parser for identifying bracket labels and their direction
 * @return a new [InputGraph] containing only vertices that can participate in value-flow paths
 */
fun <V, L : ILabel> InputGraph<V, L>.removeValueflowUnreachable(terminalFormat: ITerminalFormat = DefaultTerminalFormat): InputGraph<V, L> {
	val (vertexToScc, sccReach) = computeSccs(this)

	val sourceSccs = mutableSetOf<Int>()
	val sinkSccs = mutableSetOf<Int>()

	// Identify store and load SCCs
	for (sourceV in this.vertices) {
		for (edge in this.getEdges(sourceV)) {
			val labelStr = edge.label.toString()

			if (terminalFormat.getType(labelStr) == ITerminalFormat.BracketType.Brackets) {
				if (terminalFormat.isOpen(labelStr) == true) {
					sourceSccs += vertexToScc[sourceV]!!

				} else if (terminalFormat.isOpen(labelStr) == false) {
					sinkSccs += vertexToScc[edge.targetVertex]!!
				}
			}
		}
	}

	// Select vertices that can participate in store-to-load paths
	val verticesToKeep = mutableSetOf<V>()

	for (vert in this.vertices) {
		val vScc = vertexToScc[vert] ?: continue

		if (!sourceSccs.any { (it to vScc) in sccReach }) continue
		if (!sinkSccs.any { (vScc to it) in sccReach }) continue

		verticesToKeep += vert
	}

	// Build result graph
	val resultGraph = InputGraph<V, L>()

	for (sourceV in verticesToKeep) {
		for (edge in this.getEdges(sourceV)) {
			val targetV = edge.targetVertex
			if (edge.targetVertex in verticesToKeep) {
				resultGraph.addVertex(sourceV)
				resultGraph.addVertex(targetV)
				resultGraph.addEdge(sourceV, edge.label, targetV)
			}
		}
	}

	return resultGraph
}

/**
 * Filters paths based on matching bracket constraints using SCC reachability analysis.
 *
 * This function verifies whether each path in the given set can be "bracketed" - i.e.,
 * whether there exist matching opening and closing brackets that enclose the path.
 * Specifically, for a path `source → target` to be valid, there must exist:
 * 1. An **opening bracket edge** `source --([i)--> U`
 * 2. A **closing bracket edge** `V --(]i)--> target`
 * 3. **Matching bracket IDs**: The opening `[i` and closing `]i` must have the same identifier
 * 4. **Reachability**: Vertex `U` must be able to reach vertex `V` in the SCC condensation graph
 *
 * @param paths the set of paths to filter
 * @param terminalFormat the format parser for identifying bracket labels (defaults to [DefaultTerminalFormat])
 * @return a subset of paths that satisfy the bracket matching condition
 */
fun <V, L : ILabel> InputGraph<V, L>.filterBracketPaths(
	paths: Set<Path<V>>,
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<V>> {
	val (vertexToScc, sccReach) = computeSccs(this)

	// Invert the edges for ']i' to efficiently find InEdges.
	// Need to know which vertices v lead to path.target by the label closeBracket.
	val incomingCloseEdges = mutableMapOf<V, MutableList<Pair<V, String>>>()

	for ((source, edgeList) in this.edges) {
		for (edge in edgeList) {
			val labelStr = edge.label.toString()

			if (terminalFormat.getType(labelStr) == ITerminalFormat.BracketType.Brackets &&
				terminalFormat.isOpen(labelStr) == false
			) {
				val bracketId = terminalFormat.extractId(labelStr)
				incomingCloseEdges.getOrPut(edge.targetVertex) { mutableListOf() }
					.add(Pair(source, bracketId))
			}
		}
	}

	// Filtering paths
	return paths.filterTo(HashSet()) { path ->
		// Find all U such that: path.source --(ob)--> U
		val outNodesWithIds = this.edges[path.source]
			?.mapNotNull { edge ->
				val labelStr = edge.label.toString()
				if (terminalFormat.getType(labelStr) == ITerminalFormat.BracketType.Brackets &&
					terminalFormat.isOpen(labelStr) == true
				) {
					Pair(edge.targetVertex, terminalFormat.extractId(labelStr))
				} else null
			} ?: emptyList()

		// Find all V such that: V --(cb)--> path.target
		val inNodesWithIds = incomingCloseEdges[path.target] ?: emptyList()
		if (inNodesWithIds.isEmpty()) return@filterTo false

		// Check reachability in the condensation graph: SCC(U) -> SCC(V)
		outNodesWithIds.any outNodes@{ (u, openId) ->
			val sccU = vertexToScc[u] ?: return@outNodes false

			inNodesWithIds.any inNodes@{ (v, closeId) ->
				// Check Id match ([i ... ]i)
				if (openId != closeId) return@inNodes false

				val sccV = vertexToScc[v] ?: return@inNodes false
				(Pair(sccU, sccV)) in sccReach
			}
		}
	}
}
