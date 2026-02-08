# Performance Analysis

## Introduction

This report presents a performance analysis of the implementation of Interleaved Dyck Reachability approximation
algorithms. The comparison was made between the reference implementation in Go and a new implementation in Kotlin (using [UCFS](https://github.com/FormalLanguageConstrainedPathQuerying/UCFS), a context-free reachability solver based on the GLL algorithm), as well as a comparison of various approximation strategies within the Kotlin version.  
All results and graphs are available in the current directory.

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

$|V|$ and $|E|$ represent the number of nodes and edges, respectively; $n_α$ and $n_β$ represent the number of parenthesis and bracket labels present in the graph.

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

## Implementation Comparison: Go vs Kotlin

### Methodology and Statistical Reliability

For the `taint` dataset, each benchmark execution was repeated 5 times. The results presented in the charts below are the average values ​​of these measurements.  
The sample standard deviation for all measured execution times does not exceed 18% (only 7 of 176 measurements exceed 8%, and all of these measurements were obtained using the Kotlin implementation, with an average time of less than 0.7 seconds, which can be explained by natural error). This variance is low enough to allow meaningful conclusions, as the performance differences between the Go and Kotlin implementations significantly exceed this statistical limit.

For the `taint_additional` dataset, each benchmark execution was repeated 3 times. The sample standard deviation for all measured execution times does not exceed 2%. This variance is also low enough to draw meaningful conclusions.

95% confidence intervals were also calculated for all average execution times.

**Correctness verification:** For all successfully completed graph–grammar pairs, both implementations produced identical path counts ($R_{under}$, $R_{over}$, and their difference), confirming semantic equivalence of results.

### Results: `taint` Dataset

The comparison was conducted on the [`taint dataset`](../taint/).

#### Implementation Comparison (Go vs. Kotlin per Grammar)

The following charts compare the execution times between the Go and Kotlin implementations across different grammars:

|                                                                 |                                                               |
|-----------------------------------------------------------------|---------------------------------------------------------------|
| **Grammar `PAR`:**                                              | **Grammar `PAR2`:**                                           |
| <img alt="PAR" src="taint/parity_go_vs_kotlin_taint.png">       | <img alt="PAR2" src="taint/parity2_go_vs_kotlin_taint.png">   |
| **Grammar `PAR2E`:**                                            | **Grammar `PARUnl`:**                                         |
| <img alt="PAR2E" src="taint/se_go_vs_kotlin_taint.png">         | <img alt="PARUnl" src="taint/project_go_vs_kotlin_taint.png"> |
| **Grammar `PARErase`:**                                         | **Grammar `PARD`:**                                           |
| <img alt="PARErase" src="taint/exclude_go_vs_kotlin_taint.png"> | <img alt="PARD" src="taint/parityD_go_vs_kotlin_taint.png">   |
| **Grammar `COM`:**                                              | **Grammar `COMD`:**                                           |
| <img alt="COM" src="taint/all_go_vs_kotlin_taint.png">          | <img alt="COMD" src="taint/on-demand_go_vs_kotlin_taint.png"> |

There is a consistent pattern: the Kotlin (using GLL-based algorithm) implementation is faster than the original Go implementation for almost all grammars and graphs. The exceptions are limited to 3 graphs when using the `PARUnl` grammar and 1 graph when using the `COM` grammar.

#### Performance Hierarchy Analysis

Beyond absolute speed, the implementations exhibit different performance hierarchies regarding how they handle various
grammar types. The performance hierarchy is calculated based on the sum of execution times for all graphs within each
grammar. The total execution time follows these sequences (from fastest to slowest):

| Implementation    | Runtime Hierarchy (From Fast to Slow)                                      |
|-------------------|----------------------------------------------------------------------------|
| **Kotlin (GLL)**  | `PAR` → `PAR2` → `PARErase` → `PARD` → `PAR2E` → `PARUnl` → `COM` → `COMD` |
| **Go (Original)** | `PAR` → `PARUnl` → `PAR2` → `PARD` → `COM` → `PAR2E` → `PARErase` → `COMD` |

### Problem area: `PARUnl` Grammar (Projected/Unlabeled Dyck)

An analysis of the results and profiling data revealed a critical bottleneck in the grammar `PARUnl` - the `equals`
operation:

<figure style="text-align: center;"> <img alt="profiling grammar `project`" src="figures/profiling_project.jpg"/> <figcaption>The results of profiling the <code>taint/batterydoc.dot</code> graph using the grammar <code>PARUnl</code></figcaption></figure>

### Influence on combined methods (`COM`, `COMD`)

The problem with `PARUnl` has a cascading effect on more accurate approximation methods - `COM` and `COMD`.

The `COM` method calculates the intersection of the results of several grammars, including `PARUnl`.  
Only thanks to other components (`PAR2E`, `PARErase`), the overall execution time is not as bad as with the `PARUnl`
method.

The situation is similar with the `COMD` grammar, in which UCFS is repeatedly invoked with the `PARUnl` grammar.

#### Accuracy of approximations

<figure style="text-align: center;"> <img alt="taint statistics" src="taint/statistics_taint.png"/> <figcaption>Approximation spread (R<sub>over</sub> - R<sub>under</sub>) for all graphs and grammars in the <code>taint</code></figcaption></figure>

#### Per-Graph Trade-off Analysis (Time vs. Precision)

The following charts illustrate the relationship between execution time and approximation accuracy (measured as $R_{over} - R_{under}$) for individual graphs:

|                                                                                      |                                                                                  |
|--------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| **Graph `backflash`:**                                                               | **Graph `batterydoc`:**                                                          |
| <img alt="backflash" src="taint/application/backflash_time_vs_diff_taint.png">       | <img alt="batterydoc" src="taint/application/batterydoc_time_vs_diff_taint.png"> |
| **Graph `droidkongfu`:**                                                             | **Graph `fakebanker`:**                                                          |
| <img alt="droidkongfu" src="taint/application/droidkongfu_time_vs_diff_taint.png">   | <img alt="fakebanker" src="taint/application/fakebanker_time_vs_diff_taint.png"> |
| **Graph `fakedaum`:**                                                                | **Graph `faketaobao`:**                                                          |
| <img alt="fakedaum" src="taint/application/fakedaum_time_vs_diff_taint.png">         | <img alt="faketaobao" src="taint/application/faketaobao_time_vs_diff_taint.png"> |
| **Graph `jollyserv`:**                                                               | **Graph `loozfon`:**                                                             |
| <img alt="jollyserv" src="taint/application/jollyserv_time_vs_diff_taint.png">       | <img alt="loozfon" src="taint/application/loozfon_time_vs_diff_taint.png">       |
| **Graph `roidsec`:**                                                                 | **Graph `uranai`:**                                                              |
| <img alt="roidsec" src="taint/application/roidsec_time_vs_diff_taint.png">           | <img alt="uranai" src="taint/application/uranai_time_vs_diff_taint.png">         |
| **Graph `zertsecurity`:**                                                            |                                                                                  |
| <img alt="zertsecurity" src="taint/application/zertsecurity_time_vs_diff_taint.png"> |                                                                                  |

### Results: `taint_additional` Dataset

The comparison was conducted on the [`taint_additional dataset`](../taint_additional/), which contains larger graphs.

#### Implementation Comparison (Go vs. Kotlin per Grammar)

|                                                                                       |                                                                                     |
|---------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| **Grammar `PAR`:**                                                                    | **Grammar `PAR2`:**                                                                 |
| <img alt="PAR" src="taint_additional/parity_go_vs_kotlin_taint_additional.png">       | <img alt="PAR2" src="taint_additional/parity2_go_vs_kotlin_taint_additional.png">   |
| **Grammar `PAR2E`:**                                                                  | **Grammar `PARUnl`:**                                                               |
| <img alt="PAR2E" src="taint_additional/se_go_vs_kotlin_taint_additional.png">         | <img alt="PARUnl" src="taint_additional/project_go_vs_kotlin_taint_additional.png"> |
| **Grammar `PARErase`:**                                                               | **Grammar `PARD`:**                                                                 |
| <img alt="PARErase" src="taint_additional/exclude_go_vs_kotlin_taint_additional.png"> | <img alt="PARD" src="taint_additional/parityD_go_vs_kotlin_taint_additional.png">   |
| **Grammar `COM`:**                                                                    | **Grammar `COMD`:**                                                                 |
| <img alt="COM" src="taint_additional/all_go_vs_kotlin_taint_additional.png">          | <img alt="COMD" src="taint_additional/on-demand_go_vs_kotlin_taint_additional.png"> |

On the larger `taint_additional` dataset, both implementations experience timeout issues (`T/O`), and the GLL-based implementation also experiences out-of-memory issues (`OOM`) for many grammar-graph combinations. Overall, however, the GLL-based implementation provided the answer in more cases. However, where comparisons are possible:

1. Successful completions:
    - On `scipiex` (`PAR`): Kotlin 28.5s vs. Go 2256s (79× speedup)
    - On `simhosy` (`PAR`): Kotlin 17.2s vs. Go 1257s (73× speedup)

2. Challenges:
    - Kotlin encounters `OOM` with `PAR2`, `PARErase`, and `PARD` grammars on `phospy`
    - Go experiences timeouts more frequently across grammars

#### Performance Shift: `PARErase` vs. `PARD`

In the smaller `taint` dataset, `PARErase` was faster than `PARD`. However, in `taint_additional`, this relationship flips
due to the multi-pass execution bottleneck.

This can be explained by the fact that as the graph size and the number of unique bracket identifiers ($n_b$) increase, `PARErase's` performance significantly decreases: the algorithm performs an exhaustive path search in a loop for each unique bracket identifier ([code](../../src/main/kotlin/org/cfl_idr_with_gll/Approximation.kt#L405)).

### Profiling problem graph

Similar to profiling the `PARUnl` grammar for simpler graphs, a problem with the `equals` function is visible here:

<figure style="text-align: center;"> <img alt="profiling grammar `parity2`" src="figures/profiling_parity2.jpg"/> <figcaption>The results of profiling the <code>taint_additional/skullkey.dot</code> graph using the grammar <code>PAR</code></figcaption></figure>

#### Accuracy of approximations

<figure style="text-align: center;"> <img alt="taint_additional statistics" src="taint_additional/statistics_taint_additional.png"/> <figcaption>Approximation spread (R<sub>over</sub> - R<sub>under</sub>) for all graphs and grammars in the <code>taint_additonal</code></figcaption></figure>

#### Per-Graph Trade-off Analysis (Time vs. Precision)

|                                                                                                  |                                                                                                  |
|--------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| **Graph `phospy`:**                                                                              | **Graph `scipiex`:**                                                                             |
| <img alt="phospy" src="taint_additional/application/phospy_time_vs_diff_taint_additional.png">   | <img alt="scipiex" src="taint_additional/application/scipiex_time_vs_diff_taint_additional.png"> |
| **Graph `simhosy`:**                                                                             |                                                                                                  |
| <img alt="simhosy" src="taint_additional/application/simhosy_time_vs_diff_taint_additional.png"> |                                                                                                  |

### Summary and Conclusions

1. **Performance Improvement:** The Kotlin implementation using the GLL-based algorithm provides a significant average speedup (12.3x) compared to the Go benchmark solver in benchmarks where both implementations succeed.

2. **Correctness preserved:** Identical approximation results across implementations validate the semantic fidelity of the UCFS-based approach.

3. **Systemic equals bottleneck:** The primary performance limitation stems from inefficient equality comparisons within UCFS. During parse forest construction and traversal, the solver frequently compares complex derivation structures. This issue manifests most severely in grammars producing large intermediate structures (`PARUnl`, multi-pass variants) but affects all grammars on sufficiently large inputs.

4. **Scalability trade-offs:** While Kotlin handles moderate-scale graphs more efficiently, both implementations face fundamental scalability barriers on the largest inputs.

## Data set analysis

### Value-flow

The comparison was conducted on the [`valueflow dataset`](../valueflow/).

#### Per-Graph Trade-off Analysis (Time vs. Precision)

|                                                                                  |                                                                                    |
|----------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| **Graph `cactus`:**                                                              | **Graph `leela`:**                                                                 |
| <img alt="cactus" src="valueflow/application/cactus_time_vs_diff_valueflow.png"> | <img alt="leela" src="valueflow/application/leela_time_vs_diff_valueflow.png">     |
| **Graph `nab`:**                                                                 | **Graph `omnetpp`:**                                                               |
| <img alt="nab" src="valueflow/application/nab_time_vs_diff_valueflow.png">       | <img alt="omnetpp" src="valueflow/application/omnetpp_time_vs_diff_valueflow.png"> |
| **Graph `parest`:**                                                              | **Graph `povray`:**                                                                |
| <img alt="parest" src="valueflow/application/parest_time_vs_diff_valueflow.png"> | <img alt="povray" src="valueflow/application/povray_time_vs_diff_valueflow.png">   |
| **Graph `x264`:**                                                                | **Graph `xz`:**                                                                    |
| <img alt="x264" src="valueflow/application/x264_time_vs_diff_valueflow.png">     | <img alt="xz" src="valueflow/application/xz_time_vs_diff_valueflow.png">           |

Each benchmark in the `valueflow` dataset was executed 3 times; reported values represent averages and 95% confidence intervals.

All grammars exhibit almost identical runtime behavior. At the same time, the `imagick` and `perlbench` graphs did meet the time-out.

This suggests that in the meaning value-flow mode, all grammars work approximately the same way, regardless of their complexity.

This happens because in value-flow mode, the analysis is limited to only those paths that start with `[` and end with `]`. For these graphs, this significantly narrows the search area (for example, only when preprocessing ([function removeValueflowUnreachable](../../src/main/kotlin/org/cfl_idr_with_gll/graph/ValueflowExtensions.kt#L24)) the `x264` graph, the number of vertices decreases from **49 806** to **13 044**, and the number of edges from **56 376** to **14 351**). Therefore, the difference between grammars becomes insignificant.

However, the approximation yielded an accurate result (as confirmed by the results of the paper). This means that this test suite is not particularly challenging.

### Graphs Unlimited

The comparison was conducted on the [`graphs_unlimited dataset`](../graphs_unlimited/).  
The `graphs_unlimited` dataset represents the most demanding benchmarks for Interleaved Dyck Reachability. Due to their scale, measurements were limited to a single run per graph using only the simplest grammar (`PAR`) to establish a feasibility baseline.

<figure style="text-align: center;"> <img alt="graphs_unlimited parity perfomance" src="graphs_unlimited/parity_comparison_of_times_graphs_unlimited.png"/> <figcaption>Performance of <code>PAR</code> grammar for <code>graphs_unlimited</code></figcaption></figure>

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

<figure style="text-align: center;"> <img alt="graphs_unlimited statistics" src="graphs_unlimited/statistics_graphs_unlimited.png"/> <figcaption>R<sub>over</sub> - R<sub>under</sub> for all <code>graphs_unlimited</code> graphs and grammars</figcaption></figure>

For the subset of graphs that did complete (e.g., basic, collection, generalJava), the precision was high.

- In most completed cases like collection_slx and generalJava_slx, the precision gap $|R_{over} - R_{under}|$ was 0, indicating that the approximation reached the exact solution.
- Even in cases with a gap, such as basic or cornerCases, the difference remained constant at 2 across almost all grammar types.
