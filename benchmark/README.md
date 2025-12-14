# Performance Benchmark

## Overview

This benchmark suite measures and compares the execution performance of two implementations of Interleaved Dyck
Reachability approximation algorithms:

[Original Go Implementation](https://github.com/kobusgiovanna/cfl-idr/tree/main) - reference implementation from the
[research paper](https://www.researchgate.net/publication/390804794_CFL-based_methods_for_approximating_interleaved_Dyck_reachability)

GLL-based Kotlin Implementation - new implementation using
the [UCFS GLL solver](https://github.com/FormalLanguageConstrainedPathQuerying/UCFS)

## How to Run the Benchmark

1. Create a Python virtual environment:

```bash
python3 -m venv venv
```

2. Activate the virtual environment:

```bash
source venv/bin/activate
```

3. Install required dependencies:

```bash
pip install -r requirements.txt
``` 

4. Built JAR file:

```bash
(cd .. && ./gradlew shadowJar -x test)
```

5. Run the benchmark:

```bash
python3 benchmark.py
```

## Directory structure

```text
.
├── benchmark.py
├── comparable_impl/     # Reference implementation for comparison
│   ├── algo_go          # Compiled binary of the original Go implementation
│   └── mcfg/            # Python files for algo_go
├── plots/               # Performance plots (created automatically)
└── taint/               # Benchmark graph collection for analysis
    ├── backflash.dot
    ├── batterydoc.dot
    └── ...

```
