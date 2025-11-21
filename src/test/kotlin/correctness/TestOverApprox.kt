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

	private fun <V : Comparable<V>> List<Path<V>>.sortedPaths(): List<Path<V>> {
		return this.sortedWith(compareBy(Path<V>::source).thenBy(Path<V>::target))
	}

	@Test
	fun `parity2 on figure5 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure5/graph.dot")

		val actual = getMROverApprox(graph, "parity2", 2).sortedPaths()

		val expected = listOf(Path(source = 1, target = 9), Path(source = 6, target = 8))

		assertEquals(expected, actual)
	}

	@Test
	fun `parity2 on figure9 graph`() {
		val graph = loadGraph("src/test/resources/correctness/figure9/graph.dot")

		val actual = getMROverApprox(graph, "parity2", 2).sortedPaths()

		val expected = listOf(
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

		val actual = getMROverApprox(graph, "se", 2).sortedPaths()

		val expected = listOf(
			Path(source = 1, target = 3),
			Path(source = 1, target = 5),
			Path(source = 3, target = 5),
			Path(source = 6, target = 7),
			Path(source = 6, target = 8),
			Path(source = 7, target = 8)
		)

		assertEquals(expected, actual)
	}
}
