package correctness

import org.cfl_idr_with_gll.models.Path
import org.cfl_idr_with_gll.convertEdgesToGraphvizText
import org.cfl_idr_with_gll.getMROverApprox
import org.cfl_idr_with_gll.graph.removeValueflowUnreachable
import org.junit.jupiter.api.Test
import org.ucfs.input.DotParser
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import java.io.File
import kotlin.test.assertEquals

class TestOverApprox {
	private val basePath = "src/test/resources/correctness"

	companion object {
		private const val GRAMMAR_PARITY = "parity"
		private const val GRAMMAR_PARITY2 = "parity2"
		private const val GRAMMAR_SE = "se"
		private const val GRAMMAR_PROJECT = "project"
		private const val GRAMMAR_EXCLUDE = "exclude"
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
	fun `parity2 on figure5 graph`() {
		val graph = loadGraph("$basePath/figure5/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

		val expected = setOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `se on figure5 graph`() {
		val graph = loadGraph("$basePath/figure5/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

		val expected = setOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on figure9 graph`() {
		val graph = loadGraph("$basePath/figure9/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

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
		val graph = loadGraph("$basePath/figure9/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

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
		val graph = loadGraph("$basePath/figure10/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

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
		val graph = loadGraph("$basePath/figure10/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

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
		val graph = loadGraph("$basePath/figure10/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PROJECT)

		val expected = setOf(Path(source = 3, target = 7), Path(source = 4, target = 6))

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on figure11 graph`() {
		val graph = loadGraph("$basePath/figure11/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

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
		val graph = loadGraph("$basePath/figure11/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

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
		val graph = loadGraph("$basePath/figure11/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PROJECT)

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
		val graph = loadGraph("$basePath/figure11/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_EXCLUDE)

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
	fun `parity1 on loozfon graph`() {
		val graph = loadGraph("$basePath/loozfon/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY)

		assertEquals(212, actual.size)

		val expectedLines = File("$basePath/loozfon/parity1Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on loozfon graph`() {
		val graph = loadGraph("$basePath/loozfon/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

		assertEquals(211, actual.size)

		val expectedLines = File("$basePath/loozfon/parity2Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `se on loozfon graph`() {
		val graph = loadGraph("$basePath/loozfon/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

		assertEquals(93, actual.size)

		val expectedLines = File("$basePath/loozfon/sePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `project on loozfon graph`() {
		val graph = loadGraph("$basePath/loozfon/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PROJECT)

		assertEquals(153, actual.size)

		val expectedLines = File("$basePath/loozfon/projectPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `exclude on loozfon graph`() {
		val graph = loadGraph("$basePath/loozfon/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_EXCLUDE)

		assertEquals(212, actual.size)

		val expectedLines = File("$basePath/loozfon/excludePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `all on loozfon graph`() {
		val graph = loadGraph("$basePath/loozfon/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_ALL)

		assertEquals(93, actual.size)

		val expectedLines = File("$basePath/loozfon/allPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity1 on faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY)

		assertEquals(151, actual.size)

		val expectedLines = File("$basePath/faketaobao/parity1Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

		assertEquals(151, actual.size)

		val expectedLines = File("$basePath/faketaobao/parity2Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `se on faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

		assertEquals(64, actual.size)

		val expectedLines = File("$basePath/faketaobao/sePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `project on faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PROJECT)

		assertEquals(59, actual.size)

		val expectedLines = File("$basePath/faketaobao/projectPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `exclude on faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_EXCLUDE)

		assertEquals(151, actual.size)

		val expectedLines = File("$basePath/faketaobao/excludePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `all on faketaobao graph`() {
		val graph = loadGraph("$basePath/faketaobao/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_ALL)

		assertEquals(59, actual.size)

		val expectedLines = File("$basePath/faketaobao/allPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity1 on jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY)

		assertEquals(176, actual.size)

		val expectedLines = File("$basePath/jollyserv/parity1Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

		assertEquals(175, actual.size)

		val expectedLines = File("$basePath/jollyserv/parity2Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `se on jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

		assertEquals(164, actual.size)

		val expectedLines = File("$basePath/jollyserv/sePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `project on jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PROJECT)

		assertEquals(174, actual.size)

		val expectedLines = File("$basePath/jollyserv/projectPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `exclude on jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_EXCLUDE)

		assertEquals(176, actual.size)

		val expectedLines = File("$basePath/jollyserv/excludePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `all on jollyserv graph`() {
		val graph = loadGraph("$basePath/jollyserv/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_ALL)

		assertEquals(164, actual.size)

		val expectedLines = File("$basePath/jollyserv/allPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity1 on zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY)

		assertEquals(1081, actual.size)

		val expectedLines = File("$basePath/zertsecurity/parity1Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

		assertEquals(1045, actual.size)

		val expectedLines = File("$basePath/zertsecurity/parity2Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `se on zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

		assertEquals(883, actual.size)

		val expectedLines = File("$basePath/zertsecurity/sePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `project on zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PROJECT)

		assertEquals(1081, actual.size)

		val expectedLines = File("$basePath/zertsecurity/projectPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `exclude on zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_EXCLUDE)

		assertEquals(1080, actual.size)

		val expectedLines = File("$basePath/zertsecurity/excludePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `all on zertsecurity graph`() {
		val graph = loadGraph("$basePath/zertsecurity/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_ALL)

		assertEquals(883, actual.size)

		val expectedLines = File("$basePath/zertsecurity/allPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity1 on fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY)

		assertEquals(590, actual.size)

		val expectedLines = File("$basePath/fakebanker/parity1Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

		assertEquals(590, actual.size)

		val expectedLines = File("$basePath/fakebanker/parity2Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `se on fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

		assertEquals(279, actual.size)

		val expectedLines = File("$basePath/fakebanker/sePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `project on fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PROJECT)

		assertEquals(354, actual.size)

		val expectedLines = File("$basePath/fakebanker/projectPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `exclude on fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_EXCLUDE)

		assertEquals(590, actual.size)

		val expectedLines = File("$basePath/fakebanker/excludePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `all on fakebanker graph`() {
		val graph = loadGraph("$basePath/fakebanker/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_ALL)

		assertEquals(272, actual.size)

		val expectedLines = File("$basePath/fakebanker/allPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity1 on uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/parity1Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/parity2Paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `se on uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_SE)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/sePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `project on uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PROJECT)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/projectPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `exclude on uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_EXCLUDE)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/excludePaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `all on uranai graph`() {
		val graph = loadGraph("$basePath/uranai/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_ALL)

		assertEquals(143, actual.size)

		val expectedLines = File("$basePath/uranai/allPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on collection_slx graph`() {
		val graph = loadGraph("$basePath/collection_slx/graph.dot")

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2)

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
	fun `parity2 on xz graph`() {
		val graph = loadGraph("$basePath/xz/graph.dot").removeValueflowUnreachable()

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2, valueflow = true)

		assertEquals(211, actual.size)

		val expectedLines = File("$basePath/xz/paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on nab graph`() {
		val graph = loadGraph("$basePath/nab/graph.dot").removeValueflowUnreachable()

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2, valueflow = true)

		assertEquals(1788, actual.size)

		val expectedLines = File("$basePath/nab/paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on leela graph`() {
		val graph = loadGraph("$basePath/leela/graph.dot").removeValueflowUnreachable()

		val actual = getMROverApprox(graph, GRAMMAR_PARITY2, valueflow = true)

		assertEquals(392, actual.size)

		val expectedLines = File("$basePath/leela/paths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}
}
