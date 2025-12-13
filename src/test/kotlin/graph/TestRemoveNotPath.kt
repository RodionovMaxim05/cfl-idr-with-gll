package graph

import org.cfl_idr_with_gll.models.Path
import org.cfl_idr_with_gll.graph.computeSccs
import org.cfl_idr_with_gll.graph.removeNotPath
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term

class TestRemoveNotPath {
	// Tests for computeSccs (helper function)

	@Test
	fun `computeSccs - single vertex`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply { addVertex("A") }

		val (vertexToScc, reach) = computeSccs(graph)

		assertEquals(mapOf("A" to 0), vertexToScc)
		assertEquals(setOf(0 to 0), reach)
	}

	@Test
	fun `computeSccs - two disconnected vertices`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
		}

		val (vertexToScc, reach) = computeSccs(graph)

		assertEquals(2, vertexToScc.size)
		assertEquals(setOf(0 to 0, 1 to 1), reach)
		assertTrue(vertexToScc.values.toSet() == setOf(0, 1))
	}

	@Test
	fun `computeSccs - directed cycle is one SCC`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ab")), "B")
			addEdge("B", TerminalInputLabel(Term("bc")), "C")
			addEdge("C", TerminalInputLabel(Term("ca")), "A")
		}

		val (vertexToScc, reach) = computeSccs(graph)

		assertEquals(1, vertexToScc.values.toSet().size)
		assertEquals(setOf(0 to 0), reach)
	}

	@Test
	fun `computeSccs - DAG of two components has transitive closure`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addEdge("A", TerminalInputLabel(Term("a")), "B") // SCC1
			addEdge("C", TerminalInputLabel(Term("c")), "D") // SCC2
			addEdge("B", TerminalInputLabel(Term("bc")), "C") // A→B→C→D
		}

		val (vertexToScc, reach) = computeSccs(graph)

		// 4 vertices -> 4 SCCs (DAG without cycles)
		assertEquals(4, vertexToScc.size)
		val comps = vertexToScc.values.toSet()
		assertEquals(setOf(0, 1, 2, 3), comps)

		// Must be transitive reachable: 0->1->2->3 => 0->2, 0->3, 1->3
		assertTrue(reach.contains(0 to 0))
		assertTrue(reach.contains(1 to 1))
		assertTrue(reach.contains(2 to 2))
		assertTrue(reach.contains(3 to 3))
		assertTrue(reach.contains(0 to 1))
		assertTrue(reach.contains(1 to 2))
		assertTrue(reach.contains(2 to 3))
		assertTrue(reach.contains(0 to 2))
		assertTrue(reach.contains(0 to 3))
		assertTrue(reach.contains(1 to 3))
	}

	@Test
	fun `computeSccs - DAG of two components with multiple parallel edges`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("a")), "B")
			addEdge("A", TerminalInputLabel(Term("b")), "B")
			addEdge("A", TerminalInputLabel(Term("c")), "B")
		}

		val (vertexToScc, reach) = computeSccs(graph)

		// 2 vertices -> 2 SCCs (DAG without cycles)
		assertEquals(2, vertexToScc.size)
		val comps = vertexToScc.values.toSet()
		assertEquals(setOf(0, 1), comps)

		assertTrue(reach.contains(0 to 0))
		assertTrue(reach.contains(1 to 1))
		assertTrue(reach.contains(0 to 1))
	}

	// Tests for removeNotPath

	@Test
	fun `removeNotPath - empty graph and empty overApprox returns empty graph`() {
		val graph = InputGraph<String, TerminalInputLabel>()
		val overApprox = setOf<Path<String>>()

		val result = graph.removeNotPath(overApprox)

		assertTrue(result.vertices.isEmpty())
		assertTrue(result.edges.isEmpty())
	}

	@Test
	fun `removeNotPath - full overApprox returns original graph`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
		}
		val overApprox = setOf(
			Path("A", "B"),
			Path("B", "A")
		)

		val result = graph.removeNotPath(overApprox)

		assertEquals(graph.vertices, result.vertices)
		assertEquals(graph.edges.size, result.edges.size)
		assertTrue(result.getEdges("A").any { it.label.toString() == "x" })
	}

	@Test
	fun `removeNotPath - removes edge not in overApprox`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
			addEdge("B", TerminalInputLabel(Term("y")), "C")
		}
		val overApprox = setOf(Path("A", "B"))

		val result = graph.removeNotPath(overApprox)

		assertEquals(setOf("A", "B"), result.vertices)
		assertEquals(1, result.edges.values.sumOf { it.size })
		assertTrue(result.getEdges("A").any { it.label.toString() == "x" })
		assertTrue(result.getEdges("B").isEmpty())
	}

	@Test
	fun `removeNotPath - keeps edge via indirect SCC reachability`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("ab")), "B")
			addEdge("B", TerminalInputLabel(Term("bc")), "C")
			addEdge("C", TerminalInputLabel(Term("ca")), "A")
		}
		val overApprox = setOf(Path("A", "C"))

		val result = graph.removeNotPath(overApprox)

		assertEquals(3, result.edges.values.sumOf { it.size })
		assertTrue(result.getEdges("A").any { it.label.toString() == "ab" })
		assertTrue(result.getEdges("B").any { it.label.toString() == "bc" })
		assertTrue(result.getEdges("C").any { it.label.toString() == "ca" })
	}

	@Test
	fun `removeNotPath - multiple SCCs with allowed Path between them`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addVertex("D")
			addEdge("A", TerminalInputLabel(Term("ab")), "B")
			addEdge("B", TerminalInputLabel(Term("ba")), "A")
			addEdge("C", TerminalInputLabel(Term("cd")), "D")
			addEdge("D", TerminalInputLabel(Term("dc")), "C")
			addEdge("B", TerminalInputLabel(Term("bc")), "C") // меж-SCC
		}
		val overApprox = setOf(Path("A", "D"))

		val result = graph.removeNotPath(overApprox)


		assertEquals(5, result.edges.values.sumOf { it.size })
		assertTrue(result.getEdges("A").any { it.label.toString() == "ab" })
		assertTrue(result.getEdges("B").any { it.label.toString() == "ba" })
		assertTrue(result.getEdges("B").any { it.label.toString() == "bc" })
		assertTrue(result.getEdges("C").any { it.label.toString() == "cd" })
		assertTrue(result.getEdges("D").any { it.label.toString() == "dc" })
	}

	@Test
	fun `removeNotPath - removes inter-SCC edge when no allowed Path covers it`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addVertex("B")
			addVertex("C")
			addEdge("A", TerminalInputLabel(Term("a")), "B")
			addEdge("C", TerminalInputLabel(Term("c")), "C")
			addEdge("B", TerminalInputLabel(Term("bc")), "C")
		}
		val overApprox = setOf(Path("A", "B"))

		val result = graph.removeNotPath(overApprox)

		assertEquals(1, result.edges.values.sumOf { it.size })
		assertTrue(result.getEdges("A").any { it.label.toString() == "a" })
		assertTrue(result.getEdges("B").isEmpty())
		assertTrue(result.getEdges("C").isEmpty())
	}
}
