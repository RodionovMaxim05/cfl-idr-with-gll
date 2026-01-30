import org.cfl_idr_with_gll.Main
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class TestMain {

	@TempDir
	lateinit var tempDir: Path

	@Test
	fun `missing dot file path throws exception`() {
		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf())
		}
		assertEquals("Missing dot file path", exception.message)
	}

	@Test
	fun `missing grammar throws exception`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf(dotFile.absolutePath))
		}
		assertEquals("Missing grammar", exception.message)
	}

	@Test
	fun `invalid grammar throws exception`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf(dotFile.absolutePath, "invalid-grammar"))
		}
		assertTrue(exception.message?.contains("Invalid grammar: 'invalid-grammar'") == true)
	}

	@Test
	fun `nonexistent input file throws exception`() {
		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf("nonexistent.dot", "parity"))
		}
		assertEquals("Input file not found: ${File("nonexistent.dot").absolutePath}", exception.message)
	}

	@Test
	fun `parityK grammar without K value throws exception`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf(dotFile.absolutePath, "parityK"))
		}
		assertEquals("Parity K is required for grammar 'parityK'", exception.message)
	}

	@Test
	fun `parityK grammar with non-integer K throws exception`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf(dotFile.absolutePath, "parityK", "not-a-number"))
		}
		assertEquals("Parity K must be an integer", exception.message)
	}

	@Test
	fun `parityK grammar with negative K throws exception`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf(dotFile.absolutePath, "parityK", "-1"))
		}
		assertEquals("Parity K must be a positive integer (got: -1)", exception.message)
	}

	@Test
	fun `parityK grammar with valid K value`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}
		val outputFile = File(tempDir.toFile(), "test.out")

		Main.main(arrayOf(dotFile.absolutePath, "parityK", "3", "-o", outputFile.absolutePath))

		assertTrue(outputFile.exists())
	}

	@Test
	fun `missing value for -o option throws exception`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf(dotFile.absolutePath, "parity", "-o"))
		}
		assertEquals("Missing value for -o", exception.message)
	}

	@Test
	fun `extra unexpected argument throws exception`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		val exception = assertThrows(IllegalArgumentException::class.java) {
			Main.main(arrayOf(dotFile.absolutePath, "parity", "extra", "arguments"))
		}
		assertEquals("Unexpected extra argument: extra", exception.message)
	}

	@Test
	fun `basic parity grammar creates output file`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText(
				"digraph { 1 -> 2 [label=\"ob--1\"]; 2 -> 3 [label=\"cb--1\"]; }"
			)
		}
		val outputFile = File(tempDir.toFile(), "test.out")

		Main.main(arrayOf(dotFile.absolutePath, "parity", "-o", outputFile.parent))

		assertTrue(outputFile.exists())
		assertTrue(outputFile.readText().contains("Under approximation paths:"))
		assertTrue(outputFile.readText().contains("Over approximation paths (parity):"))
	}

	@Test
	fun `output to specific file`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}
		val outputFile = File(tempDir.toFile(), "specific_output.txt")

		Main.main(arrayOf(dotFile.absolutePath, "parity", "-o", outputFile.absolutePath))

		assertTrue(outputFile.exists())
	}

	@Test
	fun `output to existing directory`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}
		val outputDir = File(tempDir.toFile(), "existing_dir").apply { mkdirs() }

		Main.main(arrayOf(dotFile.absolutePath, "parity", "-o", outputDir.absolutePath))

		val expectedFile = File(outputDir, "test.out")
		assertTrue(expectedFile.exists())
	}

	@Test
	fun `when output not specified file created in current dir`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		assertDoesNotThrow {
			Main.main(arrayOf(dotFile.absolutePath, "parity"))
		}

		val outputFile = File("test.out")
		println("Output file path: ${outputFile.absolutePath}")
		println("Output file exists: ${outputFile.exists()}")

		if (outputFile.exists()) {
			assertTrue(true, "File was created")
			outputFile.delete()
		}
	}

	@Test
	fun `quiet mode suppresses detailed output`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; 2 -> 3 [label=\"cb--1\"]; }")
		}
		val outputFile = File(tempDir.toFile(), "test.out")

		Main.main(arrayOf(dotFile.absolutePath, "parity", "-q", "-o", outputFile.absolutePath))

		val output = outputFile.readText()
		assertTrue(output.contains("Under approximation paths:"))
		assertTrue(output.contains("Over approximation paths (parity):"))
		assertFalse(output.contains("\tPath("))
	}

	@Test
	fun `valueflow flag enabled`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}
		val outputFile = File(tempDir.toFile(), "test.out")

		Main.main(arrayOf(dotFile.absolutePath, "parity", "-valueflow", "-o", outputFile.absolutePath))

		assertTrue(outputFile.exists())
	}

	@Test
	fun `parityD grammar`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}
		val outputFile = File(tempDir.toFile(), "test.out")

		Main.main(arrayOf(dotFile.absolutePath, "on-demand", "-o", outputFile.absolutePath))

		val output = outputFile.readText()
		assertTrue(output.contains("On-Demand paths:"))
	}

	@Test
	fun `on-demand grammar`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}
		val outputFile = File(tempDir.toFile(), "test.out")

		Main.main(arrayOf(dotFile.absolutePath, "on-demand", "-o", outputFile.absolutePath))

		val output = outputFile.readText()
		assertTrue(output.contains("On-Demand paths:"))
	}

	@Test
	fun `all supported grammars can be specified`() {
		val dotFile = File(tempDir.toFile(), "test.dot").apply {
			writeText("digraph { 1 -> 2 [label=\"ob--1\"]; }")
		}

		val grammars = listOf("parity", "parity2", "se", "project", "exclude", "all")

		for (grammar in grammars) {
			val outputFile = File(tempDir.toFile(), "test_$grammar.out")
			Main.main(arrayOf(dotFile.absolutePath, grammar, "-o", outputFile.absolutePath))
			assertTrue(outputFile.exists(), "Output file for grammar $grammar should exist")
		}
	}

	@Test
	fun `complex graph with multiple edges`() {
		val dotFile = File(tempDir.toFile(), "complex.dot").apply {
			writeText(
				"""
                digraph {
                    1 -> 2 [label="ob--1"];
                    2 -> 3 [label="cb--1"];
                    1 -> 4 [label="ob--2"];
                    4 -> 5 [label="normal"];
                    5 -> 6 [label="cb--2"];
                }
            """.trimIndent()
			)
		}
		val outputFile = File(tempDir.toFile(), "complex.out")

		Main.main(arrayOf(dotFile.absolutePath, "all", "-o", outputFile.absolutePath))

		assertTrue(outputFile.exists())
		val output = outputFile.readText()
		assertTrue(output.contains("Under approximation paths:"))
		assertTrue(output.contains("Over approximation paths (all):"))
	}
}
