package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.GrammarAnalysisCache.getAlphaPaths
import org.cfl_idr_with_gll.GrammarAnalysisCache.getBetaPaths
import org.cfl_idr_with_gll.GrammarAnalysisCache.getProjectPaths
import org.cfl_idr_with_gll.graph.parseDyckComponent
import org.cfl_idr_with_gll.graph.parseDyckComponentNaive
import org.cfl_idr_with_gll.graph.splitIntoConnectedComponents
import org.cfl_idr_with_gll.graph.condensateFromUnderApprox
import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph
import org.ucfs.parser.Gll

/**
 * Represents a directed path between two vertices in a graph.
 *
 * @property source the source vertex of the path
 * @property target the target vertex of the path
 */
data class Path<V>(val source: V, val target: V)

fun <V, L : ILabel> getUnderApprox(
	graph: InputGraph<V, L>,
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): List<Path<V>> {
	val (_, _, processedGraph) = parseDyckComponentNaive(terminalFormat, graph)

	val graphComponents = processedGraph.splitIntoConnectedComponents()

	val reachablePaths = mutableListOf<Path<V>>()
	for (gComp in graphComponents) {
		val (parenthesesList, bracketsList, filteredComp) = parseDyckComponent(gComp, terminalFormat)

		val grammar = dyckGrammar(terminalFormat, parenthesesList, bracketsList)

		for (v in filteredComp.vertices) {
			filteredComp.addStartVertex(v)
		}

		val gll = Gll.gll(grammar.rsm, filteredComp)
		val sppf = gll.parse()

		val componentPaths = sppf.mapNotNull { node ->
			node.inputRange?.let { range ->
				Path(range.from, range.to)
			}
		}.filter { it.source != it.target }

		reachablePaths.addAll(componentPaths)
	}

	return reachablePaths
}

fun <V, L : ILabel> getMROverApprox(
	graph: InputGraph<V, L>,
	currGrammar: String,
	currParityK: Int,
	underApprox: List<Path<V>> = emptyList(),
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): List<Path<V>> {

	// If underApprox is passed, collapse mutually reachable vertices;
	// otherwise, simply copy the graph without merging.
	val (condensedGraph, vertexToRepresentativeMap) = if (underApprox.isNotEmpty()) {
		condensateFromUnderApprox(graph, underApprox)
	} else {
		// Each vertex is its own representative
		val representativeMap = graph.vertices.associateWith { it }
		graph to representativeMap
	}

	val refinedCondensedPaths = mutualRefinement(condensedGraph, currGrammar, currParityK, terminalFormat)

	// Create an inverse mapping: component -> vertices
	val representativeToVerticesMap = mutableMapOf<V, MutableList<V>>()
	for ((vertex, representative) in vertexToRepresentativeMap) {
		representativeToVerticesMap.getOrPut(representative) { mutableListOf() }.add(vertex)
	}

	// Recovering paths between components
	val refinedOverApproximationPaths = mutableListOf<Path<V>>()

	for (path in refinedCondensedPaths) {
		if (path.source == path.target) continue

		val sourceVertices = representativeToVerticesMap[path.source] ?: continue
		val targetVertices = representativeToVerticesMap[path.target] ?: continue

		for (sourceV in sourceVertices) {
			for (targetV in targetVertices) {
				if (sourceV == targetV) continue
				refinedOverApproximationPaths += Path(sourceV, targetV)
			}
		}
	}

	// Adding intra-cluster connections
	for (representative in condensedGraph.vertices) {
		val clusterVertices = representativeToVerticesMap[representative] ?: continue
		for (sourceV in clusterVertices) {
			for (targetV in clusterVertices) {
				if (sourceV == targetV) continue
				refinedOverApproximationPaths += Path(sourceV, targetV)
			}
		}
	}

	return refinedOverApproximationPaths
}

fun <V, L : ILabel> mutualRefinement(
	graph: InputGraph<V, L>,
	curGrammar: String,
	curParityK: Int,
	terminalFormat: ITerminalFormat,
): List<Path<V>> {
	val paths = mutableListOf<Path<V>>()
	val graphComponents = graph.splitIntoConnectedComponents()

	for (gComp in graphComponents) {
		// Parsing the Dyck component
		val (parenthesesList, bracketsList, parsedComp) = parseDyckComponent(gComp, terminalFormat)
		val initialEdgeCount = parsedComp.edges.size

		// Get alphaPaths
		val alphaSppf = getAlphaPaths(parsedComp, parenthesesList, bracketsList, curGrammar, curParityK, terminalFormat)

		// Updating the graph
		val alphaEdges = extractEdgesFromSppfResults(alphaSppf)
		val updatedAlphaGraph = createGraphFromEdges(alphaEdges)

		val (parList, braList, alphaParsedComp) = parseDyckComponent(updatedAlphaGraph, terminalFormat)

		val alphaPaths = alphaSppf.mapNotNull { node ->
			node.inputRange?.let { range ->
				Path(range.from, range.to)
			}
		}.filter { it.source != it.target }

		// Get betaPaths
		val betaSppf =
			getBetaPaths(alphaParsedComp, parList, braList, curGrammar, curParityK, terminalFormat)

		val betaEdges = extractEdgesFromSppfResults(betaSppf)
		val newBetaParsedComp = createGraphFromEdges(betaEdges)
		var (_, _, betaParsedComp) = parseDyckComponent(newBetaParsedComp, terminalFormat)

		val betaPaths = betaSppf.mapNotNull { node ->
			node.inputRange?.let { range ->
				Path(range.from, range.to)
			}
		}.filter { it.source != it.target }

		val projectPaths: List<Path<V>>
		if (curGrammar == "project" || curGrammar == "all") {
			val projectSppf = getProjectPaths(parsedComp, parList, braList, terminalFormat)

			val projectEdges = extractEdgesFromSppfResults(projectSppf)
			val newProjectParsedComp = createGraphFromEdges(projectEdges)
			val (newParList, newBraList, newParsedComp) = parseDyckComponent(newProjectParsedComp, terminalFormat)
//			parList = newParList
//			braList = newBraList
			betaParsedComp = newParsedComp

			projectPaths = projectSppf.mapNotNull { node ->
				node.inputRange?.let { range ->
					Path(range.from, range.to)
				}
			}.filter { it.source != it.target }
		} else {
			projectPaths = betaPaths
		}

//		if (curGrammar == "exclude" || curGrammar == "all") {
//			for (braId in braList) {
//				val currBraSppf = getExcludePaths(betaParsedComp, parList, braList, braId, terminalFormat)
//
//				val currBraEdges = extractEdgesFromSppfResults(currBraSppf)
//				val newExcludeParsedComp = createGraphFromEdges(currBraEdges)
//				val (newParList, newBraList, newParsedComp) = parseDyckComponent(newExcludeParsedComp, terminalFormat)
//				parList = newParList
//				braList = newBraList
//				betaParsedComp = newParsedComp
//			}
//		}

		val finalEdgeCount = betaParsedComp.edges.size

		// Check convergence
		if (finalEdgeCount == 0 || initialEdgeCount == finalEdgeCount) {
			// stability has been achieved

			val betaPathSet = betaPaths.toSet()
			val projectPathSet = projectPaths.toSet()

			for (path in alphaPaths) {
				if (path.source != path.target && betaPathSet.contains(path) && projectPathSet.contains(path)) {
					paths += path
				}
			}
		} else {
			// recursive refinement
			val refinedPaths = mutualRefinement(
				betaParsedComp,
				curGrammar,
				curParityK,
				terminalFormat
			)
			for (path in refinedPaths) {
				paths += path
			}
		}
	}

	return paths
}
