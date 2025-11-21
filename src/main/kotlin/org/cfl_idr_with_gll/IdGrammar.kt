package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.grammar.combinator.Grammar
import org.ucfs.grammar.combinator.regexp.*
import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType
import org.ucfs.rsm.symbol.Nonterminal
import org.ucfs.rsm.symbol.Term
import kotlin.collections.mutableMapOf
import kotlin.collections.set

fun dyckGrammar(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>
): Grammar {
	return object : Grammar() {
		val S by Nt().asStart()

		init {
			var alternatives: Regexp = Epsilon
			alternatives = alternatives or Term("normal")
			alternatives = alternatives or (S * S)

			// Adding rules for each pair of parentheses
			for (id in parenthesesIds) {
				val open = terminalFormat.generateLabel(BracketType.Parentheses, id, true)
				val close = terminalFormat.generateLabel(BracketType.Parentheses, id, false)
				alternatives = alternatives or (Term(open) * S * Term(close))
			}

			// Adding rules for each pair of square brackets
			for (id in bracketsIds) {
				val open = terminalFormat.generateLabel(BracketType.Brackets, id, true)
				val close = terminalFormat.generateLabel(BracketType.Brackets, id, false)
				alternatives = alternatives or (Term(open) * S * Term(close))
			}

			S /= alternatives
		}
	}
}

fun dyckAlphaGrammar(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>
): Grammar {
	return object : Grammar() {
		val S by Nt().asStart()

		init {
			var alternatives: Regexp = Epsilon
			alternatives = alternatives or Term("normal")
			alternatives = alternatives or (S * S)

			// labeled parentheses: require exact open/close labels
			for (id in parenthesesIds) {
				val open = terminalFormat.generateLabel(BracketType.Parentheses, id, true)
				val close = terminalFormat.generateLabel(BracketType.Parentheses, id, false)
				alternatives = alternatives or (Term(open) * S * Term(close))
			}

			for (id in bracketsIds) {
				val open = terminalFormat.generateLabel(BracketType.Brackets, id, true)
				val close = terminalFormat.generateLabel(BracketType.Brackets, id, false)
				alternatives = alternatives or Term(open)
				alternatives = alternatives or Term(close)
			}

			S /= alternatives
		}
	}
}

private fun maskToParity(mask: Int, k: Int): String {
	val sb = StringBuilder()

	for (i in 0 until k) {
		if (((mask shr i) % 2) == 0) {
			sb.append("p")
		} else {
			sb.append("i")
		}
	}

	return sb.toString()
}

private fun initNtReflectively(grammar: Grammar, nt: Nt, name: String) {
	val ntClass = Nt::class.java

	val nameField = ntClass.getDeclaredField("name")
	nameField.isAccessible = true
	nameField.set(nt, name)

	val nontermField = ntClass.getDeclaredField("nonterm")
	nontermField.isAccessible = true
	nontermField.set(nt, Nonterminal(name))

	grammar.nonTerms.add(nt)
}

/**
 * Generated Grammar Example
 * Parameters: k=2, brackets=[1,2], parentheses=[1,2]
 *
 * States (Parity Masks):
 * Spp = 00 (Balanced)
 * Sip = 10 (Parity 1 is odd)
 * Spi = 01 (Parity 2 is odd)
 * Sii = 11 (Both odd)
 *
 * Rules Structure:
 * 1. Spp (Target: 00)
 * -> Epsilon                 // End of string allowed
 * -> [_1 * Sip | ]_1 * Sip   // Bit 1 flip: 00 ^ 10 = 10 (Sip)
 * -> [_2 * Spi | ]_2 * Spi   // Bit 2 flip: 00 ^ 01 = 01 (Spi)
 * Parentheses (XOR Logic: Target = Inner ^ Next)
 * -> (_n * Spp * )_n * Spp   // 00 ^ 00 = 00
 * -> (_n * Sip * )_n * Sip   // 10 ^ 10 = 00
 * -> (_n * Spi * )_n * Spi   // 01 ^ 01 = 00
 * -> (_n * Sii * )_n * Sii   // 11 ^ 11 = 00
 * -> normal * Spp
 *
 * 2. Sip (Target: 10)
 * -> [_1 * Spp | ]_1 * Spp   // Bit 1 flip: 10 ^ 10 = 00 (Spp)
 * -> [_2 * Sii | ]_2 * Sii   // Bit 2 flip: 10 ^ 01 = 11 (Sii)
 * -> (_n * Spp * )_n * Sip   // 00 ^ 10 = 10
 * -> (_n * Sip * )_n * Spp   // 10 ^ 00 = 10
 * -> (_n * Spi * )_n * Sii   // 01 ^ 11 = 10
 * -> (_n * Sii * )_n * Spi   // 11 ^ 01 = 10
 * -> normal * Sip
 *
 * 3. Spi (Target: 01)
 * -> [_1 * Sii | ]_1 * Sii   // Bit 1 flip: 01 ^ 10 = 11 (Sii)
 * -> [_2 * Spp | ]_2 * Spp   // Bit 2 flip: 01 ^ 01 = 00 (Spp)
 * -> (_n * Spp * )_n * Spi   // 00 ^ 01 = 01
 * -> (_n * Sip * )_n * Sii   // 10 ^ 11 = 01
 * -> (_n * Spi * )_n * Spp   // 01 ^ 00 = 01
 * -> (_n * Sii * )_n * Sip   // 11 ^ 10 = 01
 * -> normal * Spi
 *
 * 4. Sii (Target: 11)
 * -> [_1 * Spi | ]_1 * Spi   // Bit 1 flip: 11 ^ 10 = 01 (Spi)
 * -> [_2 * Sip | ]_2 * Sip   // Bit 2 flip: 11 ^ 01 = 10 (Sip)
 * -> (_n * Spp * )_n * Sii   // 00 ^ 11 = 11
 * -> (_n * Sip * )_n * Spi   // 10 ^ 01 = 11
 * -> (_n * Spi * )_n * Sip   // 01 ^ 10 = 11
 * -> (_n * Sii * )_n * Spp   // 11 ^ 00 = 11
 * -> normal * Sii
 */
