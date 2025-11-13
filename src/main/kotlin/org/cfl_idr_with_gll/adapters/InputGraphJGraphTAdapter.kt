package org.cfl_idr_with_gll.adapters

import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.ucfs.input.InputGraph
import org.ucfs.input.ILabel

object InputGraphJGraphTAdapter {

	fun <V, L : ILabel> toJGraphT(inputGraph: InputGraph<V, L>)
			: DefaultDirectedGraph<V, DefaultEdge> {

		val jGraph = DefaultDirectedGraph<V, DefaultEdge>(DefaultEdge::class.java)

		inputGraph.vertices.forEach { jGraph.addVertex(it) }

		inputGraph.edges.forEach { (from, edges) ->
			edges.forEach { edge ->
				jGraph.addVertex(edge.targetVertex)
				jGraph.addEdge(from, edge.targetVertex)
			}
		}
		return jGraph
	}
}
