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

	val epsilon00 = LeafSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s0),
		type = EpsilonNonterminalType(s0)
	)

	val innerRange00 = VariadicSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s0),
		type = Range
	).apply { addChild(epsilon00) }

	val nonterm00 = VariadicSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s0)
	).apply { addChild(innerRange00) }

	val rangeTop00 = VariadicSppfNode(
		inputRange = InputRange(0, 0),
		rsmRange = RsmRange(s0, s1),
		type = Range
	).apply { addChild(nonterm00) }

	// -------------------
	// Branch 2: [0;2]
	// -------------------

	val epsilon11 = LeafSppfNode(
		inputRange = InputRange(1, 1),
		rsmRange = RsmRange(s0, s0),
		type = EpsilonNonterminalType(s0)
	)

	val innerRange11 = VariadicSppfNode(
		inputRange = InputRange(1, 1),
		rsmRange = RsmRange(s0, s0),
		type = Range
	).apply { addChild(epsilon11) }

	val nonterm11 = VariadicSppfNode(
		inputRange = InputRange(1, 1),
		rsmRange = RsmRange(s1, s2),
		type = NonterminalType(s0)
	).apply { addChild(innerRange11) }

	val range01ForNonterm11 = VariadicSppfNode(
		inputRange = InputRange(1, 1),
		rsmRange = RsmRange(s1, s2),
		type = Range
	).apply { addChild(nonterm11) }

	val terminalOpen = LeafSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = null,
		type = TerminalType(MockTerminal("("))
	)

	val range01InnerForTerminal = VariadicSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s1),
		type = Range
	).apply { addChild(terminalOpen) }

	val intermediateInner01 = BinarySppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s2),
		type = IntermediateType(s1, 1)
	).apply {
		addChild(range01InnerForTerminal)
		addChild(range01ForNonterm11)
	}

	val range01 = VariadicSppfNode(
		inputRange = InputRange(0, 1),
		rsmRange = RsmRange(s0, s2),
		type = Range
	).apply { addChild(intermediateInner01) }

	val terminalClose = LeafSppfNode(
		inputRange = InputRange(1, 2),
		rsmRange = null,
		type = TerminalType(MockTerminal(")"))
	)

	val range12 = VariadicSppfNode(
		inputRange = InputRange(1, 2),
		rsmRange = RsmRange(s2, s3),
		type = Range
	).apply { addChild(terminalClose) }

	val intermediateTop = BinarySppfNode(
		inputRange = InputRange(0, 2),
		rsmRange = RsmRange(s0, s3),
		type = IntermediateType(s2, 1)
	).apply {
		addChild(range01)
		addChild(range12)
	}

	val innerRange02 = VariadicSppfNode(
		inputRange = InputRange(0, 2),
		rsmRange = RsmRange(s0, s3),
		type = Range
	).apply { addChild(intermediateTop) }

	val nonterm02 = VariadicSppfNode(
		inputRange = InputRange(0, 2),
		rsmRange = RsmRange(s0, s1),
		type = NonterminalType(s0)
	).apply { addChild(innerRange02) }

	val rangeTop02 = VariadicSppfNode(
		inputRange = InputRange(0, 2),
		rsmRange = RsmRange(s0, s1),
		type = Range
	).apply { addChild(nonterm02) }

	return setOf(rangeTop00, rangeTop02)
}
