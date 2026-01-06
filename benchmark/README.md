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
├── taint_additional/       # Graph collections for comparison_of_times
├── valueflow/
└── graphs_unlimited/
```

## The size of the selected benchmarks

|V| and |E| represent the number of nodes and edges, respectively; $n_α$ and $n_β$ represent the number of parenthesis and bracket labels present in the graph

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
