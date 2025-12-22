package graph

import org.cfl_idr_with_gll.graph.removeValueflowUnreachable
import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term

class TestRemoveValueflowUnreachable {

	@Test
	fun `no ob or cb removes all vertices`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("op--1")), "B")
		}

		val result = graph.removeValueflowUnreachable(DefaultTerminalFormat)

		assertTrue(result.vertices.isEmpty())
	}

	@Test
	fun `simple ob to cb path is preserved`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("normal")), "C")
			addEdge("C", TerminalInputLabel(Term("cb--1")), "C")
		}

		val result = graph.removeValueflowUnreachable(DefaultTerminalFormat)

		assertEquals(setOf("A", "B", "C"), result.vertices)
		assertEquals(1, result.getEdges("A").size)
		assertEquals(1, result.getEdges("B").size)
	}

	@Test
	fun `reachable from ob but not leading to cb is removed`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("normal")), "C")
			// no cb reachable
		}

		val result = graph.removeValueflowUnreachable(DefaultTerminalFormat)

		assertTrue(result.vertices.isEmpty())
	}

	@Test
	fun `leading to cb but not reachable from ob is removed`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("normal")), "B")
			addEdge("B", TerminalInputLabel(Term("cb--1")), "B")
		}

		val result = graph.removeValueflowUnreachable(DefaultTerminalFormat)

		assertTrue(result.vertices.isEmpty())
	}

	@Test
	fun `cycle between ob and cb is preserved`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("normal")), "C")
			addEdge("C", TerminalInputLabel(Term("normal")), "B") // cycle
			addEdge("C", TerminalInputLabel(Term("cb--1")), "C")
		}

		val result = graph.removeValueflowUnreachable(DefaultTerminalFormat)

		assertEquals(setOf("A", "B", "C"), result.vertices)
		assertEquals(1, result.getEdges("A").size)
		assertEquals(1, result.getEdges("B").size)
		assertEquals(2, result.getEdges("C").size)
	}

	@Test
	fun `multiple sources and sinks keep only valid region`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addVertex("E")

			addEdge("A", TerminalInputLabel(Term("ob--1")), "B")
			addEdge("B", TerminalInputLabel(Term("normal")), "C")
			addEdge("C", TerminalInputLabel(Term("cb--1")), "C")

			addEdge("D", TerminalInputLabel(Term("ob--2")), "E") // dead branch
		}

		val result = graph.removeValueflowUnreachable(DefaultTerminalFormat)

		assertEquals(setOf("A", "B", "C"), result.vertices)
		assertFalse("D" in result.vertices)
		assertFalse("E" in result.vertices)
	}

	@Test
	fun `invalid cb to ob path`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")

			addEdge("A", TerminalInputLabel(Term("cb--1")), "B")
			addEdge("B", TerminalInputLabel(Term("normal")), "C")
			addEdge("C", TerminalInputLabel(Term("ob--1")), "D")
		}

		val result = graph.removeValueflowUnreachable(DefaultTerminalFormat)

		assertTrue(result.vertices.isEmpty())
	}
}

