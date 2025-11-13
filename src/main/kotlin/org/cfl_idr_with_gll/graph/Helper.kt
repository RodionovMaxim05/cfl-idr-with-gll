package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.Path
import org.ucfs.input.InputGraph
import org.ucfs.input.ILabel

/**
 * Condenses a graph by a subset of paths (underApprox),
 * merging vertices that are mutually reachable within underApprox.
 *
 * @return
 * - a new "condensed" graph (condensedGraph),
 * - mapping parent: Vertex -> Representative (for back projection)
 */
fun <V, L : ILabel> condensateFromUnderApprox(
	graph: InputGraph<V, L>,
	underApprox: List<Path<V>>
): Pair<InputGraph<V, L>, Map<V, V>> {

	// Initializing Union-Find
	val parent = mutableMapOf<V, V>()
	val weight = mutableMapOf<V, Int>()

	for (v in graph.vertices) {
		parent[v] = v
		weight[v] = 1
	}

	// PMR - Path-Minimized Representatives

	fun findPMR(vertex: V): V {
		val parentV = parent[vertex]
		if (parentV != vertex && parentV != null) {
			parent[vertex] = findPMR(parentV)
		}
		return parent[vertex] ?: vertex
	}

	fun joinPMR(vertex1: V, vertex2: V) {
		var v = findPMR(vertex1)
		var u = findPMR(vertex2)
		if (v == u) return

		if (weight[v]!! < weight[u]!!) {
			val tmp = v; v = u; u = tmp
		}

		weight[v] = weight[v]!! + weight[u]!!
		parent[u] = v
	}

	// UnderApprox traversal: merge mutually reachable vertices
	val accessibilityMap = mutableMapOf<V, MutableSet<V>>()

	for (path in underApprox) {
		if (path.source !in parent || path.target !in parent) continue

		val sourceRep = findPMR(path.source)
		val targetRep = findPMR(path.target)
		if (sourceRep == targetRep) continue

		accessibilityMap.getOrPut(sourceRep) { mutableSetOf() }.add(targetRep)
		accessibilityMap.getOrPut(targetRep) { mutableSetOf() } // ensure lv key exists

		// If mutual accessibility is found, combine the components
		if (accessibilityMap[sourceRep]?.contains(targetRep) == true && accessibilityMap[targetRep]?.contains(
				sourceRep
			) == true
		) {
			joinPMR(sourceRep, targetRep)
		}
	}

	// Constructing a condensed graph
	val condensedGraph = InputGraph<V, L>()
	val existingEdges = mutableSetOf<Triple<V, V, L>>()

	for ((source, edgeList) in graph.edges) {
		for (edge in edgeList) {
			val sourceRep = findPMR(source)
			val targetRep = findPMR(edge.targetVertex)
			if (sourceRep == targetRep) continue

			val key = Triple(sourceRep, targetRep, edge.label)
			if (existingEdges.contains(key)) continue
			existingEdges.add(key)

			condensedGraph.addVertex(sourceRep)
			condensedGraph.addVertex(targetRep)
			condensedGraph.addEdge(sourceRep, edge.label, targetRep)
		}
	}

	return condensedGraph to parent
}
