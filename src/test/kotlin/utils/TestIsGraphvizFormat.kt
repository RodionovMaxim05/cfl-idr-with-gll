package utils

import org.cfl_idr_with_gll.isGraphvizFormat
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class TestIsGraphvizFormat {

	private fun resourceFile(name: String): File {
		val url = this::class.java.classLoader.getResource(name)
			?: error("Resource not found: $name")
		return File(url.toURI())
	}

	@Test
	fun `isGraphvizFormat returns true for valid DOT with digraph`() {
		val file = resourceFile("utils/convertEdgesToGraphviz/default/output.txt")
		assertEquals(true, isGraphvizFormat(file))
	}

	@Test
	fun `isGraphvizFormat returns true for empty DOT file`() {
		val file = resourceFile("utils/convertEdgesToGraphviz/empty/output.txt")
		assertEquals(true, isGraphvizFormat(file))
	}

	@Test
	fun `isGraphvizFormat returns true for valid DOT with leading blank lines digraph`() {
		val file = resourceFile("utils/convertEdgesToGraphviz/blankLines/output.txt")
		assertEquals(true, isGraphvizFormat(file))
	}

	@Test
	fun `isGraphvizFormat returns false for edge list input`() {
		val file = resourceFile("utils/convertEdgesToGraphviz/default/input.txt")
		assertEquals(false, isGraphvizFormat(file))
	}

	@Test
	fun `isGraphvizFormat returns false for empty input file`() {
		val file = resourceFile("utils/convertEdgesToGraphviz/empty/input.txt")
		assertEquals(false, isGraphvizFormat(file))
	}

	@Test
	fun `isGraphvizFormat returns false for edges list input with leading blank lines`() {
		val file = resourceFile("utils/convertEdgesToGraphviz/blankLines/input.txt")
		assertEquals(false, isGraphvizFormat(file))
	}
}
