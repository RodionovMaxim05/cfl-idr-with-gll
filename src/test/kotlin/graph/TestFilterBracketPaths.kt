package graph

import org.cfl_idr_with_gll.graph.filterBracketPaths
import org.cfl_idr_with_gll.models.Path
import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term

class TestFilterBracketPaths {

	@Test
	fun `empty paths returns empty set`() {
		val graph = InputGraph<String, TerminalInputLabel>()
		val paths = emptySet<Path<String>>()

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertTrue(result.isEmpty())
	}

	@Test
	fun `no matching brackets removes all paths`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("op--1")), "B")
		}
		val paths = setOf(Path("A", "B"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertTrue(result.isEmpty())
	}

	@Test
	fun `simple matching bracket path is preserved`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("cb--1")), "C")
		}
		val paths = setOf(Path("A", "C"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertEquals(setOf(Path("A", "C")), result)
	}

	@Test
	fun `mismatched bracket ids are filtered out`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("cb--2")), "C")
		}
		val paths = setOf(Path("A", "C"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertTrue(result.isEmpty())
	}

	@Test
	fun `path without outgoing open bracket is filtered out`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("B", TerminalInputLabel(Term("ob--1")), "C")
			addEdge("C", TerminalInputLabel(Term("cb--1")), "A")
		}
		val paths = setOf(Path("A", "A"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertTrue(result.isEmpty())
	}

	@Test
	fun `path without incoming close bracket is filtered out`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
		}
		val paths = setOf(Path("A", "C"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertTrue(result.isEmpty())
	}

	@Test
	fun `open and close brackets exist but no reachability in SCC condensation filters path`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("C", TerminalInputLabel(Term("cb--1")), "D")
		}
		// Is there a bracket path from A to D?
		val paths = setOf(Path("A", "D"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertTrue(result.isEmpty())
	}

	@Test
	fun `bracket path through intermediate non-bracket edges is accepted`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("normal")), "C")
			addEdge("C", TerminalInputLabel(Term("cb--1")), "D")
		}
		val paths = setOf(Path("A", "D"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertEquals(setOf(Path("A", "D")), result)
	}

	@Test
	fun `multiple valid bracket paths, which all preserved`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addVertex("E")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("cb--1")), "C")
			addEdge("A", TerminalInputLabel(Term("ob--2")), "D")
			addEdge("D", TerminalInputLabel(Term("cb--2")), "E")
		}
		val paths = setOf(Path("A", "C"), Path("A", "E"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertEquals(setOf(Path("A", "C"), Path("A", "E")), result)
	}

	@Test
	fun `bracket path accepted even if multiple open and close with same ID exist`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "C")
			addEdge("B", TerminalInputLabel(Term("cb--1")), "D")
			addEdge("C", TerminalInputLabel(Term("cb--1")), "D")
		}
		val paths = setOf(Path("A", "D"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertEquals(setOf(Path("A", "D")), result)
	}

	@Test
	fun `nested brackets`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addVertex("E")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("cb--1")), "C")
			addEdge("C", TerminalInputLabel(Term("ob--1")), "D")
			addEdge("D", TerminalInputLabel(Term("cb--1")), "E")
		}
		val paths = setOf(Path("A", "C"), Path("C", "E"), Path("A", "E"))

		val result = graph.filterBracketPaths(paths, DefaultTerminalFormat)

		assertEquals(paths, result)
	}
}
