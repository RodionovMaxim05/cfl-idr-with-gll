import org.junit.jupiter.api.Test
import org.cfl_idr_with_gll.convertEdgesToGraphvizText
import java.io.File
import kotlin.test.assertEquals

class TestConvertEdgesToGraphvizText {
	private val basePath = "utils/convertEdgesToGraphviz"

	private fun resourceFile(name: String): File {
		val url = this::class.java.classLoader.getResource(name)
			?: error("Resource not found: $name")
		return File(url.toURI())
	}

	@Test
	fun `convertEdgesToGraphvizText handles empty input`() {
		val input = resourceFile("$basePath/empty/input.txt").readText()
		val expectedOutput = resourceFile("$basePath/empty/output.txt").readText().trim()

		val actualOutput = convertEdgesToGraphvizText(input).trim()
		assertEquals(expectedOutput, actualOutput)
	}

	@Test
	fun `convertEdgesToGraphvizText creates valid DOT`() {
		val input = resourceFile("$basePath/default/input.txt").readText()
		val expectedOutput = resourceFile("$basePath/default/output.txt").readText().trim()

		val actualOutput = convertEdgesToGraphvizText(input).trim()
		assertEquals(expectedOutput, actualOutput)
	}

	@Test
	fun `convertEdgesToGraphvizText ignores blank lines`() {
		val input = resourceFile("$basePath/blankLines/input.txt").readText()
		val expectedOutput = resourceFile("$basePath/blankLines/output.txt").readText().trim()

		val actualOutput = convertEdgesToGraphvizText(input).trim()
		assertEquals(expectedOutput, actualOutput)
	}
}
