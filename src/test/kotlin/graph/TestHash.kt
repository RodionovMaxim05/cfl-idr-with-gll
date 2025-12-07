package graph

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.cfl_idr_with_gll.graph.hash
import org.cfl_idr_with_gll.graph.hashGWithId
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import org.ucfs.rsm.symbol.Term

class TestHash {

	// Tests for hash

	@Test
	fun `empty graph has consistent hash`() {
		val graph = InputGraph<String, TerminalInputLabel>()
		val hash1 = graph.hash()
		val hash2 = graph.hash()

		assertEquals(hash1, hash2)
		assertEquals(0xCBF29CE484222325UL, hash1)
	}

	@Test
	fun `same graph structure produces same hash`() {
		val graph1 = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
			addEdge("B", TerminalInputLabel(Term("y")), "C")
		}
		val graph2 = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B"); addVertex("C")
			addEdge("B", TerminalInputLabel(Term("y")), "C")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
		}

		assertEquals(graph1.hash(), graph2.hash())
	}

	@Test
	fun `different graph structures produce different hashes`() {
		val graph1 = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
		}
		val graph2 = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("y")), "B")
		}

		assertNotEquals(graph1.hash(), graph2.hash())
	}

	@Test
	fun `graph with self-loop is hashed correctly`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A")
			addEdge("A", TerminalInputLabel(Term("loop")), "A")
		}
		val hash = graph.hash()
		assertNotEquals(0xCBF29CE484222325UL, hash)
	}

	@Test
	fun `order of vertices does not affect hash`() {
		val graph1 = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("Z")
			addEdge("Z", TerminalInputLabel(Term("z")), "A")
			addEdge("A", TerminalInputLabel(Term("a")), "Z")
		}
		val graph2 = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("Z")
			addEdge("A", TerminalInputLabel(Term("a")), "Z")
			addEdge("Z", TerminalInputLabel(Term("z")), "A")
		}

		assertEquals(graph1.hash(), graph2.hash())
	}

	// Tests for hashGWithId

	@Test
	fun `hash and hashGWithId null are equivalent`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("test")), "B")
		}

		assertEquals(graph.hash(), graph.hashGWithId(null))
	}

	@Test
	fun `hashGWithId with same id produces same hash`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
		}

		val hash1 = graph.hashGWithId("test")
		val hash2 = graph.hashGWithId("test")

		assertEquals(hash1, hash2)
	}

	@Test
	fun `hashGWithId with different ids produces different hashes`() {
		val graph = InputGraph<String, TerminalInputLabel>().apply {
			addVertex("A"); addVertex("B")
			addEdge("A", TerminalInputLabel(Term("x")), "B")
		}

		val hash1 = graph.hashGWithId("id1")
		val hash2 = graph.hashGWithId("id2")

		assertNotEquals(hash1, hash2)
	}
}
