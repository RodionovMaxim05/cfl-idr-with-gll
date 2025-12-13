package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.Path
import org.jgrapht.alg.TransitiveClosure
import org.jgrapht.alg.connectivity.ConnectivityInspector
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph

/**
 * Splits this [InputGraph] into its weakly connected components.
 *
 * This function partitions the graph into maximal subgraphs where each vertex
 * is reachable from every other vertex via an undirected path (ignoring edge direction).
 *
 * @return a list of connected components, each represented as a separate [InputGraph].
 */
fun <V, L : ILabel> InputGraph<V, L>.splitIntoConnectedComponents(): List<InputGraph<V, L>> {
	val jGraph = toJGraphT(this)
	val inspector = ConnectivityInspector(jGraph)

	val gComponents = inspector.connectedSets()
	val resultComp = mutableListOf<InputGraph<V, L>>()

	for (compVertices in gComponents) {
		val subGraph = InputGraph<V, L>()
		compVertices.forEach { subGraph.addVertex(it) }

		for (sourceV in compVertices) {
			val outgoingEdges = this.getEdges(sourceV)

			for (edge in outgoingEdges) {
				if (edge.targetVertex in compVertices) {
					subGraph.addEdge(sourceV, edge.label, edge.targetVertex)
				}
			}
		}
		resultComp.add(subGraph)
	}

	return resultComp
}

/**
 * Computes the FNV-1a 64-bit hash of a byte array.
 *
 * @param data the byte array to hash
 * @return the 64-bit FNV-1a hash as an unsigned long
 *
 * @see <a href="https://ssojet.com/hashing/fnv-1a-in-kotlin/">FNV-1a hash</a>
 */
private fun fnv1aHash(data: ByteArray): ULong {
	var hash = 0xCBF29CE484222325UL // FNV offset basis (64-bit)
	val fnvPrime = 0x100000001B3UL // FNV prime (64-bit)

	for (byte in data) {
		hash = hash xor byte.toULong()
		hash *= fnvPrime
	}

	return hash
}

/**
 * Computes a structural hash of this [InputGraph].
 *
 * The hash is computed from a canonical string representation of all edges
 * (sorted lexicographically) and is insensitive to vertex/edge ordering.
 *
 * @return a 64-bit hash value representing the graph structure
 */
fun <V, L : ILabel> InputGraph<V, L>.hash(): ULong {
	return hashGWithId(null)
}

/**
 * Computes a structural hash of this [InputGraph] with an optional identifier.
 *
 * The hash incorporates both the graph structure (string representation of all
 * edges sorted lexicographically) and the provided identifier.
 *
 * @param id an optional string identifier to include in the hash computation
 * @return a 64-bit hash value combining the graph structure and identifier
 */
fun <V, L : ILabel> InputGraph<V, L>.hashGWithId(id: String? = null): ULong {
	val edgeStrings = buildList {
		for (source in vertices) {
			for (edge in getEdges(source)) {
				val labelStr = edge.label.toString()

				add("$source->${edge.targetVertex}[$labelStr]")
			}
		}
		if (id != null) add("($id)")
	}.sorted()

	val key = edgeStrings.joinToString(",")

	return fnv1aHash(key.toByteArray())
}

/**
 * Computes strongly connected components (SCCs) and their reachability relations.
 *
 * This function performs two main operations:
 * 1. Identifies all strongly connected components in the graph using Kosaraju's algorithm.
 * 2. Builds the condensation DAG (where each SCC is a node) and computes its transitive closure.
 *
 * @return a [Pair] where:
 *   - first: a map from each vertex to its SCC index
 *   - second: a set of SCC index pairs representing reachability in the condensed DAG
 *             (includes reflexive pairs `(i, i)` for each SCC)
 */
