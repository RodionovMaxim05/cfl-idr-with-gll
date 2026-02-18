package org.cfl_idr_with_gll.grammar

import org.cfl_idr_with_gll.graph.hash
import org.cfl_idr_with_gll.graph.hashGWithId
import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.grammar.combinator.Grammar
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph
import org.ucfs.parser.Gll
import org.ucfs.sppf.node.RangeSppfNode

/**
 * Creates an Alpha grammar based on the specified grammar type and parameters.
 *
 * This function serves as a factory for Alpha grammars used in Dyck language analysis.
 * It selects the appropriate grammar generator based on the `curGrammar` parameter,
 * which determines the specific language class to analyze.
 *
 * The grammar type keys correspond to the article designations as follows:
 * - `parity`    → PAR (`D_par(Σ_α, Σ_β)`, k=1)
 * - `parity2`   → PAR2 (`D_par²(Σ_α, Σ_β)`, k=2)
 * - `parityK`   → PARk (`D_parᵏ(Σ_α, Σ_β)`, parameterized)
 * - `se`        → PAR2E (`D⁺_par²(Σ_α, Σ_β)`, k=2 + valid endpoints)
 * - `project`   → PARUnl — uses PAR grammar; the unlabeled-graph transformation
 *                 is applied externally in [mutualRefinement] via [getProjectSppf]
 * - `exclude`   → PARErase — uses PAR grammar; label-erasing iteration
 *                 is applied externally in [mutualRefinement] via [getExcludeSppf]
 * - `all`       → COM (PAR2E grammar)
 * - `parityD`   → PARD (PAR grammar, on-demand mode controlled externally)
 * - `on-demand` → COMD (COM grammar, on-demand mode controlled externally)
 *
 * Note: for `project`, `exclude`, `parityD`, `all`, and `on-demand`, the grammar itself
 * is the same as a simpler variant — the additional algorithmic logic that defines
 * these methods is handled in [mutualRefinement], not here.
 *
 * @param parenthesesIds list of identifiers for parentheses-type brackets
 * @param bracketsIds list of identifiers for bracket-type brackets
 * @param curGrammar the grammar type selector
 * @param curParityK the k parameter for parity grammars
 * @param terminalFormat the terminal format parser for generating bracket labels
 * @return a [Grammar] instance configured for the specified analysis
 */
private fun getAlphaGrammar(
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	curGrammar: String,
	curParityK: Int,
	terminalFormat: ITerminalFormat
): Grammar {
	return when (curGrammar) {
		"parity" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"parity2" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 2)
		"parityK" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, curParityK)
		"se" -> dyckAlphaGrammarKParitySe(terminalFormat, parenthesesIds, bracketsIds, 2)
		"project" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"exclude" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"all" -> dyckAlphaGrammarKParitySe(terminalFormat, parenthesesIds, bracketsIds, 2)
		else -> dyckAlphaGrammar(terminalFormat, parenthesesIds, bracketsIds)
	}
}

/**
 * Creates a Beta grammar based on the specified grammar type and parameters.
 *
 * This function serves as a factory for Beta grammars used in Dyck language analysis.
 * Beta grammars typically complement Alpha grammars in the analysis pipeline.
 *
 * The grammar type keys correspond to the article designations as follows:
 * - `parity`    → PAR (`D_par(Σ_β, Σ_α)`, k=1)
 * - `parity2`   → PAR2 (`D_par²(Σ_β, Σ_α)`, k=2)
 * - `parityK`   → PARk (`D_parᵏ(Σ_β, Σ_α)`, parameterized)
 * - `se`        → PAR2E (`D⁺_par²(Σ_β, Σ_α)`, k=2 + valid endpoints)
 * - `project`   → PARUnl — uses PAR grammar; the unlabeled-graph transformation
 *                 is applied externally in [mutualRefinement] via [getProjectSppf]
 * - `exclude`   → PARErase — uses PAR grammar; label-erasing iteration
 *                 is applied externally in [mutualRefinement] via [getExcludeSppf]
 * - `all`       → COM (PAR2E grammar)
 * - `parityD`   → PARD (PAR grammar, on-demand mode controlled externally)
 * - `on-demand` → COMD (COM grammar, on-demand mode controlled externally)
 *
 * Note: for `project`, `exclude`, `parityD`, `all`, and `on-demand`, the grammar itself
 * is the same as a simpler variant — the additional algorithmic logic that defines
 * these methods is handled in [mutualRefinement], not here.
 *
 * @param parenthesesIds list of identifiers for parentheses-type brackets
 * @param bracketsIds list of identifiers for bracket-type brackets
 * @param curGrammar the grammar type selector (same options as [getAlphaGrammar])
 * @param curParityK the k parameter for parity grammars
 * @param terminalFormat the terminal format parser for generating bracket labels
 * @return a [Grammar] instance configured for the specified analysis
 */
