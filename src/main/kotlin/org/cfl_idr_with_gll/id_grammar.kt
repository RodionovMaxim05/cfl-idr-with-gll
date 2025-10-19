package org.cfl_idr_with_gll

import org.ucfs.grammar.combinator.Grammar
import org.ucfs.grammar.combinator.regexp.*
import org.ucfs.rsm.symbol.Term

/**
 * A context-free grammar for the Dyck language supporting two kinds of brackets:
 * - Parentheses: op--N or cp--N
 * - Square brackets: ob--N or cb--N
 *
 * @param parenthesesIds list of IDs for parentheses (op--N | cp--N)
 * @param bracketsIds list of IDs for square bracket pairs (ob--N | cb--N)
 */
class DyckGrammar(
	private val parenthesesIds: List<Int> = emptyList(),
	private val bracketsIds: List<Int> = emptyList()
) : Grammar() {

	val S by Nt().asStart()

	init {
		val alternatives = mutableListOf<Regexp>()
		alternatives += Epsilon
		alternatives += Term("normal")
		alternatives += S * S

		// Adding rules for each pair of parentheses
		for (id in parenthesesIds) {
			val open = "op--$id"
			val close = "cp--$id"
			alternatives += Term(open) * S * Term(close)
		}

		// Adding rules for each pair of square brackets
		for (id in bracketsIds) {
			val open = "ob--$id"
			val close = "cb--$id"
			alternatives += Term(open) * S * Term(close)
		}

		S /= alternatives.reduce { acc, expr -> acc or expr }
	}
}