fun dyckAlphaGrammarKParity(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	k: Int
): Grammar {

	return (object : Grammar() {
		val S by Nt().asStart()
	}).apply {
		val grammar = this
		val numStates = 1 shl k // 2^k - Total number of parity states

		// Create 2^k non-terminals (states)
		// Each state S_mask corresponds to a row where the parity
		// of the k brackets is equal to the 'mask'
		val Smap = buildMap<Int, Nt> {
			for (mask in 0 until numStates) {
				val p = maskToParity(mask, k)
				val nt = Nt()
				initNtReflectively(grammar, nt, "S$p")
				put(mask, nt)
			}
		}

		val sortedB = bracketsIds.sorted()
		val labelGroup = mutableMapOf<String, Int>()
		// Assign each group of brackets the corresponding bit (group)
		for ((i, label) in sortedB.withIndex()) {
			labelGroup[label] = i % k
		}

		for (currentMask in 0 until numStates) {
			val currentNt = Smap.getValue(currentMask)
			val alternatives = mutableListOf<Regexp>()

			// Exit Rule (Eq 10)
			if (currentMask == 0) {
				alternatives.add(Epsilon)
			}

			// Rules for BRACKETS - Beta (Eq 12, 13)
			// Brackets consume a token and change state (XOR by 1 bit)
			// Rule: Target -> Terminal * Next, where Target = Bit ^ Next
			for (brId in sortedB) {
				val group = labelGroup.getValue(brId)
				val bit = 1 shl group

				val nextMask = currentMask xor bit
				val nextNt = Smap.getValue(nextMask)

				val open = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, true))
				val close = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, false))

				alternatives.add(open * nextNt)
				alternatives.add(close * nextNt)
			}

			// Rules for PARENTHESES - Alpha (Eq 11 + 14)
			// Parentheses are transparent (do not add their parity) and contain a substring.
			// Rule: Target -> ( Inner ) * Next, where Target = Inner ^ Next
			for (parId in parenthesesIds) {
				val open = Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, true))
				val close = Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, false))

				// We iterate over all possible masks for the content INSIDE the parentheses (innerMask)
				for (innerMask in 0 until numStates) {
					val S_inner = Smap.getValue(innerMask)

					val nextMask = currentMask xor innerMask
					val S_next = Smap.getValue(nextMask)

					alternatives.add((open * S_inner * close) * S_next)
				}
			}

			alternatives.add(Term("normal") * currentNt)

			// Combine all alternatives into one rule for the current non-terminal
			if (alternatives.isNotEmpty()) {
				var combined = alternatives[0]
				for (i in 1 until alternatives.size) {
					combined = combined or alternatives[i]
				}
				currentNt /= combined
			}
		}

		// The starting rule S must lead to a fully balanced state (mask 0)
		S /= Smap.getValue(0)
	}
}

enum class RState {
	QE, // Equivalent (q0): start
	QO, // Open (q1): seen open brackets
	QC  // Close (qf): seen close brackets, valid endpoint
}

