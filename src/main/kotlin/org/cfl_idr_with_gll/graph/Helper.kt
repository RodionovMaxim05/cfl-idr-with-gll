package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.Path
import org.ucfs.input.InputGraph
import org.ucfs.input.ILabel
import kotlin.collections.mutableMapOf
import kotlin.collections.set

// PMR - Path-Minimized Representatives

fun <V> findPMR(vertex: V, parentMap: MutableMap<V, V>): V {
	val parentV = parentMap[vertex]
	if (parentV != vertex && parentV != null) {
		parentMap[vertex] = findPMR(parentV, parentMap)
	}
	return parentMap[vertex] ?: vertex
}

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
	underApprox: Set<Path<V>>
): Pair<InputGraph<V, L>, MutableMap<V, V>> {

	// Initializing Union-Find
	val parentMap = mutableMapOf<V, V>()
	val weightMap = mutableMapOf<V, Int>()

	for (v in graph.vertices) {
		parentMap[v] = v
		weightMap[v] = 1
	}

	fun joinPMR(vertex1: V, vertex2: V) {
		var v = findPMR(vertex1, parentMap)
		var u = findPMR(vertex2, parentMap)
		if (v == u) return

		if (weightMap[v]!! < weightMap[u]!!) {
			val tmp = v; v = u; u = tmp
		}

		weightMap[v] = weightMap[v]!! + weightMap[u]!!
		parentMap[u] = v
	}

	// UnderApprox traversal: merge mutually reachable vertices
	val accessibilityMap = mutableMapOf<V, MutableSet<V>>()

	for (path in underApprox) {
		if (path.source !in parentMap || path.target !in parentMap) continue

		val sourceRoot = findPMR(path.source, parentMap)
		val targetRoot = findPMR(path.target, parentMap)
		if (sourceRoot == targetRoot) continue

		accessibilityMap.getOrPut(sourceRoot) { mutableSetOf() }.add(targetRoot)
		accessibilityMap.getOrPut(targetRoot) { mutableSetOf() } // ensure lv key exists

		// If mutual accessibility is found, combine the components
		if (accessibilityMap[sourceRoot]?.contains(targetRoot) == true && accessibilityMap[targetRoot]?.contains(
				sourceRoot
			) == true
		) {
			joinPMR(sourceRoot, targetRoot)
		}
	}

	// Constructing a condensed graph
	val condensedGraph = InputGraph<V, L>()
	
	val representatives = graph.vertices.map { findPMR(it, parentMap) }.toSet()
	for (rep in representatives) {
		condensedGraph.addVertex(rep)
	}

	val existingEdges = mutableSetOf<Triple<V, V, L>>()

	for ((sourceV, edgeList) in graph.edges) {
		for (edge in edgeList) {
			if (edge.label.toString().isEmpty()) continue

			val sourceRoot = findPMR(sourceV, parentMap)
			val targetRoot = findPMR(edge.targetVertex, parentMap)

			val key = Triple(sourceRoot, targetRoot, edge.label)
			if (existingEdges.contains(key)) continue
			existingEdges.add(key)

			condensedGraph.addVertex(sourceRoot)
			condensedGraph.addVertex(targetRoot)
			condensedGraph.addEdge(sourceRoot, edge.label, targetRoot)
		}
	}

	return condensedGraph to parentMap
}
