# Performance Benchmarks

## Overview

This benchmark suite evaluates the performance and precision characteristics of Interleaved Dyck Reachability
approximation algorithms.

Two implementations are considered:

- [Original Go Implementation](https://github.com/kobusgiovanna/cfl-idr/tree/main) - reference implementation from
  the [research paper](https://www.researchgate.net/publication/390804794_CFL-based_methods_for_approximating_interleaved_Dyck_reachability)

- GLL-based Kotlin Implementation - new implementation using
  the [UCFS GLL solver](https://github.com/FormalLanguageConstrainedPathQuerying/UCFS)

The benchmark suite contains two independent benchmarks:

1) Performance benchmark of two implementations

   Compares execution time of the Go and Kotlin implementations.

2) A benchmark that evaluates the performance of only the GLL-based implementation

   Evaluates execution time and approximation accuracy on other graphs.
3) Value-Flow Analysis Benchmark
   Specialized benchmark for memory value-flow analysis with domain-specific optimizations.

## How to Run the Benchmarks

1. Create and activate a Python virtual environment:

```bash
python3 -m venv venv

source venv/bin/activate
```

2. Install required dependencies:

```bash
pip install -r requirements.txt
``` 

3. Build the Kotlin executable JAR:

```bash
(cd .. && ./gradlew shadowJar -x test)
```

4. Run the benchmarks:

Run both benchmarks sequentially:

```bash
./run_all.sh
```

Or run them individually:

```bash
python3 comparison_go_vs_kotlin.py                    # Go vs Kotlin benchmark
python3 comparison_of_times.py <input_dir>            # Kotlin-only benchmark
python3 comparison_of_times.py <input_dir> -valueflow # Kotlin-Only benchmark (value-flow analysis)
```

## Output

All plots are saved automatically in the `plots/` directory.

### Performance plots

- `parity_go_vs_kotlin.png`, `parity2_go_vs_kotlin.png`, ..., `on-demand_go_vs_kotlin.png`

  Execution time comparison (Go vs Kotlin)

- `parity_comparison_of_times_<input_dir>.png`, ..., `on-demand_comparison_of_times_<input_dir>.png`

  Execution time of Kotlin implementation only

### Precision plots

- `over_under_diff_all_grammars_go_vs_kotlin.png`

  | $R_{over} - R_{under}$ | for all grammars (Go vs Kotlin benchmark)

- `over_under_diff_all_grammars_<input_dir>.png`

  | $R_{over} - R_{under}$ | for all grammars (Kotlin-only benchmark)

## Directory structure

```text
.
├── comparison_go_vs_kotlin.py
├── comparison_of_times.py
├── utils.py                # Shared benchmarking utilities
├── comparable_impl/        # Reference implementation for comparison
│   ├── algo_go             # Compiled binary of the original Go implementation
│   └── mcfg/               # Python files for algo_go
├── plots/                  # Performance plots (created automatically)
├── taint/                  # Graph collection for comparison_go_vs_kotlin
│   ├── backflash.dot
│   ├── batterydoc.dot
│   └── ...
└── graphs_unlimited/       # Graph collection for comparison_of_times
    ├── basic_slx.dot
    ├── basic.dot
    └── ...
```
