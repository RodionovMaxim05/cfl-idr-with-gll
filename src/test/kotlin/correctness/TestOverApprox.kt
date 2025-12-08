package correctness

import org.cfl_idr_with_gll.Path
import org.cfl_idr_with_gll.convertEdgesToGraphvizText
import org.cfl_idr_with_gll.getMROverApprox
import org.junit.jupiter.api.Test
import org.ucfs.input.DotParser
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import java.io.File
import kotlin.test.assertEquals

class TestOverApprox {

	private fun loadGraph(filePath: String): InputGraph<Int, TerminalInputLabel> {
		val inputFile = File(filePath)
		require(inputFile.exists()) { "Input file not found: ${inputFile.absolutePath}" }

		val inputText = inputFile.readText()
		val finalGraphText = convertEdgesToGraphvizText(inputText)
		return DotParser().parseDot(finalGraphText)
	}

	@Test
	fun `parity2 on figure5 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure5/graph.dot")

		val actual = getMROverApprox(graph, "parityK", 2)

		val expected = setOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `se on figure5 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure5/graph.dot")

		val actual = getMROverApprox(graph, "se", 2)

		val expected = setOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on figure9 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure9/graph.dot")

		val actual = getMROverApprox(graph, "parityK", 2)

		val expected = setOf(
			Path(source = 1, target = 3),
			Path(source = 1, target = 5),
			Path(source = 2, target = 4),
			Path(source = 3, target = 5),
			Path(source = 6, target = 7),
			Path(source = 6, target = 8),
			Path(source = 7, target = 8)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `se on figure9 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure9/graph.dot")

		val actual = getMROverApprox(graph, "se", 2)

		val expected = setOf(
			Path(source = 1, target = 3),
			Path(source = 1, target = 5),
			Path(source = 3, target = 5),
			Path(source = 6, target = 7),
			Path(source = 6, target = 8),
			Path(source = 7, target = 8)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on figure10 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure10/graph.dot")

		val actual = getMROverApprox(graph, "parityK", 2)

		val expected = setOf(
			Path(source = 1, target = 3),
			Path(source = 1, target = 5),
			Path(source = 1, target = 7),
			Path(source = 2, target = 4),
			Path(source = 2, target = 6),
			Path(source = 3, target = 5),
			Path(source = 3, target = 7),
			Path(source = 4, target = 6),
			Path(source = 5, target = 7)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `se on figure10 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure10/graph.dot")

		val actual = getMROverApprox(graph, "se", 2)

		val expected = setOf(
			Path(source = 1, target = 7),
			Path(source = 2, target = 6),
			Path(source = 3, target = 7),
			Path(source = 4, target = 6)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `project on figure10 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure10/graph.dot")

		val actual = getMROverApprox(graph, "project", 2)

		val expected = setOf(Path(source = 3, target = 7), Path(source = 4, target = 6))

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on figure11 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure11/graph.dot")

		val actual = getMROverApprox(graph, "parityK", 2)

		val expected = setOf(
			Path(source = 1, target = 3),
			Path(source = 1, target = 5),
			Path(source = 2, target = 4),
			Path(source = 3, target = 5),
			Path(source = 6, target = 7),
			Path(source = 6, target = 8),
			Path(source = 7, target = 8),
			Path(source = 9, target = 11),
			Path(source = 10, target = 12)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `se on figure11 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure11/graph.dot")

		val actual = getMROverApprox(graph, "se", 2)

		val expected = setOf(
			Path(source = 1, target = 3),
			Path(source = 1, target = 5),
			Path(source = 2, target = 4),
			Path(source = 3, target = 5),
			Path(source = 6, target = 7),
			Path(source = 6, target = 8),
			Path(source = 7, target = 8)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `project on figure11 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure11/graph.dot")

		val actual = getMROverApprox(graph, "project", 2)

		val expected = setOf(
			Path(source = 1, target = 3),
			Path(source = 1, target = 5),
			Path(source = 2, target = 4),
			Path(source = 3, target = 5),
			Path(source = 6, target = 7),
			Path(source = 6, target = 8),
			Path(source = 7, target = 8),
			Path(source = 10, target = 12)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `exclude on figure11 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure11/graph.dot")

		val actual = getMROverApprox(graph, "exclude", 2)

		val expected = setOf(
			Path(source = 1, target = 3),
			Path(source = 1, target = 5),
			Path(source = 2, target = 4),
			Path(source = 3, target = 5),
			Path(source = 6, target = 7),
			Path(source = 6, target = 8),
			Path(source = 7, target = 8),
			Path(source = 9, target = 11),
			Path(source = 10, target = 12)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on loozfon graph`() {
		val graph = loadGraph("src/test/resources/correctness/loozfon/graph.dot")

		val actual = getMROverApprox(graph, "parityK", 2)

		assertEquals(211, actual.size)

		val expectedLines = File("src/test/resources/correctness/loozfon/parity2Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `se on loozfon graph`() {
		val graph = loadGraph("src/test/resources/correctness/loozfon/graph.dot")

		val actual = getMROverApprox(graph, "se", 2)

		assertEquals(93, actual.size)

		val expectedLines = File("src/test/resources/correctness/loozfon/sePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `project on loozfon graph`() {
		val graph = loadGraph("src/test/resources/correctness/loozfon/graph.dot")

		val actual = getMROverApprox(graph, "project", 2)

		assertEquals(153, actual.size)

		val expectedLines = File("src/test/resources/correctness/loozfon/projectPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

//	@Test
//	fun `exclude on loozfon graph`() {
//		val graph = loadGraph("src/test/resources/correctness/loozfon/graph.dot")
//
//		val actual = getMROverApprox(graph, "exclude", 2)
//
//		assertEquals(212, actual.size)
//
//		val expectedLines = File("src/test/resources/correctness/loozfon/exlcudePaths.txt").readLines()
//		val expected = expectedLines
//			.map { it.split(" ") }
//			.map { Path(it[0].toInt(), it[1].toInt()) }
//			.toSet()
//
//		assertEquals(expected, actual)
//	}

//	@Test
//	fun `all on loozfon graph`() {
//		val graph = loadGraph("src/test/resources/correctness/loozfon/graph.dot")
//
//		val actual = getMROverApprox(graph, "all", 2)
//
//		assertEquals(93, actual.size)
//
//		val expectedLines = File("src/test/resources/correctness/loozfon/allPaths.txt").readLines()
//		val expected = expectedLines
//			.map { it.split(" ") }
//			.map { Path(it[0].toInt(), it[1].toInt()) }
//			.toSet()
//
//		assertEquals(expected, actual)
//	}

	@Test
	fun `parity2 on faketaobao graph`() {
		val graph = loadGraph("src/test/resources/correctness/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, "parityK", 2)

		assertEquals(151, actual.size)

		val expectedLines = File("src/test/resources/correctness/faketaobao/parity2Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `se on faketaobao graph`() {
		val graph = loadGraph("src/test/resources/correctness/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, "se", 2)

		assertEquals(64, actual.size)

		val expectedLines = File("src/test/resources/correctness/faketaobao/sePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `project on faketaobao graph`() {
		val graph = loadGraph("src/test/resources/correctness/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, "project", 2)

		assertEquals(59, actual.size)

		val expectedLines = File("src/test/resources/correctness/faketaobao/projectPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `exclude on faketaobao graph`() {
		val graph = loadGraph("src/test/resources/correctness/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, "exclude", 2)

		assertEquals(151, actual.size)

		val expectedLines = File("src/test/resources/correctness/faketaobao/excludePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `all on faketaobao graph`() {
		val graph = loadGraph("src/test/resources/correctness/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, "all", 2)

		assertEquals(59, actual.size)

		val expectedLines = File("src/test/resources/correctness/faketaobao/allPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}
}
