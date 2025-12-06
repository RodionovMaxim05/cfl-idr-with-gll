package graph

import org.cfl_idr_with_gll.Path
import org.cfl_idr_with_gll.graph.condensateFromUnderApprox
import org.cfl_idr_with_gll.graph.findPMR
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term

class TestCondensateFromUnderApprox {

	// Tests for findPMR (Union-Find helper function)

	@Test
	fun `findPMR - single vertex returns itself`() {
		val parentMap = mutableMapOf("A" to "A")
		assertEquals("A", findPMR("A", parentMap))
	}

	@Test
	fun `findPMR - Path compression works`() {
		val parentMap = mutableMapOf(
			"A" to "B",
			"B" to "C",
			"C" to "C"
		)
		assertEquals("C", findPMR("A", parentMap))
		assertEquals("C", parentMap["A"])
	}

	// Tests for condensateFromUnderApprox

	@Test
	fun `empty graph returns empty condensed graph and empty parent map`() {
		val graph = InputGraph<String, TerminalInputLabel>()
		val underApprox = setOf<Path<String>>()

		val (condensed, parentMap) = condensateFromUnderApprox(graph, underApprox)

		assertTrue(condensed.vertices.isEmpty())
		assertTrue(condensed.edges.isEmpty())
		assertTrue(parentMap.isEmpty())
	}

	@Test
	fun `mutual paths cause vertices to be merged`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
			addEdge("B", TerminalInputLabel(Term("y")), "A")
		}
		val underApprox = setOf(
			Path("A", "B"),
			Path("B", "A")
		)

		val (condensed, parentMap) = condensateFromUnderApprox(graph, underApprox)

		assertEquals(1, condensed.vertices.size)

		assertEquals(2, condensed.edges.values.sumOf { it.size })
		val edgesFromRep = condensed.getEdges("B").map { it.label.toString() }.toSet()
		assertEquals(setOf("x", "y"), edgesFromRep)

		val rep = condensed.vertices.first()
		assertEquals(rep, parentMap["A"])
		assertEquals(rep, parentMap["B"])
	}

	@Test
	fun `one-way Path does not cause merging`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
		}
		val underApprox = setOf(Path("A", "B"))

		val (condensed, parentMap) = condensateFromUnderApprox(graph, underApprox)

		assertEquals(setOf("A", "B"), condensed.vertices)
		assertEquals(1, condensed.edges.values.sumOf { it.size })
		assertTrue(condensed.getEdges("A").any { it.label.toString() == "x" })

		assertEquals("A", parentMap["A"])
		assertEquals("B", parentMap["B"])
	}

	@Test
	fun `three vertices with mutual pairs merged correctly`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ab")), "B")
			addEdge("B", TerminalInputLabel(Term("ba")), "A")
			addEdge("B", TerminalInputLabel(Term("bc")), "C")
			addEdge("C", TerminalInputLabel(Term("cb")), "B")
		}

		// A <-> B and B <-> C => all three must be in one component
		val underApprox = setOf(
			Path("A", "B"), Path("B", "A"),
			Path("B", "C"), Path("C", "B")
		)

		val (condensed, parentMap) = condensateFromUnderApprox(graph, underApprox)

		assertEquals(1, condensed.vertices.size)
		assertEquals(4, condensed.edges.values.sumOf { it.size })
		val edgesFromRep = condensed.getEdges("B").map { it.label.toString() }.toSet()
		assertEquals(setOf("ab", "ba", "bc", "cb"), edgesFromRep)

		val rep = condensed.vertices.first()
		assertEquals(rep, parentMap["A"])
		assertEquals(rep, parentMap["B"])
		assertEquals(rep, parentMap["C"])
	}

	@Test
	fun `partial mutual paths - only fully mutual groups are merged`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ab")), "B")
			addEdge("B", TerminalInputLabel(Term("ba")), "A")
			addEdge("B", TerminalInputLabel(Term("bc")), "C")
		}
		val underApprox = setOf(
			Path("A", "B"),
			Path("B", "A"),
			Path("B", "C")
		)

		val (condensed, parentMap) = condensateFromUnderApprox(graph, underApprox)

		assertEquals(2, condensed.vertices.size)
		assertEquals(3, condensed.edges.values.sumOf { it.size })

		val repAB = parentMap["A"]!!
		assertEquals(repAB, parentMap["B"])
		assertNotEquals(repAB, parentMap["C"])

		val edgesFromRepAB = condensed.getEdges(repAB).map { it.label.toString() }.toSet()
		assertEquals(3, edgesFromRepAB.size)
		assertEquals(setOf("ab", "ba", "bc"), edgesFromRepAB)
	}

	@Test
	fun `duplicate edges are not added to condensed graph`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
		}
		val underApprox = setOf(Path("A", "B"), Path("B", "A"))

		val (condensed, _) = condensateFromUnderApprox(graph, underApprox)

		assertEquals(1, condensed.edges.values.sumOf { it.size })
	}

	@Test
	fun `edges between different components are preserved once`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C"); addVertex("D")
			addEdge("A", TerminalInputLabel(Term("1")), "C")
			addEdge("B", TerminalInputLabel(Term("1")), "C")
			addEdge("A", TerminalInputLabel(Term("2")), "D")
		}
		val underApprox = setOf(Path("A", "B"), Path("B", "A"))

		val (condensed, parentMap) = condensateFromUnderApprox(graph, underApprox)

		val repAB = parentMap["A"]!!
		assertEquals(repAB, parentMap["B"])
		assertNotEquals(repAB, parentMap["C"])
		assertNotEquals(repAB, parentMap["D"])

		val edgeCount = condensed.edges.values.sumOf { it.size }
		assertEquals(2, edgeCount)

		val edgesFromRep = condensed.getEdges(repAB).map { it.label.toString() }.toSet()
		assertEquals(setOf("1", "2"), edgesFromRep)
	}

	@Test
	fun `merge two vertices with one between them`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ab")), "B")
			addEdge("B", TerminalInputLabel(Term("bc")), "C")
			addEdge("C", TerminalInputLabel(Term("cb")), "B")
			addEdge("B", TerminalInputLabel(Term("ba")), "A")
		}
		val underApprox = setOf(Path("A", "C"), Path("C", "A"))

		val (condensed, parentMap) = condensateFromUnderApprox(graph, underApprox)

		val edgeCount = condensed.edges.values.sumOf { it.size }
		assertEquals(4, edgeCount)

		val repAC = parentMap["A"]!!
		assertEquals(repAC, parentMap["C"])
		assertNotEquals(repAC, parentMap["B"])

		val edgesFromRep = condensed.getEdges(repAC).map { it.label.toString() }.toSet()
		assertEquals(setOf("ab", "cb"), edgesFromRep)
	}
}
