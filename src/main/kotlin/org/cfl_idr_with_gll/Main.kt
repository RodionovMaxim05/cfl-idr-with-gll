package org.cfl_idr_with_gll

import org.cfl_idr_with_gll.graph.removeValueflowUnreachable
import org.ucfs.input.DotParser
import java.io.File

/**
 * Main entry point for the CFL reachability analysis tool.
 *
 * This object implements a command-line interface for analyzing graph reachability
 * using context-free language (CFL) reachability algorithms with GLL parsing.
 * The tool processes Graphviz DOT files and computes under/over approximations
 * of reachable paths using various Dyck language grammars.
 *
 * ## Usage
 * ```
 * java -jar app.jar [options] <input.dot> <grammar>
 * ```
 *
 * ## Arguments
 * - `input.dot`: Path to the input Graphviz DOT file or edge list file
 * - `grammar`: The grammar type to use for analysis (see [SUPPORTED_GRAMMARS])
 *
 * ## Options
 * - `-o <path>`: Specify output file or directory (default: `src/main/resources/taint`)
 * - `-q`: Quiet mode - suppress detailed path output in results
 * - `valueflow`: Enable value-flow specific optimizations and constraints (`{s | s = [i ∗ ]i}`)
 *
 * ## Supported Grammars
 * - `parity`: (PAR) Parity grammar with k=1 - Parity condition
 * - `parity2`: (PAR2) Parity grammar with k=2 - Extended parity condition
 * - `parityK`: (PARk) Parity grammar with specified k (requires additional integer argument) - Extended parity condition
 * - `se`: (PAR2E) Structured equality grammar - Valid endpoints
 * - `project`: (PARUnl) Projection grammar - Projection to an unlabeled Dyck grammar
 * - `exclude`: (PARErase) Exclusion grammar - Erasing labels
 * - `all`: (COM) Comprehensive grammar
 * - `parityD`: (PARD) On-demand parity grammar
 * - `on-demand`: (COMD) On-demand combined method
 */
object Main {

	/** Default output directory for analysis results. */
	private const val DIRECTORY_OUTPUT = "src/main/resources/taint"

	/** Set of supported grammar types for reachability analysis. */
	private val SUPPORTED_GRAMMARS = setOf(
		"parity", "parity2", "parityK", "se", "project", "exclude", "all", "parityD", "on-demand"
	)

	/**
	 * Main entry point for the CFL reachability analysis tool.
	 *
	 * Parses command-line arguments, loads the input graph, performs reachability
	 * analysis using the specified grammar, and writes results to an output file.
	 *
	 * @param args command-line arguments:
	 *   - `input.dot`: Path to input file (required)
	 *   - `grammar`: Grammar type (required)
	 *   - `-o <path>`: Output path (optional)
	 *   - `-q`: Quiet mode (optional)
	 *   - `-valueflow`: Value-flow filtering
	 *   - For `parityK` grammar: additional integer parameter k
	 *
	 * @throws IllegalArgumentException if required arguments are missing or invalid
	 * @throws IllegalStateException if input file is not found or cannot be read
	 */
	@JvmStatic
	fun main(args: Array<String>) {
		var dotFilePath: String? = null
		var grammar: String? = null
		var parityK = 0
		var outputPath: String? = null
		var onDemand = false
		var parityD = false
		var quiet = false
		var valueflow = false

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

				"-valueflow" -> {
					valueflow = true
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
		} else if (grammar == "parityD") {
			grammar = "parity"
			parityD = true
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

		var inputGraph = DotParser().parseDot(finalGraphText)

		if (valueflow) {
			inputGraph = inputGraph.removeValueflowUnreachable()
		}

		val underPaths = getUnderApprox(inputGraph, valueflow)

		val overPaths = getMROverApprox(inputGraph, grammar, parityK, underApprox = underPaths, valueflow)

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

		if (!onDemand && !parityD) {
			println("Analysis completed. Results written to ${outputFile.absolutePath}")
			return
		}

		val onDemandPaths = getOnDemandMR(inputGraph, underPaths, overPaths, parityD, valueflow)

		outputFile.appendText("\nOn-Demand paths: ${onDemandPaths.size}\n")
		if (!quiet) {
			onDemandPaths.forEach { outputFile.appendText("\t$it\n") }
		}

		println("On-demand refinement completed. Results written to ${outputFile.absolutePath}")
	}
}
