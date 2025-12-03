package org.cfl_idr_with_gll.graph

import org.cfl_idr_with_gll.adapters.InputGraphJGraphTAdapter
import org.jgrapht.alg.connectivity.ConnectivityInspector
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph

fun <V, L : ILabel> InputGraph<V, L>.splitIntoConnectedComponents(): List<InputGraph<V, L>> {
	val jGraph = InputGraphJGraphTAdapter.toJGraphT(this)
	val inspector = ConnectivityInspector(jGraph)

	val components = inspector.connectedSets()
	val result = mutableListOf<InputGraph<V, L>>()

	for (componentVertices in components) {
		val subGraph = InputGraph<V, L>()
		componentVertices.forEach { subGraph.addVertex(it) }

		for (sourceV in componentVertices) {
			val outgoingEdges = this.getEdges(sourceV)

			for (edge in outgoingEdges) {
				if (edge.targetVertex in componentVertices) {
					subGraph.addEdge(sourceV, edge.label, edge.targetVertex)
				}
			}
		}
		result.add(subGraph)
	}

	return result
}

fun fnv1aHash(data: ByteArray): ULong {
	var hash = 0xCBF29CE484222325UL // FNV offset basis (64-bit)
	val fnvPrime = 0x100000001B3UL // FNV prime (64-bit)

	for (byte in data) {
		hash = hash xor byte.toULong()
		hash *= fnvPrime
	}

	return hash
}

fun <V, L : ILabel> InputGraph<V, L>.hash(): ULong {
	val edgeStrings = mutableListOf<String>()

	for (sourceV in vertices) {
		for (edge in getEdges(sourceV)) {
			val labelStr = edge.label.toString()

			edgeStrings += "$sourceV->${edge.targetVertex}[$labelStr]"
		}
	}

	edgeStrings.sort()
	val key = edgeStrings.joinToString(",")

	return fnv1aHash(key.toByteArray())
}

fun <V, L : ILabel> InputGraph<V, L>.hashGWithId(id: String): ULong {
	val edgeStrings = mutableListOf<String>()

	for (sourceV in vertices) {
		for (edge in getEdges(sourceV)) {
			val labelStr = edge.label.toString()

			edgeStrings += "$sourceV->${edge.targetVertex}[$labelStr]"
		}
	}

	edgeStrings += "($id)"
	edgeStrings.sort()

	val key = edgeStrings.joinToString(",")

	return fnv1aHash(key.toByteArray())
}
