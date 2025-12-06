package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.graph.parseDyckComponent
import org.cfl_idr_with_gll.graph.parseDyckComponentNaive
import org.cfl_idr_with_gll.graph.splitIntoConnectedComponents
import org.cfl_idr_with_gll.graph.condensateFromUnderApprox
import org.cfl_idr_with_gll.graph.findPMR
import org.cfl_idr_with_gll.graph.removeNotPath
import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph
import org.ucfs.parser.Gll
import org.ucfs.sppf.node.RangeSppfNode

/**
 * Represents a directed path between two vertices in a graph.
 *
 * @property source the source vertex of the path
 * @property target the target vertex of the path
 */
data class Path<V>(val source: V, val target: V)

private fun <V> extractNonTrivialPaths(sppf: Set<RangeSppfNode<V>>): Set<Path<V>> {
	return buildSet {
		for (node in sppf) {
			node.inputRange?.let { range ->
				val path = Path(range.from, range.to)
				if (path.source != path.target) {
					add(path)
				}
			}
		}
	}
}

fun <V, L : ILabel> getUnderApprox(
	graph: InputGraph<V, L>,
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<V>> {
	val graphComponents = graph.splitIntoConnectedComponents()
	val reachablePaths = mutableSetOf<Path<V>>()

	for (gComp in graphComponents) {
		val (parenthesesList, bracketsList, filteredComp) = parseDyckComponent(gComp, terminalFormat)
		val grammar = dyckGrammar(terminalFormat, parenthesesList, bracketsList)

		for (v in filteredComp.vertices) {
			filteredComp.addStartVertex(v)
		}

		val gll = Gll.gll(grammar.rsm, filteredComp)
		val sppf = gll.parse()

		val componentPaths = extractNonTrivialPaths(sppf)

		reachablePaths.addAll(componentPaths)
	}

	return reachablePaths
}

fun <V, L : ILabel> getMROverApprox(
	graph: InputGraph<V, L>,
	currGrammar: String,
	currParityK: Int,
	underApprox: Set<Path<V>> = emptySet(),
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<V>> {

	// If underApprox is passed, collapse mutually reachable vertices;
	// otherwise, simply copy the graph without merging.
	val (condensedGraph, vertexToRepresentativeMap) = if (underApprox.isNotEmpty()) {
		condensateFromUnderApprox(graph, underApprox)
	} else {
		// Each vertex is its own representative
		val representativeMap = graph.vertices.associateWith { it }
		graph to representativeMap
	}

	val sppfCache = GrammarAnalysisCache<V>()
	val refinedCondensedPaths = mutualRefinement(condensedGraph, currGrammar, currParityK, sppfCache, terminalFormat)

	// Create an inverse mapping: representative -> vertices
	val repToVerticesMap = mutableMapOf<V, MutableList<V>>()
	for ((vertex, representative) in vertexToRepresentativeMap) {
		repToVerticesMap.getOrPut(representative) { mutableListOf() }.add(vertex)
	}

	// Recovering resultPaths between components
	val refinedOverApproxPaths = mutableSetOf<Path<V>>()

	for (path in refinedCondensedPaths) {
		if (path.source == path.target) continue

		val sourceVertices = repToVerticesMap[path.source] ?: continue
		val targetVertices = repToVerticesMap[path.target] ?: continue

		for (sourceV in sourceVertices) {
			for (targetV in targetVertices) {
				if (sourceV == targetV) continue
				refinedOverApproxPaths += Path(sourceV, targetV)
			}
		}
	}

	// Adding intra-cluster connections
	for (representative in condensedGraph.vertices) {
		val clusterVertices = repToVerticesMap[representative] ?: continue
		for (sourceV in clusterVertices) {
			for (targetV in clusterVertices) {
				if (sourceV == targetV) continue
				refinedOverApproxPaths += Path(sourceV, targetV)
			}
		}
	}

	return refinedOverApproxPaths
}

fun <V, L : ILabel> getOnDemandMR(
	graph: InputGraph<V, L>,
	underApprox: Set<Path<V>>,
	overApprox: Set<Path<V>>,
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<V>> {
	// Step 1: Apply "classic" grammar refinement

	val reducedGraph1 = graph.removeNotPath(overApprox)
	val (_, _, graph1) = parseDyckComponent(reducedGraph1)

	val filteredClassicPaths = refineMRWithGrammar(graph1, underApprox, overApprox, "classic", terminalFormat)

	// Step 2: Apply "all" grammar refinement on the result of step 1

	val reducedGraph2 = graph1.removeNotPath(filteredClassicPaths)
	val (_, _, graph2) = parseDyckComponent(reducedGraph2)

	val filteredOverPaths = refineMRWithGrammar(graph2, underApprox, filteredClassicPaths, "all", terminalFormat)

	return filteredOverPaths
}

fun <V, L : ILabel> refineMRWithGrammar(
	graph: InputGraph<V, L>,
	underApprox: Set<Path<V>>,
	overApprox: Set<Path<V>>,
	curGrammar: String,
	terminalFormat: ITerminalFormat
): Set<Path<V>> {
	val (condensedGraph, parentMap) = condensateFromUnderApprox(graph, underApprox)

	// Identify unknown resultPaths: candidates not yet confirmed by under-approx
	val unknownPaths = overApprox.filter { it !in underApprox }

	// Split into root and derived resultPaths
	val rootPathsBySource = mutableMapOf<V, MutableList<Path<V>>>()
	val derivedPaths = mutableListOf<Path<V>>()

	for (path in unknownPaths) {
		val sourceRoot = findPMR(path.source, parentMap)
		val targetRoot = findPMR(path.target, parentMap)

		if (path.source == sourceRoot && path.target == targetRoot) {
			rootPathsBySource.getOrPut(path.source) { mutableListOf() }.add(path)
		} else {
			derivedPaths.add(path)
		}
	}

	// Process root resultPaths first, then derived
	val orderedUnknownPaths = buildList {
		for (list in rootPathsBySource.values) addAll(list)
		addAll(derivedPaths)
	}

	// Memory of verified PMR pairs
	val confirmedPairs = mutableMapOf<Pair<V, V>, Boolean>()
	// Already confirmed resultPaths
	val result = underApprox.toMutableSet()
	val sppfCache = GrammarAnalysisCache<V>()

	for ((idx, curPath) in orderedUnknownPaths.withIndex()) {
		// Clear cache every 10 iterations
		if (idx % 10 == 0) {
			sppfCache.clear()
		}

		val sourceRoot = findPMR(curPath.source, parentMap)
		val targetRoot = findPMR(curPath.target, parentMap)

		// Derived path: inherit result from its root pair
		if (curPath.source != sourceRoot || curPath.target != targetRoot) {
			if (confirmedPairs[Pair(sourceRoot, targetRoot)] == true) {
				result.add(curPath)
			}
			continue
		}

		// Root path: trigger mutual refinement
		val mrResult =
			mutualRefinement(
				condensedGraph,
				curGrammar,
				1,
				sppfCache,
				terminalFormat,
				true,
				Path(sourceRoot, targetRoot)
			)

		if (mrResult.isNotEmpty()) {
			confirmedPairs[Pair(sourceRoot, targetRoot)] = true
			result.add(curPath)
		}
	}

	return result
}

fun <V, L : ILabel> mutualRefinement(
	graph: InputGraph<V, L>,
	curGrammar: String,
	curParityK: Int,
	sppfCache: GrammarAnalysisCache<V>,
	terminalFormat: ITerminalFormat,
	checkOnePath: Boolean = false,
	targetPath: Path<V>? = null
): Set<Path<V>> {
	val resultPaths = mutableSetOf<Path<V>>()

	if (checkOnePath) {
		val path = targetPath ?: return resultPaths
		if (path.source !in graph.vertices || path.target !in graph.vertices) {
			return resultPaths
		}
	}

	val graphComponents = graph.splitIntoConnectedComponents()

	for (graphComp in graphComponents) {
		var gComp = graphComp

		if (checkOnePath) {
			if (targetPath!!.source !in gComp.vertices || targetPath.target !in gComp.vertices) {
				continue
			}
			gComp = gComp.removeNotPath(setOf(targetPath))
		}

		// Parsing the Dyck component
		val (parenthesesList, bracketsList, parsedComp) = parseDyckComponent(gComp, terminalFormat)
		val initialEdgeCount = parsedComp.edges.size

		// Get alphaPaths

		val alphaSppf =
			sppfCache.getAlphaPaths(parsedComp, parenthesesList, bracketsList, curGrammar, curParityK, terminalFormat)

		var alphaPaths = extractNonTrivialPaths(alphaSppf)

		if (checkOnePath) {
			if (!alphaPaths.contains(targetPath)) {
				return resultPaths
			}
			alphaPaths = setOf(targetPath!!)
		}

		// Updating the graph
		val alphaEdges = extractEdgesFromSppfResults(alphaSppf)
		val updatedAlphaGraph = createGraphFromEdges(alphaEdges)
		val (alphaParList, alphaBraList, alphaParsedComp) = parseDyckComponent(updatedAlphaGraph, terminalFormat)

		// Get betaPaths

		val betaSppf =
			sppfCache.getBetaPaths(alphaParsedComp, alphaParList, alphaBraList, curGrammar, curParityK, terminalFormat)

		var betaPaths = extractNonTrivialPaths(betaSppf)

		if (checkOnePath) {
			if (!betaPaths.contains(targetPath)) {
				return resultPaths
			}
			betaPaths = setOf(targetPath!!)
		}

		val betaEdges = extractEdgesFromSppfResults(betaSppf)
		val newBetaParsedComp = createGraphFromEdges(betaEdges)
		var (betaParList, betaBraList, betaParsedComp) = parseDyckComponent(newBetaParsedComp, terminalFormat)

		// Optional: Project phase
		var projectPaths = betaPaths
		if (curGrammar == "project" || curGrammar == "all") {
			val projectSppf = sppfCache.getProjectPaths(alphaParsedComp, betaParList, betaBraList, terminalFormat)

			projectPaths = extractNonTrivialPaths(projectSppf)

			if (checkOnePath) {
				if (!projectPaths.contains(targetPath)) {
					return resultPaths
				}
				projectPaths = setOf(targetPath!!)
			}

			val projectEdges = extractEdgesFromSppfResults(projectSppf)
			val newProjectParsedComp = createGraphFromEdges(projectEdges)
			val (newParList, newBraList, newParsedComp) = parseDyckComponent(newProjectParsedComp, terminalFormat)
			betaParList = newParList
			betaBraList = newBraList
			betaParsedComp = newParsedComp
		}

		// Optional: Exclude phase
		var excludePaths = projectPaths
		if (curGrammar == "exclude" || curGrammar == "all") {
			for (braId in betaBraList) {
				val excludeSppf =
					sppfCache.getExcludePaths(betaParsedComp, betaParList, betaBraList, braId, terminalFormat)

				excludePaths = extractNonTrivialPaths(excludeSppf)

				if (checkOnePath) {
					if (!excludePaths.contains(targetPath)) {
						return resultPaths
					}
					excludePaths = setOf(targetPath!!)
				}

				val excludeEdges = extractEdgesFromSppfResults(excludeSppf)
				val newExcludeParsedComp = createGraphFromEdges(excludeEdges)
				val (newParList, newBraList, newParsedComp) = parseDyckComponent(newExcludeParsedComp, terminalFormat)
				betaParList = newParList
				betaBraList = newBraList
				betaParsedComp = newParsedComp
			}
		}

		val finalEdgeCount = betaParsedComp.edges.size

		// Check convergence
		if (finalEdgeCount == 0 || initialEdgeCount == finalEdgeCount) {
			// Stability has been achieved

			resultPaths.addAll(
				alphaPaths.intersect(betaPaths).intersect(projectPaths).intersect(excludePaths)
			)

		} else {
			// Recursive refinement

			val refinedPaths = mutualRefinement(
				betaParsedComp,
				curGrammar,
				curParityK,
				sppfCache,
				terminalFormat
			)
			resultPaths.addAll(refinedPaths)
		}
	}

	return resultPaths
}