fun dyckAlphaGrammarKParitySe(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	k: Int
): Grammar {

	return (object : Grammar() {
		val S by Nt().asStart()
	}).apply {
		val grammar = this
		val numParityStates = 1 shl k // 2^k - Total number of parity states

		val SmapProduct = mutableMapOf<Pair<Int, RState>, Nt>()

		// Create all 2^k * 3 non-terminals
		for (mask in 0 until numParityStates) {
			val parityStr = maskToParity(mask, k)
			for (qState in RState.entries) {
				val nt = Nt()
				initNtReflectively(grammar, nt, "S${parityStr}${qState}")
				SmapProduct[Pair(mask, qState)] = nt
			}
		}

		val sortedB = bracketsIds.sorted()
		val labelGroup = mutableMapOf<String, Int>()
		for ((i, label) in sortedB.withIndex()) {
			labelGroup[label] = i % k
		}

		for (currentMask in 0 until numParityStates) {
			for (currentQ in RState.entries) {
				val currentNt = SmapProduct.getValue(Pair(currentMask, currentQ))
				val alternatives = mutableListOf<Regexp>()

				// Exit Rule
				if (currentMask == 0 && (currentQ == RState.QE || currentQ == RState.QC)) {
					alternatives.add(Epsilon)
				}

				for (brId in sortedB) {
					val group = labelGroup.getValue(brId)
					val bit = 1 shl group

					val openTerm = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, true))
					val nextParityMask = currentMask xor bit
					val nextQOpen = RState.QO

					alternatives.add(openTerm * SmapProduct.getValue(Pair(nextParityMask, nextQOpen)))

					if (currentQ != RState.QE) {
						val closeTerm = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, false))
						val nextQClose = RState.QC

						alternatives.add(closeTerm * SmapProduct.getValue(Pair(nextParityMask, nextQClose)))
					}
				}


				for (id in parenthesesIds) {
					val openTerm = Term(terminalFormat.generateLabel(BracketType.Parentheses, id, true))
					val closeTerm = Term(terminalFormat.generateLabel(BracketType.Parentheses, id, false))

					for (innerMask in 0 until numParityStates) {
						val S_inner = SmapProduct.getValue(Pair(innerMask, RState.QE))

						val nextParityMask = currentMask xor innerMask
						val S_next = SmapProduct.getValue(Pair(nextParityMask, currentQ))

						alternatives.add((openTerm * S_inner * closeTerm) * S_next)
					}
				}

				alternatives.add(Term("normal") * currentNt)

				if (alternatives.isNotEmpty()) {
					var combined = alternatives[0]
					for (i in 1 until alternatives.size) {
						combined = combined or alternatives[i]
					}
					currentNt /= combined
				}
			}
		}

		S /= SmapProduct.getValue(Pair(0, RState.QE))
	}
}

fun dyckBetaGrammar(terminalFormat: ITerminalFormat, parentheses: List<String>, brackets: List<String>): Grammar {
	return object : Grammar() {
		private val S by Nt().asStart()

		init {
			var alternatives: Regexp = Epsilon
			alternatives = alternatives or Term("normal")
			alternatives = alternatives or (S * S)

			for (id in brackets) {
				val open = terminalFormat.generateLabel(BracketType.Brackets, id, true)
				val close = terminalFormat.generateLabel(BracketType.Brackets, id, false)
				alternatives = alternatives or (Term(open) * S * Term(close))
			}

			for (id in parentheses) {
				val open = terminalFormat.generateLabel(BracketType.Parentheses, id, true)
				val close = terminalFormat.generateLabel(BracketType.Parentheses, id, false)
				alternatives = alternatives or Term(open)
				alternatives = alternatives or Term(close)
			}

			S /= alternatives
		}
	}
}

