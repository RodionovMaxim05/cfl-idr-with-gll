package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.terminal.ITerminalFormat
import org.ucfs.grammar.combinator.Grammar
import org.ucfs.grammar.combinator.regexp.*
import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType
import org.ucfs.rsm.symbol.Nonterminal
import org.ucfs.rsm.symbol.Term

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

fun dyckAlphaGrammarKParity(
	terminalFormat: ITerminalFormat,
	parenthesesIds: List<String>,
	bracketsIds: List<String>,
	k: Int
): Grammar {
	
	return (object : Grammar() {
		val S by Nt().asStart()
		val Se by Nt()
		val Eps by Nt(Epsilon, "Eps")
	}).apply {

		val grammar = this
		val Smap = buildMap<Int, Nt> {
			for (mask in 0 until (1 shl k)) {
				val p = maskToParity(mask, k)
				val nt = Nt()
				initNtReflectively(grammar, nt, "S$p")
				put(mask, nt)
			}
		}

		val sortedB = bracketsIds.sorted()
		val labelGroup = mutableMapOf<String, Int>()
		for ((i, label) in sortedB.withIndex()) {
			labelGroup[label] = i % k
		}

		var SeAlt: Regexp = Epsilon or Term("normal") or (Se * Se)
		val sAlts = mutableMapOf<Int, Regexp>()
		for ((mask, _) in Smap) sAlts[mask] = Epsilon or Term("normal")

		var emptyMask = 0
		for (brId in sortedB) {
			val group = labelGroup.getValue(brId)
			emptyMask += 1 shl group

			val openB = Term(terminalFormat.generateLabel(BracketType.Parentheses, brId, true))
			val closeB = Term(terminalFormat.generateLabel(BracketType.Parentheses, brId, false))
			sAlts[emptyMask] = sAlts.getValue(emptyMask) or openB or closeB

			emptyMask -= 1 shl group
		}

		if (parenthesesIds.isNotEmpty()) {
			for (id in parenthesesIds) {
				val open = Term(terminalFormat.generateLabel(BracketType.Brackets, id, true))
				val close = Term(terminalFormat.generateLabel(BracketType.Brackets, id, false))

				SeAlt = SeAlt or (open * Se * close)
				for ((mask, alt) in sAlts) {
					sAlts[mask] = alt or (open * Smap.getValue(mask) * close)
				}
			}
		}

		for (mask1 in 0 until (1 shl k)) {
			val S1 = Smap[mask1]!!
			sAlts[mask1] = sAlts.getValue(mask1) or (S1 * Se) or (Se * S1)
			for (mask2 in 0 until (1 shl k)) {
				val S2 = Smap[mask2]!!
				val mask3 = mask1 xor mask2
				sAlts[mask3] = sAlts.getValue(mask3) or (S1 * S2)
			}
		}

		Se /= SeAlt
		for ((mask, alt) in sAlts) {
			Smap.getValue(mask) /= alt
		}

		val S0 = Smap.getValue(0)
		S /= (Se * Eps) or (S0 * Eps)
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
		val Se by Nt()
		val Eps by Nt(Epsilon, "Eps")
	}).apply {

		val grammar = this

		val Smap = buildMap<Int, Nt> {
			for (mask in 0 until (1 shl k)) {
				val p = maskToParity(mask, k)
				val nt = Nt()
				initNtReflectively(grammar, nt, "S$p")
				put(mask, nt)
			}
		}

		val sortedB = bracketsIds.sorted()
		val labelGroup = mutableMapOf<String, Int>()
		for ((i, label) in sortedB.withIndex()) {
			labelGroup[label] = i % k
		}

		var SeAlt: Regexp = Epsilon or Term("normal") or (Se * Se)
		val sAlts = mutableMapOf<Int, Regexp>()
		for ((mask, _) in Smap) sAlts[mask] = Epsilon or Term("normal")

		var emptyMask = 0
		for (brId in sortedB) {
			val group = labelGroup.getValue(brId)
			emptyMask += 1 shl group

			val openB = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, true))
			val closeB = Term(terminalFormat.generateLabel(BracketType.Brackets, brId, false))
			sAlts[emptyMask] = sAlts.getValue(emptyMask) or openB or closeB

			emptyMask -= 1 shl group
		}

		if (parenthesesIds.isNotEmpty()) {
			for (id in parenthesesIds) {
				val open = Term(terminalFormat.generateLabel(BracketType.Parentheses, id, true))
				val close = Term(terminalFormat.generateLabel(BracketType.Parentheses, id, false))

				SeAlt = SeAlt or (open * Se * close)
				for ((mask, alt) in sAlts) {
					sAlts[mask] = alt or (open * Smap.getValue(mask) * close)
				}
			}
		}

		for (mask1 in 0 until (1 shl k)) {
			val S1 = Smap[mask1]!!
			sAlts[mask1] = sAlts.getValue(mask1) or (S1 * Se) or (Se * S1)
			for (mask2 in 0 until (1 shl k)) {
				val S2 = Smap[mask2]!!
				val mask3 = mask1 xor mask2
				sAlts[mask3] = sAlts.getValue(mask3) or (S1 * S2)
			}
		}

		Se /= SeAlt
		for ((mask, alt) in sAlts) {
			Smap.getValue(mask) /= alt
		}

		val S0 = Smap.getValue(0)
		S /= (Se * Eps) or (S0 * Eps)
	}
}
