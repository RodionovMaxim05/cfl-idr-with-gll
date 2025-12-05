package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.Path
import org.cfl_idr_with_gll.adapters.InputGraphJGraphTAdapter
import org.jgrapht.alg.TransitiveClosure
import org.jgrapht.alg.connectivity.ConnectivityInspector
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph

fun <V, L : ILabel> InputGraph<V, L>.splitIntoConnectedComponents(): List<InputGraph<V, L>> {
	val jGraph = InputGraphJGraphTAdapter.toJGraphT(this)
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

private fun fnv1aHash(data: ByteArray): ULong {
	var hash = 0xCBF29CE484222325UL // FNV offset basis (64-bit)
	val fnvPrime = 0x100000001B3UL // FNV prime (64-bit)

	for (byte in data) {
		hash = hash xor byte.toULong()
		hash *= fnvPrime
	}

	return hash
}

fun <V, L : ILabel> InputGraph<V, L>.hash(): ULong {
	return hashGWithId(null)
}

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

fun <V, L : ILabel> InputGraph<V, L>.removeNotPath(overApprox: Set<Path<V>>): InputGraph<V, L> {
	val pairMatrix = overApprox.map { Pair(it.source, it.target) }

	return removeNotPathMatrix(pairMatrix)
}

private fun <V, L : ILabel> computeSccs(graph: InputGraph<V, L>): Pair<Map<V, Int>, Set<Pair<Int, Int>>> {
	val jGraph = InputGraphJGraphTAdapter.toJGraphT(graph)
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

fun <V, L : ILabel> InputGraph<V, L>.removeNotPathMatrix(overApprox: List<Pair<V, V>>): InputGraph<V, L> {
	if (overApprox.size == vertices.size * vertices.size) {
		return this
	}

	val (vertexToSccMap, sccReach) = computeSccs(this)

	// Build set of allowed component-to-component transitions
	val allowedCompTransitions = overApprox
		.mapNotNull { (u, v) ->
			val cu = vertexToSccMap[u] ?: return@mapNotNull null
			val cv = vertexToSccMap[v] ?: return@mapNotNull null
			cu to cv
		}
		.toSet()

	// Precompute which edges to keep
	val edgesToKeep = mutableSetOf<Pair<V, V>>()

	for ((from, edgesFrom) in edges) {
		for (edge in edgesFrom) {
			val to = edge.targetVertex

			val compFrom = vertexToSccMap[from] ?: continue
			val compTo = vertexToSccMap[to] ?: continue

			// Direct match
			if (Pair(compFrom, compTo) in allowedCompTransitions) {
				edgesToKeep += Pair(from, to)
				continue
			}

			// Indirect match via reachability
			for ((a, b) in allowedCompTransitions) {
				if (sccReach.contains(Pair(a, compFrom)) &&
					sccReach.contains(Pair(compTo, b))
				) {
					edgesToKeep += from to to
					break
				}
			}
		}
	}

	// Build result graph
	val resultGraph = InputGraph<V, L>()
	vertices.forEach { resultGraph.addVertex(it) }

	for ((sourceV, outgoingEdges) in edges) {
		for (edge in outgoingEdges) {
			val targetV = edge.targetVertex
			if (sourceV != targetV && Pair(sourceV, targetV) in edgesToKeep) {
				resultGraph.addEdge(sourceV, edge.label, targetV)
			}
		}
	}

	return resultGraph
}

