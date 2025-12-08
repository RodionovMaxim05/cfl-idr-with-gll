package correctness

import org.cfl_idr_with_gll.Path
import org.cfl_idr_with_gll.convertEdgesToGraphvizText
import org.cfl_idr_with_gll.getUnderApprox
import org.junit.jupiter.api.Test
import org.ucfs.input.DotParser
import org.ucfs.input.InputGraph
import org.ucfs.input.TerminalInputLabel
import java.io.File
import kotlin.test.assertEquals

class TestUnderApprox {
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

		val actual = getUnderApprox(graph)

		val expected = setOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `figure9 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure9/graph.dot")

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
		val graph = loadGraph("src/test/resources/correctness/figure10/graph.dot")

		val actual = getUnderApprox(graph)

		val expected = setOf(
			Path(source = 3, target = 7),
			Path(source = 4, target = 6)
		)

		assertEquals(expected, actual)
	}

	@Test
	fun `figure11 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure11/graph.dot")

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
		val graph = loadGraph("src/test/resources/correctness/loozfon/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(76, actual.size)

		val expectedLines = File("src/test/resources/correctness/loozfon/underApproxPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}

	@Test
	fun `faketaobao graph`() {
		val graph = loadGraph("src/test/resources/correctness/faketaobao/graph.dot")

		val actual = getUnderApprox(graph)

		assertEquals(57, actual.size)

		val expectedLines = File("src/test/resources/correctness/faketaobao/underApproxPaths.txt").readLines()
		val expected = expectedLines
			.map { it.split(" ") }
			.map { Path(it[0].toInt(), it[1].toInt()) }
			.toSet()

		assertEquals(expected, actual)
	}
}
