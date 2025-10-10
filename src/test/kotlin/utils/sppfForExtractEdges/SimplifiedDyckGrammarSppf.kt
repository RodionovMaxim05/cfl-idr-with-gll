package utils.sppfForExtractEdges

import TestExtractEdgesFromSppfResult.MockTerminal
import org.ucfs.sppf.node.*
import org.ucfs.rsm.RsmState
import org.ucfs.rsm.symbol.Nonterminal

// https://github.com/FormalLanguageConstrainedPathQuerying/UCFS/blob/main/test-shared/src/test/resources/correctness/tree/SimplifiedDyckGrammar/linear/result.dot

fun buildSimplifiedDyckGrammarSppf(): Set<RangeSppfNode<Int>> {
	val S = Nonterminal("S")
	val s0 = RsmState(S, isStart = true, isFinal = true, numId = 0)
	val s1 = RsmState(S, isStart = false, isFinal = false, numId = 1)
	val s2 = RsmState(S, isStart = false, isFinal = false, numId = 2)
	val s3 = RsmState(S, isStart = false, isFinal = false, numId = 3)

	// -------------------
	// Branch 1: [0;0] -> Nonterminal -> Range -> Epsilon
	// -------------------

	val epsilon00 = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s0),
		type = EpsilonNonterminalType(s0)
	)

	val innerRange00 = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s0),
		type = Range
	).apply { children.add(epsilon00) }

	val nonterm00 = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s0)
	).apply { children.add(innerRange00) }

	val rangeTop00 = RangeSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s1),
		type = Range
	).apply { children.add(nonterm00) }

	// -------------------
	// Branch 2: [0;2]
	// -------------------

	val epsilon11 = RangeSppfNode(
		inputRange = InputRange(1, 1),
		rsmRange = RsmRange(s0, s0),
		type = EpsilonNonterminalType(s0)
	)

	val innerRange11 = RangeSppfNode(
		inputRange = InputRange(1, 1),
		rsmRange = RsmRange(s0, s0),
		type = Range
	).apply { children.add(epsilon11) }

	val nonterm11 = RangeSppfNode(
		inputRange = InputRange(1, 1),
		rsmRange = RsmRange(s1, s2),
		type = NonterminalType(s0)
	).apply { children.add(innerRange11) }

	val range01ForNonterm11 = RangeSppfNode(
		inputRange = InputRange(1, 1),
		rsmRange = RsmRange(s1, s2),
		type = Range
	).apply { children.add(nonterm11) }

	val terminalOpen = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = null,
		type = TerminalType(MockTerminal("("))
	)

	val range01InnerForTerminal = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s1),
		type = Range
	).apply { children.add(terminalOpen) }

	val intermediateInner01 = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s2),
		type = IntermediateType(s1, 1)
	).apply {
		children.add(range01InnerForTerminal)
		children.add(range01ForNonterm11)
	}

	val range01 = RangeSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s2),
		type = Range
	).apply { children.add(intermediateInner01) }

	val terminalClose = RangeSppfNode(
		inputRange = InputRange(1, 2),
		rsmRange = null,
		type = TerminalType(MockTerminal(")"))
	)

	val range12 = RangeSppfNode(
		inputRange = InputRange(1, 2),
		rsmRange = RsmRange(s2, s3),
		type = Range
	).apply { children.add(terminalClose) }

	val intermediateTop = RangeSppfNode(
		inputRange = InputRange(0, 2),
		rsmRange = RsmRange(s0, s3),
		type = IntermediateType(s2, 1)
	).apply {
		children.add(range01)
		children.add(range12)
	}

	val innerRange02 = RangeSppfNode(
		inputRange = InputRange(0, 2),
		rsmRange = RsmRange(s0, s3),
		type = Range
	).apply { children.add(intermediateTop) }

	val nonterm02 = RangeSppfNode(
		inputRange = InputRange(0, 2),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s0)
	).apply { children.add(innerRange02) }

	val rangeTop02 = RangeSppfNode(
		inputRange = InputRange(0, 2),
		rsmRange = RsmRange(s0, s1),
		type = Range
	).apply { children.add(nonterm02) }

	return setOf(rangeTop00, rangeTop02)
}
