package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.graph.hash
import org.cfl_idr_with_gll.graph.hashGWithId
import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.grammar.combinator.Grammar
import org.ucfs.input.ILabel
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.parser.Gll
import org.ucfs.sppf.node.RangeSppfNode

fun <V> createGraphFromEdges(
	edges: Set<SppfEdge<V>>
): InputGraph<V, TerminalInputLabel> {
	val graph = InputGraph<V, TerminalInputLabel>()

	for (edge in edges) {
		graph.addVertex(edge.from)
		graph.addVertex(edge.to)
		graph.addEdge(edge.from, TerminalInputLabel(edge.label), edge.to)
	}

	return graph
}

fun getAlphaGrammar(
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	curGrammar: String,
	curParityK: Int,
	terminalFormat: ITerminalFormat
): Grammar {
	return when (curGrammar) {
		"parityK" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, curParityK)
		"parity2" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 2)
		"se" -> dyckAlphaGrammarKParitySe(terminalFormat, parenthesesIds, bracketsIds, 2)
		"project" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"exclude" -> dyckAlphaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"all" -> dyckAlphaGrammarKParitySe(terminalFormat, parenthesesIds, bracketsIds, 2)
		else -> dyckAlphaGrammar(terminalFormat, parenthesesIds, bracketsIds)
	}
}

fun getBetaGrammar(
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	curGrammar: String,
	curParityK: Int,
	terminalFormat: ITerminalFormat
): Grammar {
	return when (curGrammar) {
		"parityK" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, curParityK)
		"parity2" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 2)
		"se" -> dyckBetaGrammarKParitySe(terminalFormat, parenthesesIds, bracketsIds, 2)
		"project" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"exclude" -> dyckBetaGrammarKParity(terminalFormat, parenthesesIds, bracketsIds, 1)
		"all" -> dyckBetaGrammarKParitySe(terminalFormat, parenthesesIds, bracketsIds, 2)
		else -> dyckBetaGrammar(terminalFormat, parenthesesIds, bracketsIds)
	}
}

fun getProjectGrammar(
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	terminalFormat: ITerminalFormat
): Grammar {
	return dyckProjectGrammar(terminalFormat, parenthesesIds, bracketsIds)
}

fun getExcludeGrammar(
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	labelId: String,
	terminalFormat: ITerminalFormat
): Grammar {
	return dyckAlphaGrammarKParityExclude(terminalFormat, parenthesesIds, bracketsIds, 2, labelId)
}

class GrammarAnalysisCache<V> {

	private val alphaCache = mutableMapOf<ULong, Set<RangeSppfNode<V>>>()
	private val betaCache = mutableMapOf<ULong, Set<RangeSppfNode<V>>>()
	private val projectCache = mutableMapOf<ULong, Set<RangeSppfNode<V>>>()
	private val excludeCache = mutableMapOf<ULong, Set<RangeSppfNode<V>>>()

	fun clear() {
		alphaCache.clear()
		betaCache.clear()
		projectCache.clear()
		excludeCache.clear()
	}

	fun <L : ILabel> getAlphaPaths(
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

	fun <L : ILabel> getBetaPaths(
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

	fun <L : ILabel> getProjectPaths(
		graph: InputGraph<V, L>,
		parenthesesLabels: List<String>,
		bracketsLabels: List<String>,
		terminalFormat: ITerminalFormat
	): Set<RangeSppfNode<V>> {
		val h: ULong = graph.hash()

		return projectCache.getOrPut(h) {
			val grammar = getProjectGrammar(parenthesesLabels, bracketsLabels, terminalFormat)

			graph.vertices.forEach { graph.addStartVertex(it) }

			val gll = Gll.gll(grammar.rsm, graph)

			gll.parse()
		}
	}

	fun <L : ILabel> getExcludePaths(
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
