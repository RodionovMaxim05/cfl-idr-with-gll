<a name="readme-top"></a>

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

The following section presents a performance analysis comparing this Kotlin (GLL-based) implementation
against the [reference Go implementation](https://github.com/kobusgiovanna/cfl-idr/tree/main)
(or more precisely, a [version with minor modifications](https://github.com/RodionovMaxim05/cfl-idr/commits/main/)),
as well as a comparison of various approximation strategies within the Kotlin version.
All raw results, charts, and scripts are available in the [`benchmark/`](benchmark/) directory.

---

# Performance Analysis

## System Configuration

- **CPU:**  AMD Ryzen 9 7900X, 12 physical, 24 logical cores;

- **RAM:** 128 Gb, 3600 MHz, DDR5;

- **OS:** Linux Ubuntu 22.04;

- **Timeout:** 2 hours (7200 seconds) per benchmark execution.

## Notation and Conventions

The following conventions are used:

1. **`T/O`** (Time out) - a response was not received within the allotted 7200 seconds (2 hours);
2. **`OOM`** (Out of Memory) - the tool ran out of 128 GiB of RAM;
3. **`SOF`** (Stack Overflow Error) - the tool ran out of 12 MiB of stack space.

## Dataset Characteristics

All graphs were obtained from publicly available sources:

- `taint` - graphs from the [taint analysis for Android](https://dl.acm.org/doi/10.1145/2771783.2771803), taken from [this repository](https://github.com/sdingcn/mutual-refinement/tree/main/exp). This data exactly matches the data analyzed in the underlying paper.
- `taint_additional` - graphs from the same source. They relate to the same area of taint analysis but were not included in the evaluation of the underlying paper; the suffix _additional denotes this distinction.
- `valueflow` - graphs from [value-flow analysis for LLVM](https://dl.acm.org/doi/10.1145/2892208.2892235), also sourced from the same repository above.
- `graphs_unlimited` - Java program-analysis graphs from [this repository](https://github.com/verbinna22/graphs_unlimited/tree/main). In the source repository, each project includes two files:
  - `<name>.g` - original graph extracted from source code;
  - `<name>_slx.g` - context-reduced variant.  
	Labels were uniformly mapped to Dyck symbols:
   	- Store / Load → Open/Close Brackets
   	- Assign Open / Assign Close → Open/Close Parentheses
   	- Everything else → Normal label

Below are the key properties of each graph. $|V|$ and $|E|$ represent the number of nodes and edges, respectively; $n_α$ and $n_β$ represent the number of parenthesis and bracket labels present in the graph.

### taint

| Benchmark    | $\|V\|$ | $\|E\|$ | $n_a$ | $n_b$ |
|--------------|---------|---------|-------|-------|
| backflash    | 544     | 2048    | 455   | 6     |
| batterydoc   | 1 674   | 4 790   | 1 132 | 87    |
| droidkongfu  | 734     | 1 983   | 367   | 39    |
| fakebanker   | 434     | 1 103   | 209   | 25    |
| fakedaum     | 1 144   | 2 603   | 715   | 42    |
| faketaobao   | 222     | 450     | 114   | 10    |
| jollyserv    | 488     | 998     | 236   | 28    |
| loozfon      | 152     | 323     | 78    | 3     |
| roidsec      | 553     | 2 026   | 362   | 13    |
| uranai       | 568     | 1 246   | 357   | 8     |
| zertsecurity | 281     | 710     | 166   | 9     |

### taint_additional

| Benchmark | $\|V\|$ | $\|E\|$ | $n_a$  | $n_b$ |
|-----------|---------|---------|--------|-------|
| phospy    | 4 402   | 15 660  | 2 795  | 118   |
| scipiex   | 1 809   | 5 820   | 844    | 173   |
| simhosy   | 4 253   | 13 768  | 2 894  | 330   |
| skullkey  | 18 862  | 69 599  | 12 316 | 1 340 |

### valueflow

| Benchmark | $\|V\|$ | $\|E\|$ | $n_a$  | $n_b$ |
|-----------|---------|---------|--------|-------|
| cactus    | 101 325 | 114 805 | 9 840  | 1     |
| imagick   | 103 594 | 131 707 | 13 751 | 1     |
| leela     | 16 134  | 19 110  | 829    | 1     |
| nab       | 12 727  | 13 605  | 331    | 1     |
| omnetpp   | 171 502 | 184 601 | 7 599  | 1     |
| parest    | 84 355  | 93 493  | 2 354  | 1     |
| perlbench | 125 345 | 160 958 | 14 327 | 1     |
| povray    | 61 802  | 71 892  | 5 844  | 1     |
| x264      | 49 806  | 56 376  | 1 546  | 1     |
| xz        | 9 918   | 10 767  | 684    | 1     |

### graphs_unlimited

| Benchmark                     | $\|V\|$ | $\|E\|$   | $n_a$  | $n_b$ |
|-------------------------------|---------|-----------|--------|-------|
| basic                         | 232     | 477       | 25     | 15    |
| basic_slx                     | 232     | 477       | 0      | 15    |
| collection                    | 167     | 313       | 19     | 10    |
| collection_slx                | 167     | 313       | 0      | 10    |
| com_fasterxml_jackson         | 243 766 | 1 070 735 | 8 149  | 2 870 |
| com_fasterxml_jackson_slx     | 243 766 | 1 070 735 | 0      | 2 870 |
| cornerCases                   | 247     | 531       | 30     | 9     |
| cornerCases_slx               | 247     | 531       | 0      | 9     |
| generalJava                   | 157     | 327       | 19     | 11    |
| generalJava_slx               | 157     | 327       | 0      | 11    |
| org_apache_jackrabbit         | 362 181 | 1 740 661 | 15 323 | 7 714 |
| org_apache_jackrabbit_slx     | 362 181 | 1 740 661 | 0      | 7 714 |
| org_jivesoftware_openfire     | 298 555 | 1 006 577 | 14 159 | 5 270 |
| org_jivesoftware_openfire_slx | 298 555 | 1 006 577 | 0      | 5 270 |
| reactor                       | 26 999  | 84 197    | 1 567  | 414   |
| reactor_slx                   | 26 999  | 84 197    | 0      | 414   |

> [!IMPORTANT]
> **Important warning about the measurement conditions**
>
> Performance measurements for the `taint` and `taint_additional` datasets were performed on an [optimized version of UCFS](libs/solver-new.jar) - the optimization code is available in [PR](https://github.com/FormalLanguageConstrainedPathQuerying/UCFS/pull/50).
>
> All other measurements (for the `valueflow` and `graphs_unlimited` sets) were performed on the [original UCFS version](libs/solver-original.jar) without optimizations - - the original code is available [here](https://github.com/FormalLanguageConstrainedPathQuerying/UCFS/tree/e9c92ba1a85e95e941d04163cd4b55f50686c1f7).

## Implementation Comparison: Go vs Kotlin

### Methodology and Statistical Reliability

For the `taint` dataset, each benchmark execution was repeated 5 times. The results presented in the charts below are the average values ​​of these measurements.  
The sample standard deviation for all measured execution times does not exceed 10% (only 3 of 176 measurements exceed 7%, and all of these measurements were obtained using the Kotlin implementation, with an average time of less than 0.5 seconds, which can be explained by natural error). This variance is low enough to allow meaningful conclusions, as the performance differences between the Go and Kotlin implementations significantly exceed this statistical limit.

For the `taint_additional` dataset, each benchmark execution was repeated 3 times. The sample standard deviation for all measured execution times does not exceed 2%. This variance is also low enough to draw meaningful conclusions.

95% confidence intervals were also calculated for all average execution times.

**Correctness verification:** For all successfully completed graph–grammar pairs, both implementations produced identical path counts ($R_{under}$, $R_{over}$, and their difference), confirming semantic equivalence of results.

### Results: `taint` Dataset

The comparison was conducted on the [`taint dataset`](benchmark/external-repos/cfl-idr/src/main/taint/).

#### Implementation Comparison (Go vs. Kotlin per Grammar)

The following charts compare the execution times between the Go and Kotlin implementations across different grammars:

|                                                                 |                                                               |
|-----------------------------------------------------------------|---------------------------------------------------------------|
| **Grammar `PAR`:**                                              | **Grammar `PAR2`:**                                           |
| <img alt="PAR" src="benchmark/results/taint/parity_go_vs_kotlin_taint.png">       | <img alt="PAR2" src="benchmark/results/taint/parity2_go_vs_kotlin_taint.png">   |
| **Grammar `PAR2E`:**                                            | **Grammar `PARUnl`:**                                         |
| <img alt="PAR2E" src="benchmark/results/taint/se_go_vs_kotlin_taint.png">         | <img alt="PARUnl" src="benchmark/results/taint/project_go_vs_kotlin_taint.png"> |
| **Grammar `PARErase`:**                                         | **Grammar `PARD`:**                                           |
| <img alt="PARErase" src="benchmark/results/taint/exclude_go_vs_kotlin_taint.png"> | <img alt="PARD" src="benchmark/results/taint/parityD_go_vs_kotlin_taint.png">   |
| **Grammar `COM`:**                                              | **Grammar `COMD`:**                                           |
| <img alt="COM" src="benchmark/results/taint/all_go_vs_kotlin_taint.png">          | <img alt="COMD" src="benchmark/results/taint/on-demand_go_vs_kotlin_taint.png"> |

There is a consistent pattern: the Kotlin (using GLL-based algorithm) implementation is faster than the original Go implementation for almost all grammars and graphs. The exceptions are limited to 1 graphs when using the `PARUnl` grammar.

#### Performance Hierarchy Analysis

Beyond absolute speed, the implementations exhibit different performance hierarchies regarding how they handle various
grammar types. The performance hierarchy is calculated based on the sum of execution times for all graphs within each
grammar. The total execution time follows these sequences (from fastest to slowest):

| Implementation    | Runtime Hierarchy (From Fast to Slow)                                      |
|-------------------|----------------------------------------------------------------------------|
| **Kotlin (GLL)**  | `PAR` → `PAR2` → `PARErase` → `PARD` → `PAR2E` → `PARUnl` → `COM` → `COMD` |
| **Go (Original)** | `PAR` → `PARUnl` → `PAR2` → `PARD` → `COM` → `PAR2E` → `PARErase` → `COMD` |

#### Accuracy of approximations

<figure style="text-align: center;"> <img alt="taint statistics" src="benchmark/results/taint/statistics_taint.png"/> <figcaption>Approximation spread (R<sub>over</sub> - R<sub>under</sub>) for all graphs and grammars in the <code>taint</code></figcaption></figure>

#### Per-Graph Trade-off Analysis (Time vs. Precision)

The following charts illustrate the relationship between execution time and approximation accuracy (measured as $R_{over} - R_{under}$) for individual graphs:

|                                                                                      |                                                                                  |
|--------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| **Graph `backflash`:**                                                               | **Graph `batterydoc`:**                                                          |
| <img alt="backflash" src="benchmark/results/taint/application/backflash_time_vs_diff_taint.png">       | <img alt="batterydoc" src="benchmark/results/taint/application/batterydoc_time_vs_diff_taint.png"> |
| **Graph `droidkongfu`:**                                                             | **Graph `fakebanker`:**                                                          |
| <img alt="droidkongfu" src="benchmark/results/taint/application/droidkongfu_time_vs_diff_taint.png">   | <img alt="fakebanker" src="benchmark/results/taint/application/fakebanker_time_vs_diff_taint.png"> |
| **Graph `fakedaum`:**                                                                | **Graph `faketaobao`:**                                                          |
| <img alt="fakedaum" src="benchmark/results/taint/application/fakedaum_time_vs_diff_taint.png">         | <img alt="faketaobao" src="benchmark/results/taint/application/faketaobao_time_vs_diff_taint.png"> |
| **Graph `jollyserv`:**                                                               | **Graph `loozfon`:**                                                             |
| <img alt="jollyserv" src="benchmark/results/taint/application/jollyserv_time_vs_diff_taint.png">       | <img alt="loozfon" src="benchmark/results/taint/application/loozfon_time_vs_diff_taint.png">       |
| **Graph `roidsec`:**                                                                 | **Graph `uranai`:**                                                              |
| <img alt="roidsec" src="benchmark/results/taint/application/roidsec_time_vs_diff_taint.png">           | <img alt="uranai" src="benchmark/results/taint/application/uranai_time_vs_diff_taint.png">         |
| **Graph `zertsecurity`:**                                                            |                                                                                  |
| <img alt="zertsecurity" src="benchmark/results/taint/application/zertsecurity_time_vs_diff_taint.png"> |                                                                                  |

### Results: `taint_additional` Dataset

The comparison was conducted on the [`taint_additional dataset`](benchmark/taint_additional/), which contains larger graphs.

#### Implementation Comparison (Go vs. Kotlin per Grammar)

|                                                                                       |                                                                                     |
|---------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| **Grammar `PAR`:**                                                                    | **Grammar `PAR2`:**                                                                 |
| <img alt="PAR" src="benchmark/results/taint_additional/parity_go_vs_kotlin_taint_additional.png">       | <img alt="PAR2" src="benchmark/results/taint_additional/parity2_go_vs_kotlin_taint_additional.png">   |
| **Grammar `PAR2E`:**                                                                  | **Grammar `PARUnl`:**                                                               |
| <img alt="PAR2E" src="benchmark/results/taint_additional/se_go_vs_kotlin_taint_additional.png">         | <img alt="PARUnl" src="benchmark/results/taint_additional/project_go_vs_kotlin_taint_additional.png"> |
| **Grammar `PARErase`:**                                                               | **Grammar `PARD`:**                                                                 |
| <img alt="PARErase" src="benchmark/results/taint_additional/exclude_go_vs_kotlin_taint_additional.png"> | <img alt="PARD" src="benchmark/results/taint_additional/parityD_go_vs_kotlin_taint_additional.png">   |
| **Grammar `COM`:**                                                                    | **Grammar `COMD`:**                                                                 |
| <img alt="COM" src="benchmark/results/taint_additional/all_go_vs_kotlin_taint_additional.png">          | <img alt="COMD" src="benchmark/results/taint_additional/on-demand_go_vs_kotlin_taint_additional.png"> |

On the larger `taint_additional` dataset, both implementations experience timeout issues (`T/O`), and the GLL-based implementation also experiences out-of-memory issues (`OOM`) for some grammar-graph combinations. Overall, however, the GLL-based implementation provided the answer in more cases. However, where comparisons are possible:

1. Successful completions:
	- On `scipiex` (`PAR`): Kotlin 25.8s vs. Go 2256s (87× speedup)
	- On `simhosy` (`PAR`): Kotlin 15.5s vs. Go 1257s (81× speedup)

2. Challenges:
	- Kotlin encounters `OOM` with `PARErase` grammar on `phospy`
	- Go experiences timeouts more frequently across grammars

#### Performance Shift: `PARErase` vs. `PARD`

In the smaller `taint` dataset, `PARErase` was faster than `PARD`. However, in `taint_additional`, this relationship flips
due to the multi-pass execution bottleneck.

This can be explained by the fact that as the graph size and the number of unique bracket identifiers ($n_b$) increase, `PARErase's` performance significantly decreases: the algorithm performs an exhaustive path search in a loop for each unique bracket identifier ([code](src/main/kotlin/org/cfl_idr_with_gll/Approximation.kt#L405)).

#### Accuracy of approximations

<figure style="text-align: center;"> <img alt="taint_additional statistics" src="benchmark/results/taint_additional/statistics_taint_additional.png"/> <figcaption>Approximation spread (R<sub>over</sub> - R<sub>under</sub>) for all graphs and grammars in the <code>taint_additonal</code></figcaption></figure>

#### Per-Graph Trade-off Analysis (Time vs. Precision)

|                                                                                                  |                                                                                                  |
|--------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| **Graph `phospy`:**                                                                              | **Graph `scipiex`:**                                                                             |
| <img alt="phospy" src="benchmark/results/taint_additional/application/phospy_time_vs_diff_taint_additional.png">   | <img alt="scipiex" src="benchmark/results/taint_additional/application/scipiex_time_vs_diff_taint_additional.png"> |
| **Graph `simhosy`:**                                                                             |                                                                                                  |
| <img alt="simhosy" src="benchmark/results/taint_additional/application/simhosy_time_vs_diff_taint_additional.png"> |                                                                                                  |

### Summary and Conclusions

1. **Performance Improvement:** The Kotlin implementation using the GLL-based algorithm provides a significant average speedup (13.6x) compared to the Go benchmark solver in benchmarks where both implementations succeed.

2. **Correctness preserved:** Identical approximation results across implementations validate the semantic fidelity of the UCFS-based approach.

3. **Systemic equals bottleneck:** The primary performance limitation stems from inefficient equality comparisons within UCFS. During parse forest construction and traversal, the solver frequently compares complex derivation structures. This issue manifests most severely in grammars producing large intermediate structures (`PARUnl`, multi-pass variants) but affects all grammars on sufficiently large inputs.

4. **Scalability trade-offs:** While Kotlin handles moderate-scale graphs more efficiently, both implementations face fundamental scalability barriers on the largest inputs.

## Data set analysis

### Value-flow

The comparison was conducted on the [`valueflow dataset`](benchmark/valueflow/).

#### Per-Graph Trade-off Analysis (Time vs. Precision)

|                                                                                  |                                                                                    |
|----------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| **Graph `cactus`:**                                                              | **Graph `leela`:**                                                                 |
| <img alt="cactus" src="benchmark/results/valueflow/application/cactus_time_vs_diff_valueflow.png"> | <img alt="leela" src="benchmark/results/valueflow/application/leela_time_vs_diff_valueflow.png">     |
| **Graph `nab`:**                                                                 | **Graph `omnetpp`:**                                                               |
| <img alt="nab" src="benchmark/results/valueflow/application/nab_time_vs_diff_valueflow.png">       | <img alt="omnetpp" src="benchmark/results/valueflow/application/omnetpp_time_vs_diff_valueflow.png"> |
| **Graph `parest`:**                                                              | **Graph `povray`:**                                                                |
| <img alt="parest" src="benchmark/results/valueflow/application/parest_time_vs_diff_valueflow.png"> | <img alt="povray" src="benchmark/results/valueflow/application/povray_time_vs_diff_valueflow.png">   |
| **Graph `x264`:**                                                                | **Graph `xz`:**                                                                    |
| <img alt="x264" src="benchmark/results/valueflow/application/x264_time_vs_diff_valueflow.png">     | <img alt="xz" src="benchmark/results/valueflow/application/xz_time_vs_diff_valueflow.png">           |

Each benchmark in the `valueflow` dataset was executed 3 times; reported values represent averages and 95% confidence intervals.

All grammars exhibit almost identical runtime behavior. At the same time, the `imagick` and `perlbench` graphs did meet the time-out.

This suggests that in the meaning value-flow mode, all grammars work approximately the same way, regardless of their complexity.

This happens because in value-flow mode, the analysis is limited to only those paths that start with `[` and end with `]`. For these graphs, this significantly narrows the search area (for example, only when preprocessing ([function removeValueflowUnreachable](src/main/kotlin/org/cfl_idr_with_gll/graph/ValueflowExtensions.kt#L24)) the `x264` graph, the number of vertices decreases from **49 806** to **13 044**, and the number of edges from **56 376** to **14 351**). Therefore, the difference between grammars becomes insignificant.

However, the approximation yielded an accurate result (as confirmed by the results of the paper). This means that this test suite is not particularly challenging.

### Graphs Unlimited

The comparison was conducted on the [`graphs_unlimited dataset`](benchmark/graphs_unlimited/).  
The `graphs_unlimited` dataset represents the most demanding benchmarks for Interleaved Dyck Reachability. Due to their scale, measurements were limited to a single run per graph using only the simplest grammar (`PAR`) to establish a feasibility baseline.

<figure style="text-align: center;"> <img alt="graphs_unlimited parity perfomance" src="benchmark/results/graphs_unlimited/parity_comparison_of_times_graphs_unlimited.png"/> <figcaption>Performance of <code>PAR</code> grammar for <code>graphs_unlimited</code></figcaption></figure>

- For all larger graphs in this dataset the basic `PAR` grammar failed to complete due to StackOverflowError (`SOF`), Out of Memory (`OOM`), or Timeout (`T/O`).

#### Stack Overflow in UCFS

Three graphs triggered java.lang.StackOverflowError despite increasing the JVM stack size to 12 MB (12× default):

```bash
Exception in thread "main" java.lang.StackOverflowError
  at org.ucfs.grammar.combinator.regexp.Regexp$DefaultImpls.getAlphabet(Regexp.kt:30)
  at org.ucfs.grammar.combinator.regexp.Alternative.getAlphabet(Alternative.kt:6)
  ...
```

The error occurs due to the recursive traversal of the grammar combinator structure in UCFS during the construction of the rsm (Recursive State Machine) - this is a limitation of the current recursive implementation when processing large grammars.

#### Precision for Successful Subsets

<figure style="text-align: center;"> <img alt="graphs_unlimited statistics" src="benchmark/results/graphs_unlimited/statistics_graphs_unlimited.png"/> <figcaption>R<sub>over</sub> - R<sub>under</sub> for all <code>graphs_unlimited</code> graphs and grammars</figcaption></figure>

For the subset of graphs that did complete (e.g., basic, collection, generalJava), the precision was high.

- In most completed cases like collection_slx and generalJava_slx, the precision gap $|R_{over} - R_{under}|$ was 0, indicating that the approximation reached the exact solution.
- Even in cases with a gap, such as basic or cornerCases, the difference remained constant at 2 across almost all grammar types.

## License

Distributed under the [MIT License](https://choosealicense.com/licenses/mit/). See [`LICENSE`](LICENSE) for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>
