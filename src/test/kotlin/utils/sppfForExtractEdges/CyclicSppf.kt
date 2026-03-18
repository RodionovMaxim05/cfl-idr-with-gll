package utils.sppfForExtractEdges

import TestExtractEdgesFromSppfResult.MockTerminal
import org.ucfs.sppf.node.*
import org.ucfs.rsm.RsmState
import org.ucfs.rsm.symbol.Nonterminal

// https://github.com/FormalLanguageConstrainedPathQuerying/UCFS/blob/main/test-shared/src/test/resources/correctness/tree/LoopDyckGrammar/oneVertex/result.dot

fun buildCyclicSppf(): Set<RangeSppfNode<Int>> {
	val S = Nonterminal("S")
	val s0 = RsmState(S, isStart = true, isFinal = false, numId = 0)
	val s00 = RsmState(S, isStart = true, isFinal = true, numId = 0)
	val s1 = RsmState(S, isStart = false, isFinal = true, numId = 1)

	val range01 = VariadicSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s1),
		type = Range
	)

	val innerNonterm = VariadicSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s00)
	).apply {
		addChild(range01)
	}

	val terminalA = LeafSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = null,
		type = TerminalType(MockTerminal("a"))
	)

	// Make a cycle
	range01.addChild(terminalA)
	range01.addChild(innerNonterm)

	val outerNonterm = VariadicSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s0)
	).apply {
		addChild(range01)
	}

	val rootRange = VariadicSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s00, s1),
		type = Range
	).apply {
		addChild(outerNonterm)
	}

	return setOf(rootRange)
}