fun <V, L : ILabel> computeSccs(graph: InputGraph<V, L>): Pair<Map<V, Int>, Set<Pair<Int, Int>>> {
	val jGraph = toJGraphT(graph)
	val inspector = KosarajuStrongConnectivityInspector(jGraph)

	val sccList: List<Set<V>> = inspector.stronglyConnectedSets()

	val vertexToSccMap = mutableMapOf<V, Int>()
	sccList.forEachIndexed { idx, comp ->
		comp.forEach { vertexToSccMap[it] = idx }
	}

	// Build condensation DAG

	val componentCount = sccList.size
	if (componentCount == 0) return vertexToSccMap to setOf()

	val condensation = SimpleDirectedGraph<Int, DefaultEdge>(DefaultEdge::class.java)
	for (i in 0 until componentCount) {
		condensation.addVertex(i)
	}

	for ((sourceV, edges) in graph.edges) {
		val fromScc = vertexToSccMap[sourceV] ?: continue
		for (edge in edges) {
			val toScc = vertexToSccMap[edge.targetVertex] ?: continue
			if (fromScc != toScc) {
				condensation.addEdge(fromScc, toScc)
			}
		}
	}

	// Compute transitive closure
	TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(condensation)

	val sccReach = mutableSetOf<Pair<Int, Int>>()

	// Reflexivity
	for (idx in 0 until componentCount) {
		sccReach.add(idx to idx)
	}

	// All edges after closure - reachability
	for (edge in condensation.edgeSet()) {
		val src = condensation.getEdgeSource(edge)
		val tgt = condensation.getEdgeTarget(edge)
		sccReach.add(src to tgt)
	}

	return vertexToSccMap to sccReach
}

/**
 * Removes edges from this graph that cannot be part of any path in the given over-approximation.
 *
 * This function filters edges based on a set of allowed source-target vertex pairs.
 * An edge `u -> v` is kept if `(u, v)` appears in the over-approximation set or
 * if there exists a path in the SCC condensation graph that connects allowed pairs.
 * It uses SCC condensation and transitive closure to determine which edges can be preserved.
 *
 * @param overApprox a set of allowed paths
 * @return a new [InputGraph] containing only edges that can participate in allowed paths
 */
fun <V, L : ILabel> InputGraph<V, L>.removeNotPath(overApprox: Set<Path<V>>): InputGraph<V, L> {
	val pairMatrix = overApprox.map { Pair(it.source, it.target) }

	if (pairMatrix.size == vertices.size * vertices.size) {
		return this
	}

	val (vertexToSccMap, sccReach) = computeSccs(this)

	// Build set of allowed component-to-component transitions
	val allowedCompTransitions = pairMatrix
		.mapNotNull { (u, v) ->
			val cu = vertexToSccMap[u] ?: return@mapNotNull null
			val cv = vertexToSccMap[v] ?: return@mapNotNull null
			cu to cv
		}
		.toSet()

	// Precompute which edges to keep
	val edgesToKeep = mutableSetOf<Pair<V, V>>()

	for ((sourceV, edgesFrom) in edges) {
		for (edge in edgesFrom) {
			val targetV = edge.targetVertex

			val compFrom = vertexToSccMap[sourceV] ?: continue
			val compTo = vertexToSccMap[targetV] ?: continue

			// Direct match
			if (Pair(compFrom, compTo) in allowedCompTransitions) {
				edgesToKeep += Pair(sourceV, targetV)
				continue
			}

			// Indirect match via reachability
			for ((a, b) in allowedCompTransitions) {
				if (sccReach.contains(Pair(a, compFrom)) &&
					sccReach.contains(Pair(compTo, b))
				) {
					edgesToKeep += sourceV to targetV
					break
				}
			}
		}
	}

	// Build result graph
	val resultGraph = InputGraph<V, L>()

	for ((sourceV, outgoingEdges) in edges) {
		for (edge in outgoingEdges) {
			val targetV = edge.targetVertex
			if (Pair(sourceV, targetV) in edgesToKeep) {
				resultGraph.addVertex(sourceV)
				resultGraph.addVertex(targetV)
				resultGraph.addEdge(sourceV, edge.label, targetV)
			}
		}
	}

	return resultGraph
}
