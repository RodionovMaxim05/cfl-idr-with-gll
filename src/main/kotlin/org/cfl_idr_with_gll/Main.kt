package org.cfl_idr_with_gll

import org.ucfs.input.DotParser
import java.io.File

object Main {

	private const val DIRECTORY_OUTPUT = "src/main/resources/taint"
	private val SUPPORTED_GRAMMARS = setOf(
		"parity", "parity2", "parityK", "se", "project", "exclude", "all", "on-demand"
	)

	@JvmStatic
	fun main(args: Array<String>) {
		var dotFilePath: String? = null
		var grammar: String? = null
		var parityK = 0
		var outputPath: String? = null
		var onDemand = false
		var quiet = false

		var i = 0
		while (i < args.size) {
			when (args[i]) {
				"-o" -> {
					if (i + 1 >= args.size) error("Missing value for -o")
					outputPath = args[++i]
				}

				"-q" -> {
					quiet = true
				}

				else -> {
					if (dotFilePath == null) {
						dotFilePath = args[i]
					} else if (grammar == null) {
						grammar = args[i]
					} else if (parityK == 0 && grammar == "parityK") {
						parityK = args[i].toIntOrNull() ?: error("Parity K must be an integer")
					} else {
						error("Unexpected extra argument: ${args[i]}")
					}
				}
			}
			i++
		}

		require(dotFilePath != null) { "Missing dot file path" }
		require(grammar != null) { "Missing grammar" }
		require(grammar in SUPPORTED_GRAMMARS) {
			"Invalid grammar: '$grammar'. Supported: $SUPPORTED_GRAMMARS"
		}

		if (grammar == "parityK") {
			require(parityK != 0) { "Parity K is required for grammar '$grammar'" }
			require(parityK > 0) { "Parity K must be a positive integer (got: $parityK)" }
		} else if (grammar == "on-demand") {
			grammar = "all"
			onDemand = true
		}

		val inputFile = File(dotFilePath).apply {
			require(exists()) { "Input file not found: ${absolutePath}" }
		}

		val benchmarkName = inputFile.name.substringBeforeLast(".")
		val outputFile = if (outputPath != null) {
			val output = File(outputPath)
			if (outputPath == "." || (output.exists() && output.isDirectory)) {
				output.mkdirs()
				File(output, "$benchmarkName.out")
			} else if (output.name.contains('.')) {
				output.parentFile?.mkdirs()
				output
			} else {
				output.mkdirs()
				File(output, "$benchmarkName.out")
			}
		} else {
			val outputDir = File(DIRECTORY_OUTPUT).apply { mkdirs() }
			File(outputDir, "$benchmarkName.out")
		}

		val inputText = inputFile.readText()
		val finalGraphText = if (isGraphvizFormat(inputFile)) {
			inputText
		} else {
			convertEdgesToGraphvizText(inputText)
		}

		val inputGraph = DotParser().parseDot(finalGraphText)

		val underPaths = getUnderApprox(inputGraph)

		val overPaths = getMROverApprox(inputGraph, grammar, parityK, underPaths)

		outputFile.writeText(buildString {
			append("Under approximation paths: ${underPaths.size}\n")
			if (!quiet) {
				underPaths.forEach { append("\t$it\n") }
			}

			append("\nOver approximation paths (${grammar}): ${overPaths.size}\n")
			if (!quiet) {
				overPaths.forEach { append("\t$it\n") }
			}
		})

		if (!onDemand) {
			return
		}

		val onDemandPaths = getOnDemandMR(inputGraph, underPaths, overPaths)

		outputFile.appendText("\nOn-Demand paths: ${onDemandPaths.size}\n")
		if (!quiet) {
			onDemandPaths.forEach { outputFile.appendText("\t$it\n") }
		}

		println("Finished. Results written to ${outputFile.absolutePath}")
	}
}
