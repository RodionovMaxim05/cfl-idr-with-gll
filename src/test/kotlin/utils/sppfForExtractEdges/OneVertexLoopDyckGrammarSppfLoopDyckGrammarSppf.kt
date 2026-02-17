package utils.sppfForExtractEdges

import TestExtractEdgesFromSppfResult.MockTerminal
import org.ucfs.sppf.node.*
import org.ucfs.rsm.RsmState
import org.ucfs.rsm.symbol.Nonterminal

// https://github.com/FormalLanguageConstrainedPathQuerying/UCFS/blob/main/test-shared/src/test/resources/correctness/tree/LoopDyckGrammar/oneVertex/result.dot

fun buildOneVertexLoopDyckGrammarSppf(): Set<RangeSppfNode<Int>> {
	val S = Nonterminal("S")
	val s00 = RsmState(S, isStart = true, isFinal = false, numId = 0)
	val s0 = RsmState(S, isStart = true, isFinal = true, numId = 0)
	val s1 = RsmState(S, isStart = false, isFinal = false, numId = 1)
	val s2 = RsmState(S, isStart = false, isFinal = false, numId = 2)

	val terminalOpen = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = null,
		type = TerminalType(MockTerminal("("))
	)

	// Range [0;0] -> Terminal '('
	val rangeForOpen = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s1),
		type = Range
	).apply {
		children.add(terminalOpen)
	}

	val nontermInner = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s1, s2),
		type = NonterminalType(s0)
	)

	// Range [0;0] -> Nonterminal S
	val rangeForNonterm = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s1, s2),
		type = Range
	).apply {
		children.add(nontermInner)
	}

	val intermediateInner = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s2),
		type = IntermediateType(s1, 0)
	).apply {
		children.add(rangeForOpen)
		children.add(rangeForNonterm)
	}

	val rangeIntermediateInner = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s2),
		type = Range
	).apply {
		children.add(intermediateInner)
	}

	val terminalClose = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = null,
		type = TerminalType(MockTerminal(")"))
	)

	// Range [0;0] -> Terminal ')'
	val rangeForClose = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s2, s0),
		type = Range
	).apply {
		children.add(terminalClose)
	}

	val intermediateTop = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s0),
		type = IntermediateType(s2, 0)
	).apply {
		children.add(rangeIntermediateInner)
		children.add(rangeForClose)
	}

	val epsilon00 = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s0),
		type = EpsilonNonterminalType(s0)
	)

	// Inner Range [0;0] that contains both epsilon and intermediateTop
	val innerRange00 = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s0),
		type = Range
	).apply {
		children.add(epsilon00)
		children.add(intermediateTop)
	}

	// NonterminalType at top level
	val nontermTop = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s0)
	).apply {
		children.add(innerRange00)
	}

	// Root Range [0;0]
	val rootRange = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s00, s1),
		type = Range
	).apply {
		children.add(nontermTop)
	}

	return setOf(rootRange)
}
