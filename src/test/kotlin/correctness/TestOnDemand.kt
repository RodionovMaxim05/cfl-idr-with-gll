package correctness

import org.cfl_idr_with_gll.Path
import org.cfl_idr_with_gll.convertEdgesToGraphvizText
import org.cfl_idr_with_gll.getMROverApprox
import org.cfl_idr_with_gll.getOnDemandMR
import org.cfl_idr_with_gll.getUnderApprox
import org.junit.jupiter.api.Test
import org.ucfs.input.DotParser
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import java.io.File
import kotlin.test.assertEquals

class TestOnDemand {
	private fun loadGraph(filePath: String): InputGraph<Int, TerminalInputLabel> {
		val inputFile = File(filePath)
		require(inputFile.exists()) { "Input file not found: ${inputFile.absolutePath}" }

		val inputText = inputFile.readText()
		val finalGraphText = convertEdgesToGraphvizText(inputText)
		return DotParser().parseDot(finalGraphText)
	}

	@Test
	fun `figure5 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure5/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		val expected = setOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `figure9 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure9/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

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
	fun `figure10 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure10/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		val expected = setOf(
			Path(source = 3, target = 7),
			Path(source = 4, target = 6)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `figure11 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure11/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

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
	fun `loozfon graph`() {
		val graph = loadGraph("src/test/resources/correctness/loozfon/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(93, actual.size)

		val expectedLines = File("src/test/resources/correctness/loozfon/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `faketaobao graph`() {
		val graph = loadGraph("src/test/resources/correctness/faketaobao/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(59, actual.size)

		val expectedLines = File("src/test/resources/correctness/faketaobao/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `jollyserv graph`() {
		val graph = loadGraph("src/test/resources/correctness/jollyserv/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(164, actual.size)

		val expectedLines = File("src/test/resources/correctness/jollyserv/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `zertsecurity graph`() {
		val graph = loadGraph("src/test/resources/correctness/zertsecurity/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(794, actual.size)

		val expectedLines = File("src/test/resources/correctness/zertsecurity/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `fakebanker graph`() {
		val graph = loadGraph("src/test/resources/correctness/fakebanker/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(251, actual.size)

		val expectedLines = File("src/test/resources/correctness/fakebanker/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `uranai graph`() {
		val graph = loadGraph("src/test/resources/correctness/uranai/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, "all", 1)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(143, actual.size)

		val expectedLines = File("src/test/resources/correctness/uranai/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}
}
