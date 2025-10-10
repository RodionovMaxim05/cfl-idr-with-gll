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


	val range01 = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s1),
		type = Range
	)

	val innerNonterm = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s00)
	).apply {
		children.add(range01)
	}

	val terminalA = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = null,
		type = TerminalType(MockTerminal("a"))
	)

	// Make a cycle
	range01.children.add(terminalA)
	range01.children.add(innerNonterm)

	val outerNonterm = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s0)
	).apply {
		children.add(range01)
	}

	val rootRange = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s00, s1),
		type = Range
	).apply {
		children.add(outerNonterm)
	}

	return setOf(rootRange)
}
