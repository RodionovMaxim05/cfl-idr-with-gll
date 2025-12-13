# cfl-idr-with-gll

A Kotlin implementation of CFL-based approximation methods for interleaved Dyck reachability using the GLL
algorithm.

## Problem Overview

Interleaved Dyck Reachability is a fundamental problem in static program analysis where path constraints are expressed
as the shuffle of two Dyck languages - typically representing context sensitivity (parentheses) and field sensitivity (
brackets). While this formulation enables high-precision analyses, the general problem is known to be undecidable.

This project implements approximation techniques based on the research paper ["CFL-based methods for approximating
interleaved Dyck reachability" (Conrado & Pavlogiannis, 2025)](https://www.researchgate.net/publication/390804794_CFL-based_methods_for_approximating_interleaved_Dyck_reachability),
which transforms the undecidable problem into a series
of
tractable CFL reachability problems solvable via the GLL algorithm from
the [UCFS project](https://github.com/FormalLanguageConstrainedPathQuerying/UCFS).

### Supported Grammars

| Grammar Type | Description                             | Designation from the article |
|--------------|-----------------------------------------|------------------------------|
| `parity`     | Parity condition (k=1)                  | `PAR`                        |
| `parity2`    | Extended parity condition (k=2)         | `PAR2`                       |
| `parityK`    | Extended parity condition (parameter k) | `PARk`                       |
| `se`         | Valid endpoints                         | `PAR2E`                      |
| `project`    | Projection to an unlabeled Dyck grammar | `PARUnl`                     |
| `exclude`    | Erasing labels                          | `PARErase`                   |
| `all`        | Comprehensive grammar                   | `COM`                        |
| `on-demand`  | On-demand mutual refinement             | `COMD`                       |

## Architecture

The project is organized into two abstraction layers:

1. Programmatic API

```kotlin
// Under-approximation (guaranteed reachable)
fun <VertexType, LabelType : ILabel> getUnderApprox(
	graph: InputGraph<VertexType, LabelType>,
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<VertexType>>

// Over-approximation with mutual refinement  
fun <VertexType, LabelType : ILabel> getMROverApprox(
	graph: InputGraph<VertexType, LabelType>,
	currGrammar: String,
	currParityK: Int = 1,
	underApprox: Set<Path<VertexType>> = emptySet(),
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<V>>

// On-demand refinement for specific queries
fun <VertexType, LabelType : ILabel> getOnDemandMR(
	graph: InputGraph<VertexType, LabelType>,
	underApprox: Set<Path<VertexType>>,
	overApprox: Set<Path<VertexType>>,
	terminalFormat: ITerminalFormat = DefaultTerminalFormat
): Set<Path<VertexType>>
```

See more [_**documentation**_](src/main/kotlin/org/cfl_idr_with_gll/Approximation.kt) to learn more about it.

2. Command-Line Interface

```bash
java -jar cfl-idr-with-gll.jar [options] <input.dot> <grammar>
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

./gradlew build

# Run tests

./gradlew test
```

## Usage Examples

### CLI

```bash
# Basic analysis with parity grammar
java -jar build/libs/cfl-idr-with-gll.jar graph.dot parity

# Analysis with parity k=3 and custom output
java -jar build/libs/cfl-idr-with-gll.jar graph.dot parityK 3 -o taint-out

# Quiet mode (disable path recording)
java -jar build/libs/cfl-idr-with-gll.jar graph.dot parity -q -o result
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

Distributed under the [MIT License](https://choosealicense.com/licenses/mit/). See [`LICENSE`](LICENSE) for more
information.

## Author

* **Maxim Rodionov:** [GitHub](https://github.com/RodionovMaxim05), [Telegram](https://t.me/Maxoon22)