fun dyckBetaGrammarKParity(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	k: Int
): Grammar {

	return (object : Grammar() {
		val S by Nt().asStart()
	}).apply {
		val grammar = this
		val numStates = 1 shl k  // 2^k - Total number of parity states

		// Create 2^k non-terminals (states)
		// Each state S_mask corresponds to a row where the parity
		// of the k parentheses is equal to the 'mask'
		val Smap = buildMap<Int, Nt> {
			for (mask in 0 until numStates) {
				val p = maskToParity(mask, k)
				val nt = Nt()
				initNtReflectively(grammar, nt, "S$p")
				put(mask, nt)
			}
		}

		val sortedP = parenthesesIds.sorted()
		val labelGroup = mutableMapOf<String, Int>()
		// Assign each group of parentheses the corresponding bit (group)
		for ((i, label) in sortedP.withIndex()) {
			labelGroup[label] = i % k
		}

		for (currentMask in 0 until numStates) {
			val currentNt = Smap.getValue(currentMask)
			val alternatives = mutableListOf<Regexp>()

			// Exit Rule (Eq 10)
			if (currentMask == 0) {
				alternatives.add(Epsilon)
			}

			// Rules for PARENTHESES - analogically Beta (Eq 12, 13)
			// Parentheses consume a token and change state (XOR by 1 bit)
			// Rule: Target -> Terminal * Next, where Target = Bit ^ Next
			for (parId in sortedP) {
				val group = labelGroup.getValue(parId)
				val bit = 1 shl group

				val nextMask = currentMask xor bit
				val nextNt = Smap.getValue(nextMask)

				val open = Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, true))
				val close = Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, false))

				alternatives.add(open * nextNt)
				alternatives.add(close * nextNt)
			}

			// Rules for BRACKETS - analogically Alpha (Eq 11 + 14)
			// Brackets are transparent (do not add their parity) and contain a substring.
			// Rule: Target -> ( Inner ) * Next, where Target = Inner ^ Next
			for (brId in bracketsIds) {
				val open = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, true))
				val close = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, false))

				// We iterate over all possible masks for the content INSIDE the brackets (innerMask)
				for (innerMask in 0 until numStates) {
					val S_inner = Smap.getValue(innerMask)

					val nextMask = currentMask xor innerMask
					val S_next = Smap.getValue(nextMask)

					alternatives.add((open * S_inner * close) * S_next)
				}
			}

			alternatives.add(Term("normal") * currentNt)

			// Combine all alternatives into one rule for the current non-terminal
			if (alternatives.isNotEmpty()) {
				var combined = alternatives[0]
				for (i in 1 until alternatives.size) {
					combined = combined or alternatives[i]
				}
				currentNt /= combined
			}
		}

		// The starting rule S must lead to a fully balanced state (mask 0)
		S /= Smap.getValue(0)
	}
}

fun dyckBetaGrammarKParitySe(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	k: Int
): Grammar {

	return (object : Grammar() {
		val S by Nt().asStart()
	}).apply {
		val grammar = this
		val numParityStates = 1 shl k // 2^k - Total number of parity states

		val SmapProduct = mutableMapOf<Pair<Int, RState>, Nt>()

		// Create all 2^k * 3 non-terminals
		for (mask in 0 until numParityStates) {
			val parityStr = maskToParity(mask, k)
			for (qState in RState.entries) {
				val nt = Nt()
				initNtReflectively(grammar, nt, "S${parityStr}${qState}")
				SmapProduct[Pair(mask, qState)] = nt
			}
		}

		val sortedP = parenthesesIds.sorted()
		val labelGroup = mutableMapOf<String, Int>()
		for ((i, label) in sortedP.withIndex()) {
			labelGroup[label] = i % k
		}

		for (currentMask in 0 until numParityStates) {
			for (currentQ in RState.entries) {
				val currentNt = SmapProduct.getValue(Pair(currentMask, currentQ))
				val alternatives = mutableListOf<Regexp>()

				// Exit Rule
				if (currentMask == 0 && (currentQ == RState.QE || currentQ == RState.QC)) {
					alternatives.add(Epsilon)
				}

				for (parId in sortedP) {
					val group = labelGroup.getValue(parId)
					val bit = 1 shl group

					val openTerm = Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, true))
					val nextParityMask = currentMask xor bit
					val nextQOpen = RState.QO

					alternatives.add(openTerm * SmapProduct.getValue(Pair(nextParityMask, nextQOpen)))

					if (currentQ != RState.QE) {
						val closeTerm = Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, false))
						val nextQClose = RState.QC

						alternatives.add(closeTerm * SmapProduct.getValue(Pair(nextParityMask, nextQClose)))
					}
				}


				for (brId in bracketsIds) {
					val openTerm = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, true))
					val closeTerm = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, false))

					for (innerMask in 0 until numParityStates) {
						val S_inner = SmapProduct.getValue(Pair(innerMask, RState.QE))

						val nextParityMask = currentMask xor innerMask
						val S_next = SmapProduct.getValue(Pair(nextParityMask, currentQ))

						alternatives.add((openTerm * S_inner * closeTerm) * S_next)
					}
				}

				alternatives.add(Term("normal") * currentNt)

				if (alternatives.isNotEmpty()) {
					var combined = alternatives[0]
					for (i in 1 until alternatives.size) {
						combined = combined or alternatives[i]
					}
					currentNt /= combined
				}
			}
		}

		S /= SmapProduct.getValue(Pair(0, RState.QE))
	}
}