private fun getBetaGrammar(
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	curGrammar: String,
	curParityK: Int,
	terminalFormat: ITerminalFormat
): Grammar {
	return when (curGrammar) {
		"parity" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"parity2" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 2)
		"parityK" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, curParityK)
		"se" -> dyckBetaGrammarKParitySe(terminalFormat, parenthesesIds, bracketsIds, 2)
		"project" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"exclude" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"all" -> dyckBetaGrammarKParitySe(terminalFormat, parenthesesIds, bracketsIds, 2)
		else -> dyckBetaGrammar(terminalFormat, parenthesesIds, bracketsIds)
	}
}

/**
 * Creates an exclusion grammar for Dyck language analysis.
 *
 * Exclusion grammars are used to analyze Dyck languages while excluding
 * specific bracket identifiers from consideration.
 *
 * @param parenthesesIds list of identifiers for parentheses-type brackets
 * @param bracketsIds list of identifiers for bracket-type brackets
 * @param labelId the specific bracket identifier to exclude
 * @param terminalFormat the terminal format parser for generating bracket labels
 * @return a [Grammar] instance configured for exclusion analysis
 */
private fun getExcludeGrammar(
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	labelId: String,
	terminalFormat: ITerminalFormat
): Grammar {
	return dyckAlphaGrammarKParityExclude(terminalFormat, parenthesesIds, bracketsIds, 2, labelId)
}

/**
 * A caching mechanism for GLL parsing results to improve performance in grammar analysis.
 *
 * This class maintains separate caches for different grammar types (Alpha, Beta,
 * Projection, Exclusion) to avoid redundant parsing of identical graphs.
 * Each cache is keyed by the graph's hash value, computed using [InputGraph.hash] or
 * [InputGraph.hashGWithId].
 *
 * @param V the vertex type of the graphs being analyzed
 */
class GrammarAnalysisCache<V> {

	private val alphaCache = HashMap<ULong, Set<RangeSppfNode<V>>>()
	private val betaCache = HashMap<ULong, Set<RangeSppfNode<V>>>()
	private val projectCache = HashMap<ULong, Set<RangeSppfNode<V>>>()
	private val excludeCache = HashMap<ULong, Set<RangeSppfNode<V>>>()

	/**
	 * Clears all cached parsing results.
	 *
	 * Use this method when the cache needs to be invalidated, such as when
	 * grammars or analysis parameters change.
	 */
	fun clear() {
		alphaCache.clear()
		betaCache.clear()
		projectCache.clear()
		excludeCache.clear()
	}

	/**
	 * Retrieves or computes Alpha grammar parse results for a graph.
	 *
	 * If the graph has been parsed previously with the same parameters, returns
	 * the cached result. Otherwise, performs GLL parsing and caches the result.
	 *
	 * @param graph the input graph to parse
	 * @param parenthesesLabels list of parentheses identifiers
	 * @param bracketsLabels list of bracket identifiers
	 * @param curGrammar the grammar type selector
	 * @param curParityK the parity parameter k
	 * @param terminalFormat the terminal format parser
	 * @return a set of SPPF nodes representing parse results
	 */
	fun <L : ILabel> getAlphaSppf(
		graph: InputGraph<V, L>,
		parenthesesLabels: List<String>,
		bracketsLabels: List<String>,
		curGrammar: String,
		curParityK: Int,
		terminalFormat: ITerminalFormat
	): Set<RangeSppfNode<V>> {
		val h: ULong = graph.hash()

		return alphaCache.getOrPut(h) {
			val grammar = getAlphaGrammar(parenthesesLabels, bracketsLabels, curGrammar, curParityK, terminalFormat)

			graph.vertices.forEach { graph.addStartVertex(it) }

			val gll = Gll.gll(grammar.rsm, graph)

			gll.parse()
		}
	}

