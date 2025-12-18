package org.cfl_idr_with_gll.grammar

import org.ucfs.grammar.combinator.Grammar
import org.ucfs.grammar.combinator.regexp.Nt
import org.ucfs.rsm.symbol.Nonterminal

/**
 * Represents the states for the "valid endpoints" condition in structured equality grammars.
 *
 * The automaton tracks whether brackets have been properly opened and closed:
 * - [QE]: Start/end state (q0) - no unmatched brackets
 * - [QO]: Open state (q1) - an opening bracket has been seen
 * - [QC]: Close state (qf) - a closing bracket has been seen after opening
 */
internal enum class RState {
	QE,
	QO,
	QC
}

/**
 * Converts a parity mask integer to a string representation using 'p' (even) and 'i' (odd).
 *
 * @param mask the integer mask representing parity states (0 to 2^k-1)
 * @param k the number of parity bits/groups
 * @return a string of length k where 'p' indicates even parity and 'i' indicates odd parity
 *         for each bit position
 */
internal fun maskToParity(mask: Int, k: Int): String {
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

/**
 * Initializes a non-terminal with proper name and nonterminal symbol using reflection.
 *
 * This is a workaround for the grammar DSL's internal structure.
 *
 * @param grammar the grammar to which the non-terminal belongs
 * @param nt the non-terminal to initialize
 * @param name the name to assign to the non-terminal
 */
internal fun initNtReflectively(grammar: Grammar, nt: Nt, name: String) {
	val ntClass = Nt::class.java

	val nameField = ntClass.getDeclaredField("name")
	nameField.isAccessible = true
	nameField.set(nt, name)

	val nontermField = ntClass.getDeclaredField("nonterm")
	nontermField.isAccessible = true
	nontermField.set(nt, Nonterminal(name))

	grammar.nonTerms.add(nt)
}
