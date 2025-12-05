package grammar

import org.cfl_idr_with_gll.dyckGrammar
import org.cfl_idr_with_gll.dyckAlphaGrammar
import org.cfl_idr_with_gll.dyckBetaGrammar
import org.cfl_idr_with_gll.dyckAlphaGrammarKParity
import org.cfl_idr_with_gll.dyckBetaGrammarKParity
import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.cfl_idr_with_gll.terminal.ITerminalFormat.BracketType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ucfs.grammar.combinator.Grammar
import org.ucfs.grammar.combinator.regexp.*

class TestIdGrammar {
	/** Extracting Regexp of a Nonterminal */
	private fun Grammar.regexpOf(name: String): Regexp {
		val nt = nonTerms.find { it.nonterm.name == name }
			?: error("NT $name not found")

		val field = nt::class.java.getDeclaredField("rsmDescription")
		field.isAccessible = true
		return field.get(nt) as Regexp
	}

	/** Expand Alternative into a list of alternatives */
	private fun Regexp.alts(): List<Regexp> = when (this) {
		is Alternative -> this.left.alts() + this.right.alts()
		else -> listOf(this)
	}

	/** Expand Concat into a list of consecutive nodes */
	private fun Regexp.concatParts(): List<Regexp> = when (this) {
		is Concat -> this.head.concatParts() + this.tail.concatParts()
		else -> listOf(this)
	}

	/** Check that this is a Terminal with a specific label */
	private fun isTerm(r: Regexp, label: String): Boolean {
		if (r !is DerivedSymbol) return false
		// DerivedSymbol.toString() = имя терминала/нетерминала
		return r.toString() == label
	}

	/** Check that this is a Nonterminal with a specific label */
	private fun isNt(r: Regexp, name: String): Boolean {
		if (r !is Nt) return false

		val nontermField = r::class.java.getDeclaredField("nonterm")
		nontermField.isAccessible = true
		val nonterm = nontermField.get(r) as org.ucfs.rsm.symbol.Nonterminal
		return nonterm.name == name
	}

	@Test
	fun `check dyckGrammar`() {
		val g = dyckGrammar(DefaultTerminalFormat, listOf("01"), listOf("11"))
		val Sbody = g.regexpOf("S")
		val alts = Sbody.alts()

		// Epsilon & normal
		assertTrue(alts.any { it is Epsilon }, "Missing Epsilon")
		assertTrue(alts.any { isTerm(it, "normal") }, "Missing normal")

		// S S
		assertTrue(alts.any { alt ->
			val p = alt.concatParts()
			p.size == 2 && isNt(p[0], "S") && isNt(p[1], "S")
		}, "Missing S S")

		// ( S )
		val open0 = DefaultTerminalFormat.generateLabel(BracketType.Parentheses, "01", true)
		val close0 = DefaultTerminalFormat.generateLabel(BracketType.Parentheses, "01", false)
		assertTrue(alts.any { alt ->
			val p = alt.concatParts()
			p.size == 3 &&
					isTerm(p[0], open0) &&
					isNt(p[1], "S") &&
					isTerm(p[2], close0)
		}, "Missing ( S )")

		// [ S ]
		val open1 = DefaultTerminalFormat.generateLabel(BracketType.Brackets, "11", true)
		val close1 = DefaultTerminalFormat.generateLabel(BracketType.Brackets, "11", false)
		assertTrue(alts.any { alt ->
			val p = alt.concatParts()
			p.size == 3 &&
					isTerm(p[0], open1) &&
					isNt(p[1], "S") &&
					isTerm(p[2], close1)
		}, "Missing [ S ]")
	}

	@Test
	fun `check dyckAlphaGrammar`() {
		val g = dyckAlphaGrammar(DefaultTerminalFormat, listOf("01"), listOf("11"))
		val Sbody = g.regexpOf("S")
		val alts = Sbody.alts()

		// Epsilon & normal
		assertTrue(alts.any { it is Epsilon })
		assertTrue(alts.any { isTerm(it, "normal") })

		// ( S )
		val open0 = DefaultTerminalFormat.generateLabel(BracketType.Parentheses, "01", true)
		val close0 = DefaultTerminalFormat.generateLabel(BracketType.Parentheses, "01", false)
		assertTrue(alts.any { alt ->
			val p = alt.concatParts()
			p.size == 3 && isTerm(p[0], open0) && isNt(p[1], "S") && isTerm(p[2], close0)
		}, "Missing ( S )")

		// [ or ]
		val open1 = DefaultTerminalFormat.generateLabel(BracketType.Brackets, "11", true)
		val close1 = DefaultTerminalFormat.generateLabel(BracketType.Brackets, "11", false)
		assertTrue(alts.any { isTerm(it, open1) })
		assertTrue(alts.any { isTerm(it, close1) })
	}

	@Test
	fun `check dyckBetaGrammar`() {
		val g = dyckBetaGrammar(DefaultTerminalFormat, listOf("01"), listOf("11"))
		val Sbody = g.regexpOf("S")
		val alts = Sbody.alts()

		// Epsilon & normal
		assertTrue(alts.any { it is Epsilon })
		assertTrue(alts.any { isTerm(it, "normal") })

		// [ S ]
		val open1 = DefaultTerminalFormat.generateLabel(BracketType.Brackets, "11", true)
		val close1 = DefaultTerminalFormat.generateLabel(BracketType.Brackets, "11", false)
		assertTrue(alts.any { alt ->
			val p = alt.concatParts()
			p.size == 3 && isTerm(p[0], open1) && isNt(p[1], "S") && isTerm(p[2], close1)
		}, "Missing ( S )")

		// ( or )
		val open0 = DefaultTerminalFormat.generateLabel(BracketType.Parentheses, "01", true)
		val close0 = DefaultTerminalFormat.generateLabel(BracketType.Parentheses, "01", false)
		assertTrue(alts.any { isTerm(it, open0) })
		assertTrue(alts.any { isTerm(it, close0) })
	}
}