	/**
	 * Retrieves or computes Beta grammar parse results for a graph.
	 *
	 * If the graph has been parsed previously with the same parameters, returns
	 * the cached result. Otherwise, performs GLL parsing and caches the result.
	 *
	 * @param graph the input graph to parse
	 * @param parenthesesLabels list of parentheses identifiers
	 * @param bracketsLabels list of bracket identifiers
	 * @param curGrammar the grammar type selector
	 * @param curParityK the parity parameter k
	 * @param terminalFormat the terminal format parser
	 * @return a set of SPPF nodes representing parse results
	 */
	fun <L : ILabel> getBetaSppf(
		graph: InputGraph<V, L>,
		parenthesesLabels: List<String>,
		bracketsLabels: List<String>,
		curGrammar: String,
		curParityK: Int,
		terminalFormat: ITerminalFormat
	): Set<RangeSppfNode<V>> {
		val h: ULong = graph.hash()

		return betaCache.getOrPut(h) {
			val grammar = getBetaGrammar(parenthesesLabels, bracketsLabels, curGrammar, curParityK, terminalFormat)

			graph.vertices.forEach { graph.addStartVertex(it) }

			val gll = Gll.gll(grammar.rsm, graph)

			gll.parse()
		}
	}

	/**
	 * Retrieves or computes Projection grammar parse results for a graph.
	 *
	 * If the graph has been parsed previously with the same parameters, returns
	 * the cached result. Otherwise, performs GLL parsing and caches the result.
	 *
	 * @param graph the input graph to parse
	 * @param parenthesesLabels list of parentheses identifiers
	 * @param bracketsLabels list of bracket identifiers
	 * @param terminalFormat the terminal format parser
	 * @return a set of SPPF nodes representing parse results
	 */
	fun <L : ILabel> getProjectSppf(
		graph: InputGraph<V, L>,
		parenthesesLabels: List<String>,
		bracketsLabels: List<String>,
		terminalFormat: ITerminalFormat
	): Set<RangeSppfNode<V>> {
		val h: ULong = graph.hash()

		return projectCache.getOrPut(h) {
			val grammar = dyckProjectGrammar(terminalFormat, parenthesesLabels, bracketsLabels)

			graph.vertices.forEach { graph.addStartVertex(it) }

			val gll = Gll.gll(grammar.rsm, graph)

			gll.parse()
		}
	}

	/**
	 * Retrieves or computes Exclusion grammar parse results for a graph.
	 *
	 * Exclusion grammars use [InputGraph.hashGWithId] for caching, which includes
	 * the excluded label ID in the hash computation.
	 *
	 * @param graph the input graph to parse
	 * @param parenthesesLabels list of parentheses identifiers
	 * @param bracketsLabels list of bracket identifiers
	 * @param braId the bracket identifier to exclude
	 * @param terminalFormat the terminal format parser
	 * @return a set of SPPF nodes representing parse results
	 */
	fun <L : ILabel> getExcludeSppf(
		graph: InputGraph<V, L>,
		parenthesesLabels: List<String>,
		bracketsLabels: List<String>,
		braId: String,
		terminalFormat: ITerminalFormat
	): Set<RangeSppfNode<V>> {
		val h: ULong = graph.hashGWithId(braId)

		return excludeCache.getOrPut(h) {
			val grammar = getExcludeGrammar(parenthesesLabels, bracketsLabels, braId, terminalFormat)

			graph.vertices.forEach { graph.addStartVertex(it) }

			val gll = Gll.gll(grammar.rsm, graph)

			gll.parse()
		}
	}
}
