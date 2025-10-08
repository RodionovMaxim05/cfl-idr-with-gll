package org.main

import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class TestMain {

	@Test
	fun testMainOutput() {
		val outputStream = ByteArrayOutputStream()
		val printStream = PrintStream(outputStream)
		System.setOut(printStream)

		main()

		val expected = buildString {
			appendLine("Hello, Kotlin!")
			for (i in 1..5) {
				appendLine("i = $i")
			}
		}

		assertEquals(expected.trim(), outputStream.toString().trim())
	}
}
