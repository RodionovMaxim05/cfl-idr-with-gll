import org.junit.jupiter.api.Test
import org.ucfs.rsm.symbol.ITerminal
import kotlin.test.assertEquals
import org.cfl_idr_with_gll.extractEdgesFromSppfResults
import org.cfl_idr_with_gll.Edge
import utils.sppfForExtractEdges.buildSimpleSppf
import utils.sppfForExtractEdges.buildSimplifiedDyckGrammarSppf
import utils.sppfForExtractEdges.buildOneVertexLoopDyckGrammarSppf
import utils.sppfForExtractEdges.buildCyclicSppf


class TestExtractEdgesFromSppfResult {

	data class MockTerminal(val name: String) : ITerminal {
		override fun getComparator(): Comparator<ITerminal> =
			Comparator { a, b -> (a as MockTerminal).name.compareTo((b as MockTerminal).name) }
	}

	@Test
	fun `single terminal SPPF produces one edge`() {
		val sppf = buildSimpleSppf()

		val result = extractEdgesFromSppfResults(sppf)

		assertEquals(
			setOf(Edge(0, MockTerminal("a"), 1)),
			result
		)
	}

	@Test
	fun `Dyck-like SPPF with sequential terminals produces two edges`() {
		val sppf = buildSimplifiedDyckGrammarSppf()

		val result = extractEdgesFromSppfResults(sppf)

		val expected = setOf(Edge(0, MockTerminal("("), 1), Edge(1, MockTerminal(")"), 2))
		assertEquals(expected, result)
	}

	@Test
	fun `looped Dyck SPPF with single vertex produces self-loop edges`() {
		val sppf = buildOneVertexLoopDyckGrammarSppf()

		val result = extractEdgesFromSppfResults(sppf)

		val expected = setOf(Edge(0, MockTerminal("("), 0), Edge(0, MockTerminal(")"), 0))
		assertEquals(expected, result)
	}

	@Test
	fun `cyclic SPPF with recursive nonterminal produces single terminal edge`() {
		val sppf = buildCyclicSppf()

		val result = extractEdgesFromSppfResults(sppf)

		val expected = setOf(Edge(0, MockTerminal("a"), 1))
		assertEquals(expected, result)
	}
}
