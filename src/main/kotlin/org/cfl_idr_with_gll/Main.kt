package org.cfl_idr_with_gll

import org.ucfs.input.DotParser
import java.io.File

object Main {

	private const val DIRECTORY_INPUT = "taint"
	private const val DIRECTORY_OUTPUT = "taint-out"

	private var currentGrammar = "classic"
	private var currParityK = 2

	private val supportedGrammars = setOf(
		"parity", "parity2", "se", "project", "exclude", "all", "on-demand"
	)

	@JvmStatic
	fun main(args: Array<String>) {
		if (args.size < 2) {
			error("Usage: <dot-file-name-in-resources> <grammar>")
		}

		val dotFileName = args[0]
		currentGrammar = args[1]

		if (currentGrammar !in supportedGrammars) {
			error("Invalid grammar: $currentGrammar. Supported: $supportedGrammars")
		}

		val resourcePath = "/$DIRECTORY_INPUT/$dotFileName"
		val inputText = Main::class.java.getResourceAsStream(resourcePath)
			?.bufferedReader()?.use { it.readText() }
			?: error("Resource not found: $resourcePath")

//		val finalGraphText = if (isGraphvizFormat(resourcePath)) inputText
//		else convertEdgesToGraphvizText(inputText)
		val finalGraphText = convertEdgesToGraphvizText(inputText)

		val inputGraph = DotParser().parseDot(finalGraphText)

		val benchmarkName = dotFileName.substringBeforeLast(".")
		val outputDir = File(DIRECTORY_OUTPUT)
//		outputDir.mkdirs()
		val outputFile = File(outputDir, "$benchmarkName.out")
	}
}