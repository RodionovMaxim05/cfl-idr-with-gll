# cfl-idr-with-gll

A Kotlin implementation of CFL-based approximation methods for interleaved Dyck reachability using the GLL-based algorithm.

## Problem Overview

Interleaved Dyck Reachability is a fundamental problem in static program analysis where path constraints are expressed as the shuffle of two Dyck languages - typically representing context sensitivity (parentheses) and field sensitivity (brackets). While this formulation enables high-precision analyses, the problem is known to be undecidable in general, and thus existing approaches resort to clever overapproximations.

This project implements approximation techniques based on the research paper ["CFL-based methods for approximating interleaved Dyck reachability" (Conrado & Pavlogiannis, 2025)](https://www.researchgate.net/publication/390804794_CFL-based_methods_for_approximating_interleaved_Dyck_reachability), which transforms the undecidable problem into a series of tractable CFL reachability problems solvable via the GLL-based algorithm from the [UCFS project](https://github.com/FormalLanguageConstrainedPathQuerying/UCFS).

### Supported Grammars

| Grammar Type | Description                                                                                                                                                                       | Article designation |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|
| `parity`     | Mutual refinement with k=1 parity condition on parentheses/brackets: requires the total count of parentheses/bracket symbols to be even                                           | `PAR`               |
| `parity2`    | Mutual refinement with k=2 parity condition: parentheses/brackets are split into 2 groups, count in each group must be even                                                       | `PAR2`              |
| `parityK`    | Mutual refinement with parameterized k-parity condition: parentheses/brackets are split into k groups                                                                             | `PARk`              |
| `se`         | Extends `PAR2` with a "valid endpoints" (structured equality) condition: parentheses/bracket substrings must start with `(`/`[` and end with `)`/`]`                              | `PAR2E`             |
| `project`    | Extends `PAR` with unlabeled projection: additionally runs reachability on the graph with all labels stripped, catching unreachable pairs where total nesting depth is unbalanced | `PARUnl`            |
| `exclude`    | Extends `PAR` with label erasure: iterates over each bracket label, erases it from the graph, and reruns `PAR` - exposing unreachable pairs that were hidden by that label        | `PARErase`          |
| `all`        | Combines `PAR2E` grammar with both the unlabeled projection phase and the label-erasure phase in the mutual refinement loop                                                       | `COM`               |
| `parityD`    | Same grammar as `PAR`, but run in on-demand mode: mutual refinement is performed independently for each queried node pair, filtering out irrelevant graph parts                   | `PARD`              |
| `on-demand`  | Same grammar as `COM`, but run in on-demand mode: combines all stronger grammars and graph simplifications with per-pair refinement                                               | `COMD`              |

## Architecture

The project is organized into two abstraction layers:

1. Programmatic API

	```kotlin
	// Under-approximation (guaranteed reachable)
	fun <V, L : ILabel> getUnderApprox(
		graph: InputGraph<V, L>,
		valueflow: Boolean = false,
		terminalFormat: ITerminalFormat = DefaultTerminalFormat
	): Set<Path<V>>

	// Over-approximation with mutual refinement  
	fun <V, L : ILabel> getMROverApprox(
		graph: InputGraph<V, L>,
		currGrammar: String,
		currParityK: Int = 1,
		underApprox: Set<Path<V>> = emptySet(),
		valueflow: Boolean = false,
		terminalFormat: ITerminalFormat = DefaultTerminalFormat
	): Set<Path<V>>

	// On-demand refinement for specific queries
	fun <V, L : ILabel> getOnDemandMR(
		graph: InputGraph<V, L>,
		underApprox: Set<Path<V>>,
		overApprox: Set<Path<V>>,
		parityD: Boolean = false,
		valueflow: Boolean = false,
		terminalFormat: ITerminalFormat = DefaultTerminalFormat
	): Set<Path<V>>
	```

	See more **[documentation](https://rodionovmaxim05.github.io/cfl-idr-with-gll/cfl-idr-with-gll/org.cfl_idr_with_gll/index.html)** to learn more about it.

2. Command-Line Interface

	```bash
	java -jar cfl-idr-with-gll-all.jar [options] <input.dot> <grammar>
	```

## Value-Flow Analysis Mode

The library provides a specialized value-flow analysis mode (`-valueflow` flag) optimized for memory value-flow analysis: a valid path must start with `[` (similar to the `store` operation) and end with `]` (similar to the `load` operation) with the same id.

`Recommendation` For manual preprocessing before analysis:

```kotlin
// Remove vertices unreachable from store/load operations
val filteredGraph = inputGraph.removeValueflowUnreachable(terminalFormat)
```

## Quick Start

### Requirements

- JDK 21+ (toolchain targets 21)
- Gradle Wrapper included (`./gradlew`)

### Building from Source

```bash
# Clone the repository
git clone https://github.com/RodionovMaxim05/cfl-idr-with-gll.git
cd cfl-idr-with-gll

# Build the project
./gradlew shadowJar -x test

# Initialize submodules (for tests)
git submodule update --init

# Run tests
./gradlew test
```

## Usage Examples

### CLI

```bash
# Basic analysis with parity grammar
java -jar build/libs/cfl-idr-with-gll-all.jar graph.dot parity

# Analysis with parity k=3 and custom output
java -jar build/libs/cfl-idr-with-gll-all.jar graph.dot parityK 3 -o taint-out

# Quiet mode (disable path recording)
java -jar build/libs/cfl-idr-with-gll-all.jar graph.dot parity -q -o result

# Value-flow analysis with parity grammar
java -jar build/libs/cfl-idr-with-gll-all.jar -valueflow graph.dot parity
```

### API Integration

```kotlin
import org.cfl_idr_with_gll.*

// Load graph from DOT file
val dotText = File("graph.dot").readText()
val graph = DotParser().parseDot(dotText)

// Compute approximations
val underPaths = getUnderApprox(graph)
val overPaths = getMROverApprox(graph, "parityK", parityK = 3, underApprox = underPaths)

// Refine on-demand
val onDemandPaths = getOnDemandMR(graph, underPaths, overPaths)
```

## Benchmark

More details about this can be found in the [`benchmark`](benchmark/) directory.

## License

Distributed under the [MIT License](https://choosealicense.com/licenses/mit/). See [`LICENSE`](LICENSE) for more information.
