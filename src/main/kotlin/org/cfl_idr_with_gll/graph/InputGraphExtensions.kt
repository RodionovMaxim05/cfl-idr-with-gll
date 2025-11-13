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

fun <V, L : ILabel> InputGraph<V, L>.hash(): Int {
	val edgeStrings = mutableListOf<String>()

	for (sourceV in vertices) {
		for (edge in getEdges(sourceV)) {
			val labelStr = edge.label.toString()

			if (labelStr.isEmpty()) continue
			edgeStrings.add("$sourceV->${edge.targetVertex}[$labelStr]")
		}
	}

	edgeStrings.sort()
	val key = edgeStrings.joinToString(",")

	return key.hashCode()
}

fun <V, L : ILabel> InputGraph<V, L>.hashGWithInt(intParameter: Int): Int {
	val edgeStrings = mutableListOf<String>()

	for (sourceV in vertices) {
		for (edge in getEdges(sourceV)) {
			val labelStr = edge.label.toString()

			if (labelStr.isEmpty()) continue
			edgeStrings.add("$sourceV->${edge.targetVertex}[$labelStr]")
		}
	}

	edgeStrings.add("($intParameter)")
	edgeStrings.sort()
	val key = edgeStrings.joinToString(",")

	return key.hashCode()
}

