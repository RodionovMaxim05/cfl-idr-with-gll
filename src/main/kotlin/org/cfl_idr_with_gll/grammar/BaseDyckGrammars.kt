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
 * Creates a basic Dyck grammar that accepts properly nested parenthesis and bracket sequences.
 *
 * This grammar corresponds to the standard Dyck language without interleaving constraints.
 * It generates strings where parentheses and brackets are properly matched and nested.
 *
 * @param terminalFormat the format parser for generating bracket labels
 * @param parenthesesIds list of identifiers for parentheses-type brackets
 * @param bracketsIds list of identifiers for bracket-type brackets
 * @return a [Grammar] that accepts strings in the Dyck language
 */
internal fun dyckGrammar(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>
): Grammar {
	return object : Grammar() {
		val S by Nt().asStart()

		init {
			var alternatives: Regexp = Epsilon or (Term("normal") * S)

			// Adding rules for each pair of parentheses
			for (id in parenthesesIds) {
				val open = terminalFormat.generateLabel(BracketType.Parentheses, id, true)
				val close = terminalFormat.generateLabel(BracketType.Parentheses, id, false)
				alternatives = alternatives or ((Term(open) * S * Term(close)) * S)
			}

			// Adding rules for each pair of square brackets
			for (id in bracketsIds) {
				val open = terminalFormat.generateLabel(BracketType.Brackets, id, true)
				val close = terminalFormat.generateLabel(BracketType.Brackets, id, false)
				alternatives = alternatives or ((Term(open) * S * Term(close)) * S)
			}

			S /= alternatives
		}
	}
}

/**
 * Creates a projection grammar that abstracts away specific bracket labels.
 *
 * This grammar projects all opening symbols to a single abstract '(' and all closing symbols
 * to a single abstract ')', then accepts properly nested sequences. It's used for
 * unlabeled Dyck language analysis.
 *
 * @param terminalFormat the format parser for generating bracket labels
 * @param parenthesesIds list of identifiers for parentheses-type brackets
 * @param bracketsIds list of identifiers for bracket-type brackets
 * @return a [Grammar] that accepts strings with proper nesting regardless of labels
 */
internal fun dyckProjectGrammar(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>
): Grammar {
	return object : Grammar() {
		val S by Nt().asStart()

		init {
			val alternatives = mutableListOf<Regexp>(Epsilon)

			val openTerms = mutableListOf<Term<*>>()
			val closeTerms = mutableListOf<Term<*>>()

			for (parId in parenthesesIds) {
				openTerms.add(Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, true)))
				closeTerms.add(Term(terminalFormat.generateLabel(BracketType.Parentheses, parId, false)))
			}

			for (brId in bracketsIds) {
				openTerms.add(Term(terminalFormat.generateLabel(BracketType.Brackets, brId, true)))
				closeTerms.add(Term(terminalFormat.generateLabel(BracketType.Brackets, brId, false)))
			}

			if (openTerms.isNotEmpty() && closeTerms.isNotEmpty()) {
				// This corresponds to the projection of all opening symbols into one abstract '('
				var anyOpen: Regexp = openTerms[0]
				for (i in 1 until openTerms.size) {
					anyOpen = anyOpen or openTerms[i]
				}

				// This corresponds to the projection of all closing symbols into a one abstract ')'
				var anyClose: Regexp = closeTerms[0]
				for (i in 1 until closeTerms.size) {
					anyClose = anyClose or closeTerms[i]
				}

				alternatives.add((anyOpen * S * anyClose) * S)
			}

			alternatives.add(Term("normal") * S)

			var combined = alternatives[0]
			for (i in 1 until alternatives.size) {
				combined = combined or alternatives[i]
			}
			S /= combined
		}
	}
}
