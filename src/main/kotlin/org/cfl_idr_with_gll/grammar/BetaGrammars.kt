package org.cfl_idr_with_gll.grammar

import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType
import org.ucfs.grammar.combinator.Grammar
import org.ucfs.grammar.combinator.regexp.Epsilon
import org.ucfs.grammar.combinator.regexp.Nt
import org.ucfs.grammar.combinator.regexp.Regexp
import org.ucfs.grammar.combinator.regexp.or
import org.ucfs.grammar.combinator.regexp.times
import org.ucfs.rsm.symbol.Term

/**
 * Creates a Beta grammar that precisely tracks brackets but approximates parentheses.
 *
 * Beta grammars are used in the mutual refinement algorithm to:
 * 1. Precisely match brackets (require exact open/close pairs)
 * 2. Approximate parentheses (accept any parenthesis symbol individually)
 *
 * @param terminalFormat the format parser for generating bracket labels
 * @param parenthesesIds list of identifiers for parentheses-type brackets
 * @param bracketsIds list of identifiers for bracket-type brackets
 * @return a Beta [Grammar] for mutual refinement
 */
internal fun dyckBetaGrammar(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>
): Grammar {
	return object : Grammar() {
		private val S by Nt().asStart()

		init {
			var alternatives: Regexp = Epsilon or Term("normal")

			for (brId in bracketsIds) {
				val open = terminalFormat.generateLabel(BracketType.Brackets, brId, true)
				val close = terminalFormat.generateLabel(BracketType.Brackets, brId, false)
				alternatives = alternatives or ((Term(open) * S * Term(close)) * S)
			}

			for (parId in parenthesesIds) {
				val open = terminalFormat.generateLabel(BracketType.Parentheses, parId, true)
				val close = terminalFormat.generateLabel(BracketType.Parentheses, parId, false)
				alternatives = alternatives or (Term(open) * S)
				alternatives = alternatives or (Term(close) * S)
			}

			S /= alternatives
		}
	}
}

/**
 * Creates a Beta grammar with k-parity conditions for enhanced precision.
 *
 * This grammar extends the basic Beta grammar by tracking parity of parenthesis counts
 * across k groups.
 *
 * @param terminalFormat the format parser for generating bracket labels
 * @param parenthesesIds list of identifiers for parentheses-type brackets
 * @param bracketsIds list of identifiers for bracket-type brackets
 * @param k the number of parity groups (1 ≤ k ≤ number of parenthesis types)
 * @return an enhanced Beta [Grammar] with parity tracking
 */
fun dyckBetaGrammarKParity(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	k: Int
): Grammar {
	return object : Grammar() {
		val S by Nt().asStart()

		init {
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

			// First the numbers in numerical order, then the remaining strings in lexicographic order
			val sortedP = parenthesesIds.sortedWith(
				compareBy(
					{ it.toIntOrNull() == null },
					{ it.toIntOrNull() },
					{ it }
				))
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
}

/**
 * Creates a Beta grammar with k-parity conditions and structured equality (valid endpoints).
 *
 * This is the Beta counterpart to [dyckAlphaGrammarKParitySe], combining parity tracking
 * with the "valid endpoints" condition for parentheses.
 *
 * @param terminalFormat the format parser for generating bracket labels
 * @param parenthesesIds list of identifiers for parentheses-type brackets
 * @param bracketsIds list of identifiers for bracket-type brackets
 * @param k the number of parity groups
 * @return a Beta [Grammar] with parity and structured equality constraints
 */
internal fun dyckBetaGrammarKParitySe(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	k: Int
): Grammar {
	return object : Grammar() {
		val S by Nt().asStart()

		init {
			val grammar = this
			val numParityStates = 1 shl k // 2^k - Total number of parity states

			// Non-terminals are tracked: (Parity, StartState, EndState)
			// Smask_qStart_qEnd means: "The line takes the automaton from qStart to qEnd with mask parity"
			val SmapProduct = mutableMapOf<Triple<Int, RState, RState>, Nt>()

			// Create 2^k * 3 * 3 non-terminals
			for (mask in 0 until numParityStates) {
				val parityStr = maskToParity(mask, k)
				for (qStart in RState.entries) {
					for (qEnd in RState.entries) {
						val nt = Nt()
						initNtReflectively(grammar, nt, "S${parityStr}_${qStart}_${qEnd}")
						SmapProduct[Triple(mask, qStart, qEnd)] = nt
					}
				}
			}

			// First the numbers in numerical order, then the remaining strings in lexicographic order
			val sortedP = parenthesesIds.sortedWith(
				compareBy(
					{ it.toIntOrNull() == null },
					{ it.toIntOrNull() },
					{ it }
				))
			val labelGroup = mutableMapOf<String, Int>()
			for ((i, label) in sortedP.withIndex()) {
				labelGroup[label] = i % k
			}

			for (currentMask in 0 until numParityStates) {
				for (qStart in RState.entries) {
					for (qEnd in RState.entries) {
						val currentNt = SmapProduct.getValue(Triple(currentMask, qStart, qEnd))
						val alternatives = mutableListOf<Regexp>()

						// Exit Rule
						if (currentMask == 0 && qStart == qEnd) {
							alternatives.add(Epsilon)
						}

						for (parId in sortedP) {
							val group = labelGroup.getValue(parId)
							val bit = 1 shl group

							val openTerm = Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, true))
							val nextMask = currentMask xor bit
							val nextQOpen = RState.QO
							alternatives.add(openTerm * SmapProduct.getValue(Triple(nextMask, nextQOpen, qEnd)))

							if (qStart != RState.QE) {
								val closeTerm =
									Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, false))
								val nextQClose = RState.QC
								alternatives.add(closeTerm * SmapProduct.getValue(Triple(nextMask, nextQClose, qEnd)))
							}
						}

						for (brId in bracketsIds) {
							val openTerm = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, true))
							val closeTerm = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, false))

							for (innerMask in 0 until numParityStates) {
								// We don't know in what state the internal block will end
								// Therefore, we iterate through all possible intermediate states of qMid
								for (qMid in RState.entries) {
									val S_inner = SmapProduct.getValue(Triple(innerMask, qStart, qMid))

									val nextMask = currentMask xor innerMask
									val S_next = SmapProduct.getValue(Triple(nextMask, qMid, qEnd))

									alternatives.add((openTerm * S_inner * closeTerm) * S_next)
								}
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
			}

			// Valid lines in PARkE start in QE and end either in QE or in QC
			val S_QE_QE = SmapProduct.getValue(Triple(0, RState.QE, RState.QE))
			val S_QE_QC = SmapProduct.getValue(Triple(0, RState.QE, RState.QC))

			S /= S_QE_QE or S_QE_QC
		}
	}
}
