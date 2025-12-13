package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.grammar.GrammarAnalysisCache
import org.cfl_idr_with_gll.grammar.dyckGrammar
import org.cfl_idr_with_gll.graph.parseDyckComponent
import org.cfl_idr_with_gll.graph.splitIntoConnectedComponents
import org.cfl_idr_with_gll.graph.condensateFromUnderApprox
import org.cfl_idr_with_gll.graph.findPMR
import org.cfl_idr_with_gll.graph.removeNotPath
import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.cfl_idr_with_gll.models.Path
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph
import org.ucfs.parser.Gll

/**
 * Computes an under-approximation of reachable paths in a graph using Dyck language analysis.
 *
 * This function analyzes the input graph to identify paths that are guaranteed to be
 * present (a conservative under-approximation).
 *
 * @param graph the input graph to analyze
 * @param terminalFormat the format parser for bracket labels (defaults to [DefaultTerminalFormat])
 * @return a set of [Path] objects representing the under-approximation of reachable paths
 */
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

/**
 * Computes a refined over-approximation of reachable paths using mutual refinement (MR) algorithm.
 *
 * This function implements the mutual refinement algorithm to compute a precise
 * over-approximation of reachable paths. It combines graph condensation with
 * iterative grammar-based analysis to refine path approximations.
 *
 * @param graph the input graph to analyze
 * @param currGrammar the grammar type to use for refinement ("classic", "all", etc.)
 * @param currParityK the parity parameter k (default: 1)
 * @param underApprox an optional under-approximation of paths for condensation
 * @param terminalFormat the format parser for bracket labels (defaults to [DefaultTerminalFormat])
 * @return a set of [Path] objects representing the refined over-approximation
 */
