package org.cfl_idr_with_gll

import org.ucfs.input.DotParser
import java.io.File

object Main {

	private const val DIRECTORY_OUTPUT = "src/main/resources/taint"

	private var currentGrammar = "classic"
	private var currParityK = 2

	private val supportedGrammars = setOf(
		"parity", "parity2", "se", "project", "exclude", "all", "on-demand"
	)

	@JvmStatic
	fun main(args: Array<String>) {
		if (args.size < 2) {
			error("Usage: <dot-file-paths> <grammar>")
		}

		val dotFilePath = args[0]
		currentGrammar = args[1]

		if (currentGrammar !in supportedGrammars) {
			error("Invalid grammar: $currentGrammar. Supported: $supportedGrammars")
		}

		val inputFile = File(dotFilePath)
		if (!inputFile.exists()) {
			error("Input file not found: ${inputFile.absolutePath}")
		}

		val inputText = inputFile.readText()

		val finalGraphText = if (isGraphvizFormat(inputFile)) {
			inputText
		} else {
			convertEdgesToGraphvizText(inputText)
		}

		val inputGraph = DotParser().parseDot(finalGraphText)

		val benchmarkName = inputFile.name.substringBeforeLast(".")
		val outputDir = File(DIRECTORY_OUTPUT)
		outputDir.mkdirs()
		val outputFile = File(outputDir, "$benchmarkName.out")

		val underPaths = getUnderApprox(inputGraph)

		val overPaths = getMROverApprox(inputGraph, "parity2", 2)

		outputFile.writeText("Under approximation paths:\n")
		for (path in underPaths) {
			outputFile.appendText("\t$path\n")
		}

		outputFile.appendText("\nOver approximation paths:\n")
		for (path in overPaths) {
			outputFile.appendText("\t$path\n")
		}
	}
}
