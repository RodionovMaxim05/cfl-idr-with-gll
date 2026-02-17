package correctness

import org.cfl_idr_with_gll.models.Path
import org.cfl_idr_with_gll.convertEdgesToGraphvizText
import org.cfl_idr_with_gll.getMROverApprox
import org.cfl_idr_with_gll.getOnDemandMR
import org.cfl_idr_with_gll.getUnderApprox
import org.cfl_idr_with_gll.graph.removeValueflowUnreachable
import org.junit.jupiter.api.Test
import org.ucfs.input.DotParser
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import java.io.File
import kotlin.test.assertEquals

class TestOnDemand {
	private val basePath = "src/test/resources/correctness"

	companion object {
		private const val GRAMMAR_PARITY = "parity"
		private const val GRAMMAR_ALL = "all"
	}

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
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		val expected = setOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `figure9 graph`() {
		val graph = loadGraph("$basePath/figure9/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

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
		val graph = loadGraph("$basePath/figure10/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		val expected = setOf(
			Path(source = 3, target = 7),
			Path(source = 4, target = 6)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `figure11 graph`() {
		val graph = loadGraph("$basePath/figure11/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

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
	fun `parityD loozfon graph`() {
		val graph = loadGraph("$basePath/loozfon/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_PARITY)

		val actual = getOnDemandMR(graph, underPaths, overPaths, parityD = true)

		assertEquals(93, actual.size)

		val expectedLines = File("$basePath/loozfon/parityDPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `on-demand loozfon graph`() {
		val graph = loadGraph("$basePath/loozfon/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(93, actual.size)

		val expectedLines = File("$basePath/loozfon/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parityD faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_PARITY)

		val actual = getOnDemandMR(graph, underPaths, overPaths, parityD = true)

		assertEquals(61, actual.size)

		val expectedLines = File("$basePath/faketaobao/parityDPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `on-demand faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(59, actual.size)

		val expectedLines = File("$basePath/faketaobao/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parityD jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_PARITY)

		val actual = getOnDemandMR(graph, underPaths, overPaths, parityD = true)

		assertEquals(164, actual.size)

		val expectedLines = File("$basePath/jollyserv/parityDPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `on-demand jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(164, actual.size)

		val expectedLines = File("$basePath/jollyserv/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parityD zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_PARITY)

		val actual = getOnDemandMR(graph, underPaths, overPaths, parityD = true)

		assertEquals(808, actual.size)

		val expectedLines = File("$basePath/zertsecurity/parityDPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `on-demand zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(794, actual.size)

		val expectedLines = File("$basePath/zertsecurity/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parityD fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_PARITY)

		val actual = getOnDemandMR(graph, underPaths, overPaths, parityD = true)

		assertEquals(254, actual.size)

		val expectedLines = File("$basePath/fakebanker/parityDPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `on-demand fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(251, actual.size)

		val expectedLines = File("$basePath/fakebanker/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parityD uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_PARITY)

		val actual = getOnDemandMR(graph, underPaths, overPaths, parityD = true)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/parityDPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `on-demand uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/onDemandPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `collection_slx graph`() {
		val graph = loadGraph("$basePath/collection_slx/graph.dot")
		val underPaths = getUnderApprox(graph)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL)

		val actual = getOnDemandMR(graph, underPaths, overPaths)

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
		val underPaths = getUnderApprox(graph, valueflow = true)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL, valueflow = true)

		val actual = getOnDemandMR(graph, underPaths, overPaths, valueflow = true)

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
		val underPaths = getUnderApprox(graph, valueflow = true)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL, valueflow = true)

		val actual = getOnDemandMR(graph, underPaths, overPaths, valueflow = true)

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
		val underPaths = getUnderApprox(graph, valueflow = true)
		val overPaths = getMROverApprox(graph, GRAMMAR_ALL, valueflow = true)

		val actual = getOnDemandMR(graph, underPaths, overPaths, valueflow = true)

		assertEquals(392, actual.size)

		val expectedLines = File("$basePath/leela/paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}
}
