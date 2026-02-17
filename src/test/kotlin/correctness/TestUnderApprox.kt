package correctness

import org.cfl_idr_with_gll.models.Path
import org.cfl_idr_with_gll.convertEdgesToGraphvizText
import org.cfl_idr_with_gll.getUnderApprox
import org.cfl_idr_with_gll.graph.removeValueflowUnreachable
import org.junit.jupiter.api.Test
import org.ucfs.input.DotParser
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import java.io.File
import kotlin.test.assertEquals

class TestUnderApprox {
	private val basePath = "src/test/resources/correctness"

	private fun loadGraph(filePath: String): InputGraph<Int, TerminalInputLabel> {
		val inputFile = File(filePath)
		require(inputFile.exists()) { "Input file not found: ${inputFile.absolutePath}" }

		val inputText = inputFile.readText()
		val finalGraphText = convertEdgesToGraphvizText(inputText)
		return DotParser().parseDot(finalGraphText)
	}

	@Test
	fun `figure5 graph`() {
		val graph = loadGraph("$basePath/figure5/graph.dot")

		val actual = getUnderApprox(graph)

		val expected = setOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `figure9 graph`() {
		val graph = loadGraph("$basePath/figure9/graph.dot")

		val actual = getUnderApprox(graph)

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
		val graph = loadGraph("$basePath/figure10/graph.dot")

		val actual = getUnderApprox(graph)

		val expected = setOf(
			Path(source = 3, target = 7),
			Path(source = 4, target = 6)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `figure11 graph`() {
		val graph = loadGraph("$basePath/figure11/graph.dot")

		val actual = getUnderApprox(graph)

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
		val graph = loadGraph("$basePath/loozfon/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(76, actual.size)

		val expectedLines = File("$basePath/loozfon/underApproxPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(57, actual.size)

		val expectedLines = File("$basePath/faketaobao/underApproxPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(155, actual.size)

		val expectedLines = File("$basePath/jollyserv/underApproxPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(779, actual.size)

		val expectedLines = File("$basePath/zertsecurity/underApproxPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(249, actual.size)

		val expectedLines = File("$basePath/fakebanker/underApproxPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/underApproxPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `collection_slx graph`() {
		val graph = loadGraph("$basePath/collection_slx/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(5210, actual.size)

		val expectedLines = File("$basePath/collection_slx/paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	// Value-flow analysis

	@Test
	fun `xz graph`() {
		val graph = loadGraph("$basePath/xz/graph.dot").removeValueflowUnreachable()

		val actual = getUnderApprox(graph, valueflow = true)

		assertEquals(211, actual.size)

		val expectedLines = File("$basePath/xz/paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `nab graph`() {
		val graph = loadGraph("$basePath/nab/graph.dot").removeValueflowUnreachable()

		val actual = getUnderApprox(graph, valueflow = true)

		assertEquals(1788, actual.size)

		val expectedLines = File("$basePath/nab/paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `leela graph`() {
		val graph = loadGraph("$basePath/leela/graph.dot").removeValueflowUnreachable()

		val actual = getUnderApprox(graph, valueflow = true)

		assertEquals(392, actual.size)

		val expectedLines = File("$basePath/leela/paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}
}
