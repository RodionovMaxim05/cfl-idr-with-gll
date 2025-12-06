package graph

import org.cfl_idr_with_gll.graph.splitIntoConnectedComponents
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term

class TestSplitIntoConnectedComponents {

	@Test
	fun `empty graph returns empty list`() {
		val graph = InputGraph<String, TerminalInputLabel>()
		val components = graph.splitIntoConnectedComponents()
		assertTrue(components.isEmpty())
	}

	@Test
	fun `single isolated vertex returns one component`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
		}

		val components = graph.splitIntoConnectedComponents()

		assertEquals(1, components.size)
		val comp = components[0]
		assertEquals(setOf("A"), comp.vertices)
		assertTrue(comp.edges.isEmpty())
	}

	@Test
	fun `single edge forms one component`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
		}

		val components = graph.splitIntoConnectedComponents()

		assertEquals(1, components.size)
		val comp = components[0]
		assertEquals(setOf("A", "B"), comp.vertices)
		assertEquals(1, comp.getEdges("A").size)
		assertEquals("x", comp.getEdges("A").first().label.toString())
	}

	@Test
	fun `two disconnected edges form two components`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
			addEdge("C", TerminalInputLabel(Term("y")), "D")
		}

		val components = graph.splitIntoConnectedComponents()

		assertEquals(2, components.size)

		val vertexSets = components.map { it.vertices }.toSet()
		assertEquals(
			setOf(setOf("A", "B"), setOf("C", "D")),
			vertexSets
		)
	}

	@Test
	fun `directed cycle is one weakly connected component`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("a")), "B")
			addEdge("B", TerminalInputLabel(Term("b")), "C")
			addEdge("C", TerminalInputLabel(Term("c")), "A") // cycle
		}

		val components = graph.splitIntoConnectedComponents()

		assertEquals(1, components.size)
		assertEquals(setOf("A", "B", "C"), components[0].vertices)
	}

	@Test
	fun `two separate cycles form two components`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			// Cycle 1
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("a1")), "B")
			addEdge("B", TerminalInputLabel(Term("a2")), "A")
			// Cycle 2
			addVertex("X")
			addVertex("Y")
			addEdge("X", TerminalInputLabel(Term("x1")), "Y")
			addEdge("Y", TerminalInputLabel(Term("x2")), "X")
		}

		val components = graph.splitIntoConnectedComponents()

		assertEquals(2, components.size)
		val vertexSets = components.map { it.vertices }.toSet()
		assertEquals(
			setOf(setOf("A", "B"), setOf("X", "Y")),
			vertexSets
		)
	}

	@Test
	fun `vertex with self-loop is one component`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addEdge("A", TerminalInputLabel(Term("loop")), "A")
		}

		val components = graph.splitIntoConnectedComponents()

		assertEquals(1, components.size)
		assertEquals(setOf("A"), components[0].vertices)
		assertEquals(1, components[0].getEdges("A").size)
	}

	@Test
	fun `isolated vertex plus connected pair`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("X") // isolated
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("e")), "B")
		}

		val components = graph.splitIntoConnectedComponents()

		assertEquals(2, components.size)

		val vertexSets = components.map { it.vertices }.toSet()
		assertEquals(
			setOf(setOf("X"), setOf("A", "B")),
			vertexSets
		)
	}

	@Test
	fun `complex graph with three components`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			// Component 1: A <-> B -> C
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ab")), "B")
			addEdge("B", TerminalInputLabel(Term("ba")), "A")
			addEdge("B", TerminalInputLabel(Term("bc")), "C")

			// Component 2: D -> E <- F
			addVertex("D")
			addVertex("E")
			addVertex("F")
			addEdge("D", TerminalInputLabel(Term("de")), "E")
			addEdge("F", TerminalInputLabel(Term("fe")), "E")

			// Component 3: isolated
			addVertex("Z")
		}

		val components = graph.splitIntoConnectedComponents()

		assertEquals(3, components.size)

		val vertexSets = components.map { it.vertices }.toSet()
		assertEquals(
			setOf(
				setOf("A", "B", "C"),
				setOf("D", "E", "F"),
				setOf("Z")
			),
			vertexSets
		)

		// Проверим, что рёбра скопированы корректно
		val comp1 = components.find { it.vertices.contains("A") }!!
		assertEquals(3, comp1.vertices.size)
		assertEquals(3, comp1.edges.values.sumOf { it.size }) // ab, ba, bc

		val comp2 = components.find { it.vertices.contains("D") }!!
		assertEquals(3, comp2.vertices.size)
		assertEquals(2, comp2.edges.values.sumOf { it.size }) // de, fe
	}

	@Test
	fun `components preserve edge labels exactly`() {
		val graph = InputGraph<Int, TerminalInputLabel>().apply {
			addVertex(1)
			addVertex(2)
			addVertex(3)
			addEdge(1, TerminalInputLabel(Term("label1")), 2)
			addEdge(2, TerminalInputLabel(Term("label2")), 3)
		}

		val components = graph.splitIntoConnectedComponents()
		
		assertEquals(1, components.size)
		val comp = components[0]

		assertEquals("label1", comp.getEdges(1).first().label.toString())
		assertEquals("label2", comp.getEdges(2).first().label.toString())
	}
}
