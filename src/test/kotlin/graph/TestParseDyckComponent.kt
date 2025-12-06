package graph

import org.cfl_idr_with_gll.graph.parseDyckComponent
import org.cfl_idr_with_gll.terminal.DefaultTerminalFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term

class TestParseDyckComponent {

	@Test
	fun `empty graph returns empty lists and empty graph`() {
		val graph = InputGraph<String, TerminalInputLabel>()
		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertTrue(parList.isEmpty())
		assertTrue(barList.isEmpty())
		assertTrue(parsedGraph.vertices.isEmpty())
		assertTrue(parsedGraph.edges.isEmpty())
	}

	@Test
	fun `only normal edges are kept, but no IDs extracted`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C")
			addEdge("A", TerminalInputLabel(Term("normal")), "B")
			addEdge("B", TerminalInputLabel(Term("normal")), "C")
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertTrue(parList.isEmpty())
		assertTrue(barList.isEmpty())
		assertEquals(2, parsedGraph.edges.values.sumOf { it.size })
		assertTrue(parsedGraph.getEdges("A").any { it.label.toString() == "normal" })
	}

	@Test
	fun `unmatched open parentheses are ignored`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("op--1")), "B")
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertTrue(parList.isEmpty())
		assertTrue(barList.isEmpty())
		assertTrue(parsedGraph.edges.isEmpty())
	}

	@Test
	fun `unmatched close parentheses are ignored`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("cp--1")), "B")
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertTrue(parList.isEmpty())
		assertTrue(barList.isEmpty())
		assertTrue(parsedGraph.edges.isEmpty())
	}

	@Test
	fun `matched parentheses are extracted and kept`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C")
			addEdge("A", TerminalInputLabel(Term("op--1")), "B") // open
			addEdge("B", TerminalInputLabel(Term("cp--1")), "C") // close
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertEquals(listOf("1"), parList)
		assertTrue(barList.isEmpty())
		assertEquals(2, parsedGraph.edges.values.sumOf { it.size })
		assertTrue(parsedGraph.getEdges("A").any { it.label.toString() == "op--1" })
		assertTrue(parsedGraph.getEdges("B").any { it.label.toString() == "cp--1" })
	}

	@Test
	fun `matched brackets are extracted and kept`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("X"); addVertex("Y"); addVertex("Z")
			addEdge("X", TerminalInputLabel(Term("ob--box")), "Y")
			addEdge("Y", TerminalInputLabel(Term("cb--box")), "Z")
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertTrue(parList.isEmpty())
		assertEquals(listOf("box"), barList)
		assertEquals(2, parsedGraph.edges.values.sumOf { it.size })
	}

	@Test
	fun `mixed matched and unmatched brackets`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C"); addVertex("D")
			addVertex("E"); addVertex("F"); addVertex("G")
			// Matched
			addEdge("A", TerminalInputLabel(Term("op--p1")), "B")
			addEdge("B", TerminalInputLabel(Term("cp--p1")), "C")
			// Unmatched
			addEdge("D", TerminalInputLabel(Term("op--p2")), "E") // open without close
			addEdge("F", TerminalInputLabel(Term("cb--b2")), "G") // close without open
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertEquals(listOf("p1"), parList)
		assertTrue(barList.isEmpty())
		assertEquals(2, parsedGraph.edges.values.sumOf { it.size })
		assertTrue(parsedGraph.getEdges("A").any { it.label.toString() == "op--p1" })
		assertTrue(parsedGraph.getEdges("B").any { it.label.toString() == "cp--p1" })

		assertFalse(parsedGraph.vertices.contains("D"))
		assertFalse(parsedGraph.vertices.contains("F"))
	}

	@Test
	fun `normal edges are always kept alongse Dyck edges`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C"); addVertex("D")
			addEdge("A", TerminalInputLabel(Term("normal")), "B")
			addEdge("B", TerminalInputLabel(Term("op--x")), "C")
			addEdge("C", TerminalInputLabel(Term("cp--x")), "D")
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertEquals(listOf("x"), parList)
		assertTrue(barList.isEmpty())
		assertEquals(3, parsedGraph.edges.values.sumOf { it.size })
		assertTrue(parsedGraph.getEdges("A").any { it.label.toString() == "normal" })
		assertTrue(parsedGraph.getEdges("B").any { it.label.toString() == "op--x" })
		assertTrue(parsedGraph.getEdges("C").any { it.label.toString() == "cp--x" })
	}

	@Test
	fun `duplicate matched parentheses are extracted only once`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C"); addVertex("D")
			addEdge("A", TerminalInputLabel(Term("op--dup")), "B")
			addEdge("B", TerminalInputLabel(Term("cp--dup")), "C")
			addEdge("C", TerminalInputLabel(Term("op--dup")), "D") // duplicate open
			addEdge("D", TerminalInputLabel(Term("cp--dup")), "A") // duplicate close
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertEquals(listOf("dup"), parList)
		assertTrue(barList.isEmpty())
		assertEquals(4, parsedGraph.edges.values.sumOf { it.size })
	}

	@Test
	fun `empty labels are ignored completely`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("")), "B")
		}

		val (parList, barList, parsedGraph) = parseDyckComponent(graph, DefaultTerminalFormat)

		assertTrue(parList.isEmpty())
		assertTrue(barList.isEmpty())
		assertTrue(parsedGraph.edges.isEmpty())
	}
}
