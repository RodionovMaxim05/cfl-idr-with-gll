package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.models.Path
import org.ucfs.input.InputGraph
import org.ucfs.input.ILabel
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge

/**
 * Converts an [InputGraph] to a JGraphT [DefaultDirectedGraph].
 *
 * This function creates a JGraphT representation of the input graph, preserving
 * all vertices and edges while discarding edge labels. The resulting graph
 * is suitable for use with JGraphT algorithms that require [DefaultDirectedGraph].
 *
 * @param inputGraph the graph to convert
 * @return a JGraphT [DefaultDirectedGraph] with the same vertices and edges
 *         (labels are not preserved)
 *
 * @note Edge labels are not preserved in the conversion as [DefaultEdge] does not support labels.
 */
internal fun <V, L : ILabel> toJGraphT(inputGraph: InputGraph<V, L>)
		: DefaultDirectedGraph<V, DefaultEdge> {

	val jGraph = DefaultDirectedGraph<V, DefaultEdge>(DefaultEdge::class.java)

	inputGraph.vertices.forEach { jGraph.addVertex(it) }

	inputGraph.edges.forEach { (from, edges) ->
		edges.forEach { edge ->
			jGraph.addEdge(from, edge.targetVertex)
		}
	}
	return jGraph
}

/**
 * Finds the Path-Minimized Representative (PMR) for a vertex using union-find with path compression.
 *
 * This function implements the `find` operation for a union-find data structure
 * with path compression. It recursively finds the root representative of a vertex
 * and updates parent pointers along the path to flatten the tree structure.
 *
 * @param vertex the vertex to find the representative for
 * @param parentMap mutable map storing parent relationships for the union-find structure
 * @return the root representative of the vertex's equivalence class
 */
fun <V> findPMR(vertex: V, parentMap: MutableMap<V, V>): V {
	val parentV = parentMap[vertex]
	if (parentV != vertex && parentV != null) {
		parentMap[vertex] = findPMR(parentV, parentMap)
	}
	return parentMap[vertex] ?: vertex
}

/**
 * Condenses a graph based on mutual reachability within a given under-approximation of paths.
 *
 * This function performs graph condensation by merging vertices that are mutually
 * reachable according to the provided under-approximation of paths. It uses a
 * union-find data structure to efficiently group vertices into equivalence classes.
 *
 * The algorithm works as follows:
 * 1. Initializes each vertex as its own component
 * 2. Processes the under-approximation paths to build an accessibility matrix
 * 3. Merges components that are mutually reachable (both directions)
 * 4. Constructs a condensed graph with representatives as vertices
 * 5. Adds edges between representatives (avoiding duplicates)
 *
 * @param graph the input graph to condense
 * @param underApprox a set of paths representing an under-approximation of reachability
 * @return a [Pair] containing:
 *   - [InputGraph] the condensed graph with representatives as vertices
 *   - [MutableMap] mapping from original vertices to their representatives
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
