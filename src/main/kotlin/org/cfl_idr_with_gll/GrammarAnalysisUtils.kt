package org.cfl_idr_with_gll

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
		else -> dyckBetaGrammar(terminalFormat, parenthesesIds, bracketsIds)
	}
}

//fun getExcludeGrammar(
//	parenthesesIds: List<String>,
//	bracketsIds: List<String>,
//	labelId: String,
//	terminalFormat: ITerminalFormat
//): Grammar {
//	return dyckAlphaGrammarKParityExclude(terminalFormat, parenthesesIds, bracketsIds, 2, labelId)
//}

object GrammarAnalysisCache {
	fun <V, L : ILabel> getAlphaPaths(
		graph: InputGraph<V, L>,
		parenthesesLabels: List<String>,
		bracketsLabels: List<String>,
		curGrammar: String,
		curParityK: Int,
		terminalFormat: ITerminalFormat
	): Set<RangeSppfNode<V>> {
		val grammar = getAlphaGrammar(parenthesesLabels, bracketsLabels, curGrammar, curParityK, terminalFormat)

		for (v in graph.vertices) {
			graph.addStartVertex(v)
		}

		val gll = Gll.gll(grammar.rsm, graph)
		val sppf = gll.parse()

		return sppf
	}

	fun <V, L : ILabel> getBetaPaths(
		graph: InputGraph<V, L>,
		parenthesesLabels: List<String>,
		bracketsLabels: List<String>,
		curGrammar: String,
		curParityK: Int,
		terminalFormat: ITerminalFormat
	): Set<RangeSppfNode<V>> {
		val grammar = getBetaGrammar(parenthesesLabels, bracketsLabels, curGrammar, curParityK, terminalFormat)

		for (v in graph.vertices) {
			graph.addStartVertex(v)
		}

		val gll = Gll.gll(grammar.rsm, graph)
		val sppf = gll.parse()

		return sppf
	}

//	fun <V, L : ILabel> getExcludePaths(
//		graph: InputGraph<V, L>,
//		parenthesesLabels: List<String>,
//		bracketsLabels: List<String>,
//		braId: String,
//		terminalFormat: ITerminalFormat
//	): Set<RangeSppfNode<V>> {
//		val grammar = getExcludeGrammar(parenthesesLabels, bracketsLabels, braId, terminalFormat)
//
//		for (v in graph.vertices) {
//			graph.addStartVertex(v)
//		}
//
//		val gll = Gll.gll(grammar.rsm, graph)
//		val sppf = gll.parse()
//
//		return sppf
//	}
}