fun <V, L : ILabel> getMROverApprox(
	graph: InputGraph<V, L>,
	currGrammar: String,
	currParityK: Int = 1,
	underApprox: Set<Path<V>> = emptySet(),
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<V>> {
	val (_, _, parsedGraph) = parseDyckComponent(graph)

	// If underApprox is passed, collapse mutually reachable vertices;
	// otherwise, simply copy the graph without merging.
	val (condensedGraph, vertexToRepresentativeMap) = if (underApprox.isNotEmpty()) {
		condensateFromUnderApprox(parsedGraph, underApprox)
	} else {
		// Each vertex is its own representative
		val representativeMap = parsedGraph.vertices.associateWith { it }
		parsedGraph to representativeMap
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

/**
 * Performs on-demand mutual refinement using two-phase grammar analysis.
 *
 * This approach is more efficient than full mutual refinement when only
 * incremental updates are needed.
 *
 * @param graph the input graph to analyze
 * @param underApprox the current under-approximation of paths
 * @param overApprox the current over-approximation of paths
 * @param terminalFormat the format parser for bracket labels (defaults to [DefaultTerminalFormat])
 * @return a refined set of [Path] objects
 */
fun <V, L : ILabel> getOnDemandMR(
	graph: InputGraph<V, L>,
	underApprox: Set<Path<V>>,
	overApprox: Set<Path<V>>,
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<V>> {
	val (_, _, parsedGraph) = parseDyckComponent(graph)

	// Step 1: Apply "classic" grammar refinement

	val reducedGraph1 = parsedGraph.removeNotPath(overApprox)
	val (_, _, graph1) = parseDyckComponent(reducedGraph1)

	val filteredClassicPaths = refineMRWithGrammar(graph1, underApprox, overApprox, "classic", terminalFormat)

	// Step 2: Apply "all" grammar refinement on the result of step 1

	val reducedGraph2 = graph1.removeNotPath(filteredClassicPaths)
	val (_, _, graph2) = parseDyckComponent(reducedGraph2)

	val filteredOverPaths = refineMRWithGrammar(graph2, underApprox, filteredClassicPaths, "all", terminalFormat)

	return filteredOverPaths
}

/**
 * Refines path approximations using grammar-based analysis and condensation.
 *
 * This function implements the core refinement logic that:
 * 1. Condenses the graph based on the under-approximation
 * 2. Identifies unknown paths (in over-approximation but not in under-approximation)
 * 3. Processes paths in a specific order (root paths first, then derived paths)
 * 4. Uses mutual refinement to verify individual path candidates
 * 5. Caches results to avoid redundant computations
 *
 * @param graph the input graph to analyze
 * @param underApprox the current under-approximation of paths
 * @param overApprox the current over-approximation of paths
 * @param curGrammar the grammar type to use for refinement
 * @param terminalFormat the format parser for bracket labels
 * @return a refined set of [Path] objects
 */
private fun <V, L : ILabel> refineMRWithGrammar(
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

/**
 * Performs mutual refinement analysis on a graph using grammar-based reachability.
 *
 * This is the core mutual refinement algorithm that iteratively refines path
 * approximations using alternating grammar analyses (Alpha, Beta, Project, Exclude).
 * The algorithm continues until convergence (no further edge reduction) or
 * all relevant paths have been analyzed.
 *
 * The algorithm proceeds as follows for each connected component:
 * 1. **Alpha phase**: Analyze with Alpha grammar to get initial path set
 * 2. **Beta phase**: Analyze filtered graph with Beta grammar
 * 3. **Optional Project phase**: Apply projection grammar if specified
 * 4. **Optional Exclude phase**: Apply exclusion grammar if specified
 * 5. **Check convergence**: If graph unchanged, return intersection of all phases
 * 6. **Recursive refinement**: Otherwise, recurse on the refined graph
 *
 * @param graph the input graph to analyze
 * @param curGrammar the grammar type selector ("classic", "all", "project", "exclude", etc.)
 * @param curParityK the parity parameter k
 * @param sppfCache a cache for SPPF parsing results to improve performance
 * @param terminalFormat the format parser for bracket labels
 * @param checkOnePath if `true`, only check the specified `targetPath`
 * @param targetPath an optional specific path to check (used when `checkOnePath` is `true`)
 * @return a set of [Path] objects that satisfy all grammar constraints
 */
private fun <V, L : ILabel> mutualRefinement(
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
		var initialEdgeCount = 0
		parsedComp.edges.forEach { initialEdgeCount += it.value.size }

		// Get alphaPaths

		val alphaSppf =
			sppfCache.getAlphaSppf(parsedComp, parenthesesList, bracketsList, curGrammar, curParityK, terminalFormat)

		var alphaPaths = extractNonTrivialPaths(alphaSppf)

		if (checkOnePath) {
			if (!alphaPaths.contains(targetPath)) {
				return resultPaths
			}
			alphaPaths = setOf(targetPath!!)
		}

		// Updating the graph
		val alphaEdges = extractEdgesFromSppfResults(alphaSppf, targetPath)
		val updatedAlphaGraph = createGraphFromEdges(alphaEdges)
		val (alphaParList, alphaBraList, alphaParsedComp) = parseDyckComponent(updatedAlphaGraph, terminalFormat)

		// Get betaPaths

		val betaSppf =
			sppfCache.getBetaSppf(alphaParsedComp, alphaParList, alphaBraList, curGrammar, curParityK, terminalFormat)

		var betaPaths = extractNonTrivialPaths(betaSppf)

		if (checkOnePath) {
			if (!betaPaths.contains(targetPath)) {
				return resultPaths
			}
			betaPaths = setOf(targetPath!!)
		}

		val betaEdges = extractEdgesFromSppfResults(betaSppf, targetPath)
		val newBetaParsedComp = createGraphFromEdges(betaEdges)
		var (betaParList, betaBraList, betaParsedComp) = parseDyckComponent(newBetaParsedComp, terminalFormat)

		// Optional: Project phase
		var projectPaths = betaPaths
		if (curGrammar == "project" || curGrammar == "all") {
			val projectSppf = sppfCache.getProjectSppf(betaParsedComp, betaParList, betaBraList, terminalFormat)

			projectPaths = extractNonTrivialPaths(projectSppf)

			if (checkOnePath) {
				if (!projectPaths.contains(targetPath)) {
					return resultPaths
				}
				projectPaths = setOf(targetPath!!)
			}

			val projectEdges = extractEdgesFromSppfResults(projectSppf, targetPath)
			val newProjectParsedComp = createGraphFromEdges(projectEdges)
			val (newParList, newBraList, newParsedComp) = parseDyckComponent(newProjectParsedComp, terminalFormat)
			betaParList = newParList
			betaBraList = newBraList
			betaParsedComp = newParsedComp
		}

		// Optional: Exclude phase
		val excludePaths: MutableSet<Path<*>> = mutableSetOf()
		if (curGrammar == "exclude" || curGrammar == "all") {
			for ((i, braId) in betaBraList.withIndex()) {
				val excludeSppf =
					sppfCache.getExcludeSppf(betaParsedComp, betaParList, betaBraList, braId, terminalFormat)

				var curBraPaths = extractNonTrivialPaths(excludeSppf)

				if (checkOnePath) {
					if (!curBraPaths.contains(targetPath)) {
						return resultPaths
					}
					curBraPaths = setOf(targetPath!!)
				}

				val excludeEdges = extractEdgesFromSppfResults(excludeSppf, targetPath)
				val newExcludeParsedComp = createGraphFromEdges(excludeEdges)
				val (newParList, newBraList, newParsedComp) = parseDyckComponent(newExcludeParsedComp, terminalFormat)
				betaParList = newParList
				betaBraList = newBraList
				betaParsedComp = newParsedComp

				if (i == 0) {
					excludePaths.addAll(curBraPaths)
				} else {
					excludePaths.retainAll { it in curBraPaths }
				}
			}
		} else {
			excludePaths.addAll(betaPaths)
		}

		var finalEdgeCount = 0
		betaParsedComp.edges.forEach { finalEdgeCount += it.value.size }

		// Check convergence
		if (finalEdgeCount == 0 || initialEdgeCount == finalEdgeCount) {
			// Stability has been achieved

			if (checkOnePath && alphaPaths.isNotEmpty() &&
				betaPaths.isNotEmpty() && projectPaths.isNotEmpty()
			) {
				return alphaPaths
			}

			resultPaths.addAll(
				alphaPaths.intersect(betaPaths).intersect(projectPaths)
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

			if (checkOnePath) {
				if (refinedPaths.isEmpty()) {
					return emptySet()
				} else if (!refinedPaths.contains(targetPath)) {
					continue
				}
			}

			resultPaths.addAll(refinedPaths)
		}
	}

	return resultPaths
}
